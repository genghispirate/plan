package app.haven.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import app.haven.data.local.entity.HabitLogEntity
import app.haven.data.model.HabitCategory
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {

    @Insert
    suspend fun insert(log: HabitLogEntity): Long

    @Insert
    suspend fun insertAll(logs: List<HabitLogEntity>): List<Long>

    /** Unclaimed signals awaiting aggregation into a pending claim. */
    @Query("SELECT * FROM habit_log WHERE claimed = 0 ORDER BY occurredAtEpochMillis ASC")
    fun observeUnclaimed(): Flow<List<HabitLogEntity>>

    @Query(
        """
        SELECT * FROM habit_log
        WHERE occurredAtEpochMillis BETWEEN :startEpochMillis AND :endEpochMillis
        ORDER BY occurredAtEpochMillis DESC
        """,
    )
    fun observeBetween(startEpochMillis: Long, endEpochMillis: Long): Flow<List<HabitLogEntity>>

    @Query(
        """
        SELECT COALESCE(SUM(amount), 0) FROM habit_log
        WHERE category = :category
          AND occurredAtEpochMillis BETWEEN :startEpochMillis AND :endEpochMillis
        """,
    )
    suspend fun sumAmount(
        category: HabitCategory,
        startEpochMillis: Long,
        endEpochMillis: Long,
    ): Double

    /** Attach a set of logs to the pending claim they were rolled into. */
    @Query("UPDATE habit_log SET claimed = 1, claimId = :claimId WHERE id IN (:ids)")
    suspend fun markClaimed(ids: List<Long>, claimId: Long)
}
