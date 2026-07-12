package app.haven.domain.reward

import app.haven.data.model.HabitCategory

/**
 * The two-metric economy rules (Section 2), kept as one pure, tested object.
 *
 *  - Growth Points (GP): volume-based output. Earned per unit of measured
 *    activity and used to auto-fuel world evolution.
 *  - Haven Coins (HC): time-based consistency. Earned only at streak
 *    milestones (and detox challenges), spent voluntarily in the market.
 *
 * Keeping earning here — not scattered across features — is what lets the whole
 * economy be reasoned about and unit-tested in one place.
 */
object RewardRules {

    /**
     * Growth Points for a single measured signal. [amount] is in the natural
     * unit of the category (minutes, steps, ml, hours), matching what the
     * repository logged.
     */
    fun growthPointsFor(category: HabitCategory, amount: Double): Long {
        val gp = when (category) {
            HabitCategory.FOCUS -> amount * GP_PER_FOCUS_MINUTE
            HabitCategory.READING -> amount * GP_PER_READING_MINUTE
            HabitCategory.MEDITATION -> amount * GP_PER_MEDITATION_MINUTE
            HabitCategory.CLEANING -> amount * GP_PER_CLEANING_MINUTE
            HabitCategory.EXERCISE -> amount / STEPS_PER_GP        // step count
            HabitCategory.HYDRATION -> amount / ML_PER_GP          // millilitres
            HabitCategory.SLEEP -> amount * GP_PER_SLEEP_HOUR      // hours
        }
        return gp.toLong().coerceAtLeast(0)
    }

    /**
     * Haven Coins for reaching [newStreak] days in a category. Consistency, not
     * volume: a milestone every [STREAK_MILESTONE] days pays a flat reward.
     */
    fun havenCoinsForStreak(newStreak: Int): Long =
        if (newStreak > 0 && newStreak % STREAK_MILESTONE == 0) HC_PER_MILESTONE else 0L

    /** Bonus for completing a full weekend detox challenge (Section 2). */
    fun havenCoinsForWeekendDetox(): Long = HC_WEEKEND_DETOX

    // --- Tunables ---------------------------------------------------------
    private const val GP_PER_FOCUS_MINUTE = 1.0
    private const val GP_PER_READING_MINUTE = 1.5
    private const val GP_PER_MEDITATION_MINUTE = 2.0
    private const val GP_PER_CLEANING_MINUTE = 1.5
    private const val GP_PER_SLEEP_HOUR = 8.0
    private const val STEPS_PER_GP = 100.0
    private const val ML_PER_GP = 50.0

    private const val STREAK_MILESTONE = 7
    private const val HC_PER_MILESTONE = 5L
    private const val HC_WEEKEND_DETOX = 15L
}
