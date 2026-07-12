package app.haven.ui.render

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.os.BatteryManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext

/**
 * Ambient Standby Mode (Section 5.2): true when the device is in landscape AND
 * receiving power. Callers use it to fade the interface to 0, expand the
 * diorama edge-to-edge, and render the sky-box clock.
 *
 * Charging is tracked live via power connect/disconnect broadcasts; orientation
 * comes from the current [Configuration], so both recompose the caller.
 */
@Composable
fun rememberAmbientStandby(): Boolean {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val landscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    var charging by remember { mutableStateOf(isCharging(context)) }
    DisposableEffect(context) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context, intent: Intent) {
                charging = when (intent.action) {
                    Intent.ACTION_POWER_CONNECTED -> true
                    Intent.ACTION_POWER_DISCONNECTED -> false
                    else -> isCharging(ctx)
                }
            }
        }
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_POWER_CONNECTED)
            addAction(Intent.ACTION_POWER_DISCONNECTED)
        }
        context.registerReceiver(receiver, filter)
        onDispose { runCatching { context.unregisterReceiver(receiver) } }
    }

    return landscape && charging
}

private fun isCharging(context: Context): Boolean {
    val bm = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager ?: return false
    return bm.isCharging
}
