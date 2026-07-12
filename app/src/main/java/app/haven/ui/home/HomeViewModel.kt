package app.haven.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.haven.data.local.entity.PendingClaimEntity
import app.haven.data.repository.ClaimRepository
import app.haven.data.repository.EconomyRepository
import app.haven.data.repository.WorldRepository
import app.haven.data.world.WorldState
import app.haven.domain.usecase.BankHabitsUseCase
import app.haven.domain.usecase.ClaimRewardUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Connects the framed home UI and diorama renderer to the offline data layer,
 * and drives the Claim ritual: banks any detected habits into pending rewards on
 * open, exposes them for the tactile claim cards, and applies a claim (economy +
 * world evolution + rest clearing) through [ClaimRewardUseCase].
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val worldRepository: WorldRepository,
    private val claimRepository: ClaimRepository,
    private val bankHabits: BankHabitsUseCase,
    private val claimReward: ClaimRewardUseCase,
    economyRepository: EconomyRepository,
) : ViewModel() {

    init {
        viewModelScope.launch {
            worldRepository.ensureInitialized()
            // Convert any detected-but-unclaimed activity (e.g. interceptor water
            // logs) into pending rewards awaiting the deliberate claim.
            bankHabits()
        }
    }

    val world: StateFlow<WorldState?> = worldRepository.observeWorld()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val growthPoints: StateFlow<Long> = economyRepository.observeGrowthPoints()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0L)

    val havenCoins: StateFlow<Long> = economyRepository.observeHavenCoins()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0L)

    val pendingClaims: StateFlow<List<PendingClaimEntity>> = claimRepository.observePending()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun onClaim(claimId: Long) {
        viewModelScope.launch { claimReward(claimId) }
    }
}
