package name.lmj0011.jetpackreleasetracker.helpers.workers

import android.app.Application
import android.content.Context
import androidx.work.*
import kotlinx.coroutines.*
import name.lmj0011.jetpackreleasetracker.database.AndroidXArtifact
import name.lmj0011.jetpackreleasetracker.database.AppDatabase
import name.lmj0011.jetpackreleasetracker.helpers.GMavenXmlParser
import name.lmj0011.jetpackreleasetracker.ui.libraries.LibrariesViewModel
import timber.log.Timber
import kotlin.math.ceil

class LibraryRefreshWorker (private val appContext: Context, parameters: WorkerParameters) :
    CoroutineWorker(appContext, parameters) {

    companion object {
        const val Progress = "Progress"
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val application = appContext.applicationContext as Application
        val dataSource = AppDatabase.getInstance(appContext).androidXArtifactDao
        val librariesViewModel = LibrariesViewModel(dataSource, application)

        val artifactsToInsert = mutableListOf<AndroidXArtifact>()
        val artifactsToUpdate = mutableListOf<AndroidXArtifact>()
        val newArtifactVersionsToNotifySet = mutableSetOf<String>()

        val job = async {
            val localArtifacts = dataSource.getAllAndroidXArtifacts()
            val upstreamArtifactsList = GMavenXmlParser().loadArtifacts()

            if (upstreamArtifactsList.isNotEmpty()) {
                var progress = 0f
                // (progressIncrement * <collection>.size) should always equal a minimum of 100
                var progressIncrement =  (100f / upstreamArtifactsList.size)

                for (upstreamArtifact in upstreamArtifactsList) {
                    val updatedArtifact = localArtifacts?.find { localArtifact ->
                        val upKey = "${upstreamArtifact.packageName}:${upstreamArtifact.name}"
                        val localKey = "${localArtifact.packageName}:${localArtifact.name}"
                        (upKey == localKey)
                    }.apply {
                        if (this != null){
                            if(librariesViewModel.artifactHasNewerVersion(this, upstreamArtifact)){
                                val notifyStr = "${this.packageName} ${upstreamArtifact.latestVersion}"
                                newArtifactVersionsToNotifySet.add(notifyStr)
                                Timber.d("$notifyStr was added to newArtifactVersionsToNotifySet!")
                            }

                            this.latestStableVersion = upstreamArtifact.latestStableVersion
                            this.latestVersion = upstreamArtifact.latestVersion
                        }
                    }

                    if (updatedArtifact != null) {
                        artifactsToUpdate.add(updatedArtifact)
                    } else {
                        artifactsToInsert.add(upstreamArtifact)
                    }

                    progress = progress.plus(progressIncrement)
                    val roundedUpProgress = ceil(progress).toInt()

                    if (isStopped) {
                        Timber.d("isStopped: $isStopped")
                        break
                    } else {
                        setProgress(workDataOf(Progress to roundedUpProgress))
                    }


                    Timber.d("artifactName: ${upstreamArtifact.name}, progress: $roundedUpProgress")
                }
            }


            if(artifactsToInsert.isNotEmpty()) { dataSource.insertAll(artifactsToInsert) }
            if(artifactsToUpdate.isNotEmpty()) { dataSource.updateAll(artifactsToUpdate) }
        }

        job.await()

        AppDatabase.closeInstance()
        Result.success()
    }
}