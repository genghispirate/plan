package app.haven

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

/**
 * Application entry point. Hosts the Hilt object graph and supplies WorkManager
 * with a Hilt-aware [HiltWorkerFactory] (the default initializer is removed in
 * the manifest) so background workers — daily backup, timelapse compile — can
 * inject the same singleton repositories the UI uses.
 */
@HiltAndroidApp
class HavenApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}
