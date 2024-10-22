package name.lmj0011.jetpackreleasetracker.helpers.workers

import android.app.Application
import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import kotlinx.coroutines.delay
import name.lmj0011.jetpackreleasetracker.database.AppDatabase
import name.lmj0011.jetpackreleasetracker.ui.projectsyncs.ProjectSyncsViewModel
import timber.log.Timber
import kotlin.math.ceil

class ProjectSyncAllWorker (private val appContext: Context, parameters: WorkerParameters) :
    CoroutineWorker(appContext, parameters) {

    companion object {
        const val Progress = "Progress"
    }

    override suspend fun doWork(): Result {
        val application = appContext.applicationContext as Application
        val dataSource = AppDatabase.getInstance(appContext).projectSyncDao
        val projectSyncViewModel = ProjectSyncsViewModel(dataSource, application)
        val projectSyncs = dataSource.getAllProjectSyncsForWorker()

        if (projectSyncs.isNotEmpty()) {
            var progress = 0f
            // (progressIncrement * <collection>.size) should always equal a minimum of 100
            var progressIncrement =  (100f / projectSyncs.size)

            projectSyncs.forEach { ps ->
                projectSyncViewModel.synchronizeProject(ps).join()
                progress = progress.plus(progressIncrement)
                val roundedUpProgress = ceil(progress).toInt()
                setProgress(workDataOf(Progress to roundedUpProgress))

                Timber.d("projectName: ${ps.name}, progress: $roundedUpProgress")
            }
        }

        // gives progress enough time to get passed to Observer(s)
        delay(1000L)
        AppDatabase.closeInstance()
        return Result.success()
    }
}