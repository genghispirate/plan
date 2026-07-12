package app.haven.domain

import app.haven.data.local.entity.OnboardingChoiceEntity
import app.haven.data.world.WorldState
import app.haven.domain.onboarding.OnboardingCoordinator
import app.haven.domain.onboarding.OnboardingStep
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.TimeUnit

class OnboardingCoordinatorTest {

    private val day = TimeUnit.DAYS.toMillis(1)
    private fun world() = WorldState.newWorld("w", seed = 1L, nowEpochMillis = 0L)

    private fun committed(step: OnboardingStep, value: String) = OnboardingChoiceEntity(
        choiceKey = step.key,
        choiceValue = value,
        dayIndex = step.dayIndex,
        locked = true,
        committedAtEpochMillis = 0L,
    )

    @Test fun `first choice is available on install day`() {
        val state = OnboardingCoordinator.computeState(world(), emptyList(), nowEpochMillis = 0L)
        assertEquals(OnboardingStep.HEARTH_WOOD_GRAIN, state.step)
        assertEquals(0, state.completedCount)
        assertFalse(state.isComplete)
    }

    @Test fun `second choice stays locked until the next day`() {
        val choices = listOf(committed(OnboardingStep.HEARTH_WOOD_GRAIN, "WALNUT"))
        val state = OnboardingCoordinator.computeState(world(), choices, nowEpochMillis = 0L)
        assertNull(state.step)
        assertEquals(1, state.completedCount)
        assertEquals(2, state.nextUnlockDayIndex)
    }

    @Test fun `second choice unlocks on day two`() {
        val choices = listOf(committed(OnboardingStep.HEARTH_WOOD_GRAIN, "WALNUT"))
        val state = OnboardingCoordinator.computeState(world(), choices, nowEpochMillis = day)
        assertEquals(OnboardingStep.PATH_LAYOUT, state.step)
    }

    @Test fun `all three choices completes onboarding`() {
        val choices = listOf(
            committed(OnboardingStep.HEARTH_WOOD_GRAIN, "WALNUT"),
            committed(OnboardingStep.PATH_LAYOUT, "WINDING"),
            committed(OnboardingStep.HEARTH_STONEWORK, "SLATE"),
        )
        val state = OnboardingCoordinator.computeState(world(), choices, nowEpochMillis = 3 * day)
        assertTrue(state.isComplete)
        assertNull(state.step)
        assertEquals(3, state.completedCount)
    }
}
