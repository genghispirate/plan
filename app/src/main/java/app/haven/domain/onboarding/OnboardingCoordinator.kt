package app.haven.domain.onboarding

import app.haven.data.local.entity.OnboardingChoiceEntity
import app.haven.data.repository.OnboardingRepository
import app.haven.data.repository.WorldRepository
import app.haven.data.world.AestheticChoices
import app.haven.data.world.HearthStonework
import app.haven.data.world.HearthWoodGrain
import app.haven.data.world.PathLayoutStyle
import app.haven.data.world.WorldState
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * What the UI should show for onboarding right now.
 *  - [step] non-null: present it full-screen and require a choice to proceed.
 *  - [step] null while not [isComplete]: today's choice is done (or the next one
 *    hasn't unlocked yet) — the app is usable; come back on [nextUnlockDayIndex].
 */
data class OnboardingUiState(
    val step: OnboardingStep?,
    val completedCount: Int,
    val isComplete: Boolean,
    val nextUnlockDayIndex: Int?,
)

/**
 * Drives the Endowment Effect onboarding (Section 1.3): one permanent aesthetic
 * choice per day for the first three days. Gating is pure and testable;
 * committing persists the choice and applies it irreversibly to the world.
 */
class OnboardingCoordinator @Inject constructor(
    private val onboardingRepository: OnboardingRepository,
    private val worldRepository: WorldRepository,
) {
    /** Persists [optionId] for [step] and stamps it permanently onto the world. */
    suspend fun commit(step: OnboardingStep, optionId: String) {
        onboardingRepository.commitChoice(step.key, optionId, step.dayIndex)

        val world = worldRepository.currentWorld()
        val updated = applyToAesthetics(world.aesthetics, step, optionId)
        val locked = updated.hearthWoodGrain != null &&
            updated.pathLayout != null &&
            updated.hearthStonework != null
        worldRepository.persist(world.copy(aesthetics = updated.copy(locked = locked)))
    }

    private fun applyToAesthetics(
        current: AestheticChoices,
        step: OnboardingStep,
        optionId: String,
    ): AestheticChoices = when (step) {
        OnboardingStep.HEARTH_WOOD_GRAIN ->
            current.copy(hearthWoodGrain = HearthWoodGrain.valueOf(optionId))
        OnboardingStep.PATH_LAYOUT ->
            current.copy(pathLayout = PathLayoutStyle.valueOf(optionId))
        OnboardingStep.HEARTH_STONEWORK ->
            current.copy(hearthStonework = HearthStonework.valueOf(optionId))
    }

    companion object {
        private val DAY_MILLIS: Long = TimeUnit.DAYS.toMillis(1)

        /** Pure projection of onboarding progress; no side effects. */
        fun computeState(
            world: WorldState?,
            choices: List<OnboardingChoiceEntity>,
            nowEpochMillis: Long = System.currentTimeMillis(),
        ): OnboardingUiState {
            val committedKeys = choices.filter { it.locked }.map { it.choiceKey }.toSet()
            val completed = OnboardingStep.ordered.count { it.key in committedKeys }
            if (completed >= OnboardingStep.TOTAL) {
                return OnboardingUiState(null, completed, isComplete = true, nextUnlockDayIndex = null)
            }

            val nextStep = OnboardingStep.ordered.first { it.key !in committedKeys }
            // Day 0 = install day, so a step with dayIndex N unlocks on day N-1.
            val createdAt = world?.createdAtEpochMillis ?: nowEpochMillis
            val currentDay = ((nowEpochMillis - createdAt) / DAY_MILLIS).toInt().coerceAtLeast(0)
            val unlocked = currentDay >= (nextStep.dayIndex - 1)

            return OnboardingUiState(
                step = if (unlocked) nextStep else null,
                completedCount = completed,
                isComplete = false,
                nextUnlockDayIndex = if (unlocked) null else nextStep.dayIndex,
            )
        }
    }
}
