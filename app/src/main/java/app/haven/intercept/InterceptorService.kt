package app.haven.intercept

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import app.haven.R
import app.haven.data.local.dao.BlockedAppDao
import app.haven.data.local.entity.BlockedAppEntity
import app.haven.data.model.DataSource
import app.haven.data.model.GateType
import app.haven.data.model.HabitCategory
import app.haven.data.repository.HabitRepository
import app.haven.intercept.gate.ActionGateRequest
import app.haven.intercept.gate.ActionGateResult
import app.haven.intercept.overlay.ActionGateScreen
import app.haven.intercept.overlay.OverlayHost
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject

/**
 * The screen-time interceptor (Section 3). A specialUse foreground service that
 * polls the foreground app and, when a user-blocked app crosses its daily
 * limit, raises the Action Gate overlay. Passing the gate grants a short grace
 * window; leaving sends the user home. Nothing is counted down passively — the
 * gate demands an active micro-habit.
 */
@AndroidEntryPoint
class InterceptorService : Service() {

    @Inject lateinit var foregroundMonitor: ForegroundAppMonitor
    @Inject lateinit var blockedAppDao: BlockedAppDao
    @Inject lateinit var habitRepository: HabitRepository

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private lateinit var overlayHost: OverlayHost

    /** Latest enabled block configs, keyed by package. Refreshed reactively. */
    @Volatile private var blockedApps: Map<String, BlockedAppEntity> = emptyMap()

    /** Package currently behind a raised gate, or null. Read across threads. */
    @Volatile private var gatedPackage: String? = null

    /** package -> epoch millis until which the gate is suppressed after a pass. */
    private val graceUntil = ConcurrentHashMap<String, Long>()

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        overlayHost = OverlayHost(this)
        observeBlockedApps()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startAsForeground()
        startPolling()
        return START_STICKY
    }

    private fun observeBlockedApps() {
        scope.launch {
            blockedAppDao.observeEnabled().collect { list ->
                blockedApps = list.associateBy { it.packageName }
            }
        }
    }

    private fun startPolling() {
        scope.launch {
            while (isActive) {
                evaluateForeground()
                delay(POLL_INTERVAL_MILLIS)
            }
        }
    }

    private suspend fun evaluateForeground() {
        val pkg = foregroundMonitor.currentForegroundPackage()
        val config = pkg?.let { blockedApps[it] }

        val shouldGate = when {
            pkg == null || pkg == packageName -> false
            config == null -> false
            isInGrace(pkg) -> false
            else -> isOverLimit(pkg, config)
        }

        when {
            shouldGate && gatedPackage != pkg -> raiseGate(config!!)
            !shouldGate && gatedPackage != null -> dismissGate()
        }
    }

    private fun isInGrace(pkg: String): Boolean {
        val until = graceUntil[pkg] ?: return false
        if (System.currentTimeMillis() < until) return true
        graceUntil.remove(pkg)
        return false
    }

    /** A zero/absent limit is a hard block; otherwise gate once today's cap is hit. */
    private fun isOverLimit(pkg: String, config: BlockedAppEntity): Boolean =
        if (config.dailyLimitMinutes <= 0) true
        else foregroundMonitor.todayForegroundMinutes(pkg) >= config.dailyLimitMinutes

    private suspend fun raiseGate(config: BlockedAppEntity) {
        gatedPackage = config.packageName
        val request = ActionGateRequest(
            packageName = config.packageName,
            appLabel = config.label,
            gateType = config.gateType,
            seed = config.packageName.hashCode().toLong(),
        )
        withContext(Dispatchers.Main) {
            overlayHost.show {
                ActionGateScreen(request = request) { result -> onGateResult(result, config) }
            }
        }
    }

    private suspend fun dismissGate() {
        gatedPackage = null
        withContext(Dispatchers.Main) { overlayHost.hide() }
    }

    /** Invoked on the main thread from the overlay composition. */
    private fun onGateResult(result: ActionGateResult, config: BlockedAppEntity) {
        when (result) {
            is ActionGateResult.Overridden -> {
                graceUntil[config.packageName] = System.currentTimeMillis() + GRACE_MILLIS
                logMicroHabit(result.gateType)
            }
            ActionGateResult.Left -> goHome()
        }
        gatedPackage = null
        overlayHost.hide()
    }

    /** Hydration gates log a real glass of water; others are transitions only. */
    private fun logMicroHabit(gateType: GateType) {
        if (gateType != GateType.HYDRATION_LOG) return
        scope.launch {
            habitRepository.log(
                category = HabitCategory.HYDRATION,
                amount = GLASS_ML,
                unit = "ml",
                source = DataSource.INTERCEPTOR_GATE,
                occurredAtEpochMillis = System.currentTimeMillis(),
            )
        }
    }

    private fun goHome() {
        val home = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(home)
    }

    private fun startAsForeground() {
        val channelId = ensureNotificationChannel()
        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_haven_shield)
            .setContentTitle("Haven is protecting your focus")
            .setContentText("Watching for apps you've chosen to gate.")
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()

        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
        } else {
            0
        }
        ServiceCompat.startForeground(this, NOTIFICATION_ID, notification, type)
    }

    private fun ensureNotificationChannel(): String {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Focus protection",
                NotificationManager.IMPORTANCE_LOW,
            ).apply {
                description = "Keeps the screen-time interceptor running."
                setShowBadge(false)
            }
            manager.createNotificationChannel(channel)
        }
        return CHANNEL_ID
    }

    override fun onDestroy() {
        scope.cancel()
        if (this::overlayHost.isInitialized) overlayHost.destroy()
        super.onDestroy()
    }

    companion object {
        private const val CHANNEL_ID = "haven_interceptor"
        private const val NOTIFICATION_ID = 4711
        private const val POLL_INTERVAL_MILLIS = 1_200L
        private const val GRACE_MILLIS = 5 * 60 * 1_000L
        private const val GLASS_ML = 250.0

        /** Start the interceptor only when both special permissions are granted. */
        fun start(context: Context) {
            if (!InterceptorPermissions.hasAllAccess(context)) return
            val intent = Intent(context, InterceptorService::class.java)
            context.startForegroundService(intent)
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, InterceptorService::class.java))
        }
    }
}
