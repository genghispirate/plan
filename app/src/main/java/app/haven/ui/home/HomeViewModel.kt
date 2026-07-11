package app.haven.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.haven.data.repository.ClaimRepository
import app.haven.data.repository.EconomyRepository
import app.haven.data.repository.WorldRepository
import app.haven.data.world.WorldState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Connects the framed home UI and the diorama renderer to the offline data
 * layer: the live world drives the Canvas, and the two economy metrics feed the
 * Haven Coin chip and Growth Points gauge. Ensures a world exists on first launch.
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val worldRepository: WorldRepository,
    economyRepository: EconomyRepository,
    claimRepository: ClaimRepository,
) : ViewModel() {

    init {
        viewModelScope.launch { worldRepository.ensureInitialized() }
    }

    val world: StateFlow<WorldState?> = worldRepository.observeWorld()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val growthPoints: StateFlow<Long> = economyRepository.observeGrowthPoints()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0L)

    val havenCoins: StateFlow<Long> = economyRepository.observeHavenCoins()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0L)

    val pendingClaimCount: StateFlow<Int> = claimRepository.observePendingCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)
}
