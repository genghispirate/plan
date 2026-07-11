package app.haven.domain

import app.haven.data.model.HabitCategory
import app.haven.domain.reward.RewardRules
import org.junit.Assert.assertEquals
import org.junit.Test

class RewardRulesTest {

    @Test fun `focus minutes map one to one to GP`() {
        assertEquals(30L, RewardRules.growthPointsFor(HabitCategory.FOCUS, 30.0))
    }

    @Test fun `meditation earns two GP per minute`() {
        assertEquals(20L, RewardRules.growthPointsFor(HabitCategory.MEDITATION, 10.0))
    }

    @Test fun `steps convert at one GP per hundred`() {
        assertEquals(10L, RewardRules.growthPointsFor(HabitCategory.EXERCISE, 1000.0))
    }

    @Test fun `hydration converts at one GP per fifty millilitres`() {
        assertEquals(10L, RewardRules.growthPointsFor(HabitCategory.HYDRATION, 500.0))
    }

    @Test fun `sleep hours are weighted heavily`() {
        assertEquals(64L, RewardRules.growthPointsFor(HabitCategory.SLEEP, 8.0))
    }

    @Test fun `haven coins pay only on streak milestones`() {
        assertEquals(0L, RewardRules.havenCoinsForStreak(3))
        assertEquals(5L, RewardRules.havenCoinsForStreak(7))
        assertEquals(5L, RewardRules.havenCoinsForStreak(14))
        assertEquals(0L, RewardRules.havenCoinsForStreak(0))
    }
}
