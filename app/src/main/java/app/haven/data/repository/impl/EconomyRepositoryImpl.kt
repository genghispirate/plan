package app.haven.data.repository.impl

import androidx.room.withTransaction
import app.haven.data.local.HavenDatabase
import app.haven.data.local.entity.EconomyLedgerEntity
import app.haven.data.model.RewardMetric
import app.haven.data.repository.EconomyRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class EconomyRepositoryImpl(
    private val db: HavenDatabase,
) : EconomyRepository {

    private val economyDao get() = db.economyDao()

    override fun observeGrowthPoints(): Flow<Long> =
        economyDao.observeBalance(RewardMetric.GROWTH_POINTS).map { it ?: 0L }

    override fun observeHavenCoins(): Flow<Long> =
        economyDao.observeBalance(RewardMetric.HAVEN_COINS).map { it ?: 0L }

    override suspend fun spendHavenCoins(amount: Long, reason: String): Result<Long> =
        runCatching {
            require(amount > 0) { "Spend amount must be positive" }
            db.withTransaction {
                val current = economyDao.latestBalance(RewardMetric.HAVEN_COINS) ?: 0L
                check(current >= amount) { "Insufficient Haven Coins: have $current, need $amount" }
                val after = current - amount
                economyDao.insert(
                    EconomyLedgerEntity(
                        metric = RewardMetric.HAVEN_COINS,
                        delta = -amount,
                        balanceAfter = after,
                        reason = reason,
                        refType = "market",
                        refId = null,
                        createdAtEpochMillis = System.currentTimeMillis(),
                    ),
                )
                after
            }
        }
}
