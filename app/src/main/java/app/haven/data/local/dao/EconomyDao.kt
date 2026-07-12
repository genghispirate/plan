package app.haven.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import app.haven.data.local.entity.EconomyLedgerEntity
import app.haven.data.model.RewardMetric
import kotlinx.coroutines.flow.Flow

@Dao
interface EconomyDao {

    @Insert
    suspend fun insert(entry: EconomyLedgerEntity): Long

    /** Current balance = the latest ledger row's running total for the metric. */
    @Query(
        """
        SELECT balanceAfter FROM economy_ledger
        WHERE metric = :metric
        ORDER BY id DESC LIMIT 1
        """,
    )
    suspend fun latestBalance(metric: RewardMetric): Long?

    @Query(
        """
        SELECT balanceAfter FROM economy_ledger
        WHERE metric = :metric
        ORDER BY id DESC LIMIT 1
        """,
    )
    fun observeBalance(metric: RewardMetric): Flow<Long?>

    @Query("SELECT * FROM economy_ledger ORDER BY id DESC LIMIT :limit")
    fun observeRecent(limit: Int = 50): Flow<List<EconomyLedgerEntity>>
}
