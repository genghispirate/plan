package app.haven.data.repository.impl

import androidx.room.withTransaction
import app.haven.data.local.HavenDatabase
import app.haven.data.local.entity.EconomyLedgerEntity
import app.haven.data.local.entity.PendingClaimEntity
import app.haven.data.model.ClaimStatus
import app.haven.data.model.HabitCategory
import app.haven.data.model.RewardMetric
import app.haven.data.repository.ClaimOutcome
import app.haven.data.repository.ClaimRepository
import kotlinx.coroutines.flow.Flow

/**
 * Owns the deliberate Claim transaction. Banking and claiming both run inside a
 * single Room transaction so a pending reward can never be applied twice or be
 * left half-applied to the ledger.
 */
class ClaimRepositoryImpl(
    private val db: HavenDatabase,
) : ClaimRepository {

    private val claimDao get() = db.claimDao()
    private val habitDao get() = db.habitDao()
    private val economyDao get() = db.economyDao()

    override fun observePending(): Flow<List<PendingClaimEntity>> =
        claimDao.observeByStatus(ClaimStatus.PENDING)

    override fun observePendingCount(): Flow<Int> = claimDao.observePendingCount()

    override suspend fun bank(
        category: HabitCategory,
        growthPoints: Long,
        havenCoins: Long,
        summary: String,
        logIds: List<Long>,
        detectedAtEpochMillis: Long,
        expiresAtEpochMillis: Long?,
    ): Long = db.withTransaction {
        val claimId = claimDao.insert(
            PendingClaimEntity(
                category = category,
                growthPoints = growthPoints,
                havenCoins = havenCoins,
                summary = summary,
                detectedAtEpochMillis = detectedAtEpochMillis,
                expiresAtEpochMillis = expiresAtEpochMillis,
            ),
        )
        if (logIds.isNotEmpty()) habitDao.markClaimed(logIds, claimId)
        claimId
    }

    override suspend fun claim(claimId: Long): Result<ClaimOutcome> = runCatching {
        db.withTransaction {
            val claim = claimDao.getById(claimId)
                ?: error("No pending claim with id=$claimId")
            check(claim.status == ClaimStatus.PENDING) {
                "Claim $claimId is already ${claim.status}"
            }

            val now = System.currentTimeMillis()
            val gpAfter = applyDelta(RewardMetric.GROWTH_POINTS, claim.growthPoints, claimId, now)
            val hcAfter = applyDelta(RewardMetric.HAVEN_COINS, claim.havenCoins, claimId, now)

            claimDao.updateStatus(claimId, ClaimStatus.CLAIMED, now)
            ClaimOutcome(
                growthPointsAfter = gpAfter,
                havenCoinsAfter = hcAfter,
                category = claim.category,
            )
        }
    }

    override suspend fun expireStale(nowEpochMillis: Long) {
        claimDao.expireStale(nowEpochMillis)
    }

    /** Appends a ledger row for [delta] (skipping zero) and returns the balance. */
    private suspend fun applyDelta(
        metric: RewardMetric,
        delta: Long,
        claimId: Long,
        nowEpochMillis: Long,
    ): Long {
        val current = economyDao.latestBalance(metric) ?: 0L
        if (delta == 0L) return current
        val after = current + delta
        economyDao.insert(
            EconomyLedgerEntity(
                metric = metric,
                delta = delta,
                balanceAfter = after,
                reason = "Claimed reward",
                refType = "claim",
                refId = claimId,
                createdAtEpochMillis = nowEpochMillis,
            ),
        )
        return after
    }
}
