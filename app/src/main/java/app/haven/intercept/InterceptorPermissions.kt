package app.haven.intercept

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Process
import android.provider.Settings

/**
 * Central checks + intents for the two special-access permissions the
 * interceptor needs. Both are user-granted via Settings (not runtime dialogs),
 * so the UI must route the user out and re-verify on return.
 */
object InterceptorPermissions {

    /** PACKAGE_USAGE_STATS — required to read the foreground app. */
    fun hasUsageAccess(context: Context): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.unsafeCheckOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            context.packageName,
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    /** SYSTEM_ALERT_WINDOW — required to draw the Action Gate over other apps. */
    fun hasOverlayAccess(context: Context): Boolean = Settings.canDrawOverlays(context)

    fun hasAllAccess(context: Context): Boolean =
        hasUsageAccess(context) && hasOverlayAccess(context)

    fun usageAccessSettingsIntent(): Intent =
        Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

    fun overlaySettingsIntent(context: Context): Intent =
        Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:${context.packageName}"),
        ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
}
