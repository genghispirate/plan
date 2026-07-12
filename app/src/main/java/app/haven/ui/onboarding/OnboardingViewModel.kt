package app.haven.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.haven.data.repository.OnboardingRepository
import app.haven.data.repository.WorldRepository
import app.haven.domain.onboarding.OnboardingCoordinator
import app.haven.domain.onboarding.OnboardingStep
import app.haven.domain.onboarding.OnboardingUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Decides whether the app shows an onboarding ritual or the home diorama, and
 * commits founding aesthetic choices. Recomputes reactively from the world
 * (install date + locked aesthetics) and the committed-choice records.
 */
@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val coordinator: OnboardingCoordinator,
    worldRepository: WorldRepository,
    onboardingRepository: OnboardingRepository,
) : ViewModel() {

    val state: StateFlow<OnboardingUiState?> = combine(
        worldRepository.observeWorld(),
        onboardingRepository.observeChoices(),
    ) { world, choices ->
        OnboardingCoordinator.computeState(world, choices)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun commit(step: OnboardingStep, optionId: String) {
        viewModelScope.launch { coordinator.commit(step, optionId) }
    }
}
