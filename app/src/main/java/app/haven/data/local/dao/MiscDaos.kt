package app.haven.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import app.haven.data.local.entity.BackupRecordEntity
import app.haven.data.local.entity.BlockedAppEntity
import app.haven.data.local.entity.OnboardingChoiceEntity
import app.haven.data.local.entity.StreakEntity
import app.haven.data.local.entity.TimelapseFrameEntity
import app.haven.data.model.HabitCategory
import kotlinx.coroutines.flow.Flow

@Dao
interface StreakDao {
    @Upsert
    suspend fun upsert(streak: StreakEntity)

    @Query("SELECT * FROM streak WHERE category = :category LIMIT 1")
    suspend fun get(category: HabitCategory): StreakEntity?

    @Query("SELECT * FROM streak")
    fun observeAll(): Flow<List<StreakEntity>>
}

@Dao
interface OnboardingDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(choice: OnboardingChoiceEntity)

    @Query("SELECT * FROM onboarding_choice ORDER BY dayIndex ASC")
    fun observeAll(): Flow<List<OnboardingChoiceEntity>>

    @Query("SELECT * FROM onboarding_choice WHERE choiceKey = :key LIMIT 1")
    suspend fun get(key: String): OnboardingChoiceEntity?

    @Query("SELECT COUNT(*) FROM onboarding_choice WHERE locked = 1")
    suspend fun lockedCount(): Int
}

@Dao
interface BlockedAppDao {
    @Upsert
    suspend fun upsert(app: BlockedAppEntity)

    @Query("DELETE FROM blocked_app WHERE packageName = :packageName")
    suspend fun delete(packageName: String)

    @Query("SELECT * FROM blocked_app WHERE enabled = 1")
    fun observeEnabled(): Flow<List<BlockedAppEntity>>

    /** Snapshot for the interceptor's hot polling loop (Step 3). */
    @Query("SELECT * FROM blocked_app WHERE enabled = 1")
    suspend fun enabledSnapshot(): List<BlockedAppEntity>
}

@Dao
interface BackupDao {
    @Insert
    suspend fun insert(record: BackupRecordEntity): Long

    @Upsert
    suspend fun upsert(record: BackupRecordEntity)

    @Query("SELECT * FROM backup_record ORDER BY createdAtEpochMillis DESC LIMIT :limit")
    fun observeRecent(limit: Int = 25): Flow<List<BackupRecordEntity>>

    @Query("SELECT * FROM backup_record ORDER BY createdAtEpochMillis DESC LIMIT 1")
    suspend fun latest(): BackupRecordEntity?
}

@Dao
interface TimelapseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(frame: TimelapseFrameEntity)

    @Query("SELECT * FROM timelapse_frame ORDER BY epochDay ASC")
    suspend fun allFramesOrdered(): List<TimelapseFrameEntity>

    @Query("SELECT * FROM timelapse_frame ORDER BY epochDay ASC")
    fun observeAll(): Flow<List<TimelapseFrameEntity>>

    @Query("SELECT COUNT(*) FROM timelapse_frame")
    suspend fun frameCount(): Int
}
