package app.haven.domain.usecase

import app.haven.data.repository.ClaimOutcome
import app.haven.data.repository.ClaimRepository
import app.haven.data.repository.WorldRepository
import app.haven.domain.world.RestStateManager
import app.haven.domain.world.WorldEvolver
import javax.inject.Inject

/**
 * The complete Claim ritual side-effect (Section 1.2): applying a banked reward
 * atomically credits the economy, evolves the diorama per the graphics matrix,
 * and clears any Rest State — mapping the real-world achievement directly to the
 * visual dopamine hit of the world upgrading.
 */
class ClaimRewardUseCase @Inject constructor(
    private val claimRepository: ClaimRepository,
    private val worldRepository: WorldRepository,
) {
    suspend operator fun invoke(claimId: Long): Result<ClaimOutcome> {
        val outcome = claimRepository.claim(claimId).getOrElse { return Result.failure(it) }

        val now = System.currentTimeMillis()
        val current = worldRepository.currentWorld()
        val evolved = WorldEvolver.evolve(
            world = current,
            category = outcome.category,
            growthPoints = outcome.claimedGrowthPoints,
            nowEpochMillis = now,
        )
        // Completing a habit immediately returns the world to sunshine.
        val cleared = RestStateManager.clearRest(evolved, now)
        worldRepository.persist(cleared)

        return Result.success(outcome)
    }
}
