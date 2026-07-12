package app.haven.intercept

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Thin wrapper over [UsageStatsManager]. The interceptor engine polls
 * [currentForegroundPackage] to learn which app is on screen, and
 * [todayForegroundMinutes] to enforce per-app daily limits.
 *
 * We deliberately use event/stat queries rather than an AccessibilityService:
 * it needs only the user-granted PACKAGE_USAGE_STATS special access and never
 * reads screen content, keeping the privacy posture tight.
 */
@Singleton
class ForegroundAppMonitor @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val usageStatsManager: UsageStatsManager? =
        context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager

    /**
     * The package that most recently moved to the foreground within the last
     * [lookbackMillis]. Null if usage access is missing or nothing surfaced.
     */
    fun currentForegroundPackage(lookbackMillis: Long = DEFAULT_LOOKBACK_MILLIS): String? {
        val usm = usageStatsManager ?: return null
        val end = System.currentTimeMillis()
        val begin = end - lookbackMillis
        val events = usm.queryEvents(begin, end)
        val event = UsageEvents.Event()
        var latestPackage: String? = null
        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            if (event.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                latestPackage = event.packageName
            }
        }
        return latestPackage
    }

    /** Total foreground minutes for [packageName] since the start of today. */
    fun todayForegroundMinutes(packageName: String): Long {
        val usm = usageStatsManager ?: return 0L
        val now = System.currentTimeMillis()
        val stats = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startOfToday(), now)
        val totalMillis = stats
            ?.filter { it.packageName == packageName }
            ?.sumOf { it.totalTimeInForeground }
            ?: 0L
        return totalMillis / 60_000L
    }

    private fun startOfToday(): Long = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    private companion object {
        // Slightly longer than the poll interval so a fast switch isn't missed.
        const val DEFAULT_LOOKBACK_MILLIS = 10_000L
    }
}
