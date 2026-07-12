package app.haven.domain.usecase

import app.haven.data.local.dao.StreakDao
import app.haven.data.local.entity.StreakEntity
import app.haven.data.model.HabitCategory
import app.haven.data.repository.ClaimRepository
import app.haven.data.repository.HabitRepository
import app.haven.domain.reward.RewardRules
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Turns raw, unclaimed habit signals into banked pending rewards for the Claim
 * ritual (Section 1.2). Detected activity is never auto-applied; it is grouped
 * by category, priced by [RewardRules], streak-updated for consistency Haven
 * Coins, and parked as a pending claim the user must deliberately claim.
 *
 * Idempotent-ish: only *unclaimed* logs are banked, and banking marks them
 * claimed against the new pending claim, so re-running won't double-count.
 */
class BankHabitsUseCase @Inject constructor(
    private val habitRepository: HabitRepository,
    private val claimRepository: ClaimRepository,
    private val streakDao: StreakDao,
) {
    /** Returns the number of pending claims created. */
    suspend operator fun invoke(nowEpochMillis: Long = System.currentTimeMillis()): Int {
        val unclaimed = habitRepository.observeUnclaimed().first()
        if (unclaimed.isEmpty()) return 0

        val byCategory = unclaimed.groupBy { it.category }
        var created = 0

        for ((category, logs) in byCategory) {
            val growthPoints = logs.sumOf { RewardRules.growthPointsFor(category, it.amount) }
            val havenCoins = updateStreakAndAwardCoins(category, nowEpochMillis)
            if (growthPoints <= 0 && havenCoins <= 0) continue

            claimRepository.bank(
                category = category,
                growthPoints = growthPoints,
                havenCoins = havenCoins,
                summary = summarize(category, logs.sumOf { it.amount }, logs.first().unit),
                logIds = logs.map { it.id },
                detectedAtEpochMillis = nowEpochMillis,
                expiresAtEpochMillis = nowEpochMillis + CLAIM_TTL_MILLIS,
            )
            created++
        }
        return created
    }

    /** Advances the per-category daily streak, paying HC on milestone days. */
    private suspend fun updateStreakAndAwardCoins(
        category: HabitCategory,
        nowEpochMillis: Long,
    ): Long {
        val today = nowEpochMillis / DAY_MILLIS
        val existing = streakDao.get(category)
        val newStreak = when (existing?.lastCompletedEpochDay) {
            today -> return 0L                 // already counted today
            today - 1 -> existing.currentStreak + 1
            else -> 1
        }
        streakDao.upsert(
            StreakEntity(
                category = category,
                currentStreak = newStreak,
                longestStreak = maxOf(newStreak, existing?.longestStreak ?: 0),
                lastCompletedEpochDay = today,
                freezeTokens = existing?.freezeTokens ?: 0,
            ),
        )
        return RewardRules.havenCoinsForStreak(newStreak)
    }

    private fun summarize(category: HabitCategory, total: Double, unit: String): String {
        val amount = if (total % 1.0 == 0.0) total.toInt().toString() else "%.1f".format(total)
        val label = category.name.lowercase().replaceFirstChar { it.uppercase() }
        return "$label · $amount $unit"
    }

    private companion object {
        val DAY_MILLIS = TimeUnit.DAYS.toMillis(1)
        val CLAIM_TTL_MILLIS = TimeUnit.DAYS.toMillis(3)
    }
}
