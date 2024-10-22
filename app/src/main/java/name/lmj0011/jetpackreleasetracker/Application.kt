package name.lmj0011.jetpackreleasetracker

import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceManager
import androidx.work.*
import name.lmj0011.jetpackreleasetracker.helpers.workers.LibraryRefreshWorker
import timber.log.Timber
import java.util.concurrent.TimeUnit

class Application: android.app.Application() {
    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
        applyTheme()
        enqueueWorkers()
    }


    /**
     * Applies the App's Theme from sharedPrefs
     */
    fun applyTheme() {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)

        val modeNight = sharedPreferences.getInt(
            getString(R.string.pref_key_mode_night),
            AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        )

        AppCompatDelegate.setDefaultNightMode(modeNight)
    }

    private fun enqueueWorkers() {
        val workManager = WorkManager.getInstance(applicationContext)

        val constraints = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresDeviceIdle(true)
                .build()
        } else {
            Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
        }

        val updateWorkRequest = PeriodicWorkRequestBuilder<LibraryRefreshWorker>(
            8, TimeUnit.HOURS, // runs 3 times a day
            8, TimeUnit.HOURS
        )
            .setInitialDelay(2, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .addTag(applicationContext.getString(R.string.update_periodic_worker_tag))
            .build()

        workManager.enqueueUniquePeriodicWork(applicationContext.getString(R.string.update_periodic_worker), ExistingPeriodicWorkPolicy.REPLACE, updateWorkRequest)
    }
}