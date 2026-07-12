package app.haven.system

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import app.haven.data.local.dao.BlockedAppDao
import app.haven.intercept.InterceptorPermissions
import app.haven.intercept.InterceptorService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Re-arms the interceptor after a reboot or app update. Only starts the service
 * if the user has both special permissions AND at least one enabled block —
 * otherwise we don't run a foreground service the user isn't using.
 *
 * WorkManager reschedules its own persisted jobs, so no work is re-enqueued here.
 */
@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject lateinit var blockedAppDao: BlockedAppDao

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED &&
            intent.action != Intent.ACTION_MY_PACKAGE_REPLACED
        ) {
            return
        }
        if (!InterceptorPermissions.hasAllAccess(context)) return

        val appContext = context.applicationContext
        val pending = goAsync()
        CoroutineScope(Dispatchers.Default).launch {
            try {
                if (blockedAppDao.enabledSnapshot().isNotEmpty()) {
                    withContext(Dispatchers.Main) { InterceptorService.start(appContext) }
                }
            } finally {
                pending.finish()
            }
        }
    }
}
