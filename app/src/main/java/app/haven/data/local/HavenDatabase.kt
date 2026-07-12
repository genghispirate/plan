package app.haven.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import app.haven.data.local.converter.Converters
import app.haven.data.local.dao.BackupDao
import app.haven.data.local.dao.BlockedAppDao
import app.haven.data.local.dao.ClaimDao
import app.haven.data.local.dao.EconomyDao
import app.haven.data.local.dao.HabitDao
import app.haven.data.local.dao.OnboardingDao
import app.haven.data.local.dao.StreakDao
import app.haven.data.local.dao.TimelapseDao
import app.haven.data.local.dao.WorldDao
import app.haven.data.local.entity.BackupRecordEntity
import app.haven.data.local.entity.BlockedAppEntity
import app.haven.data.local.entity.EconomyLedgerEntity
import app.haven.data.local.entity.HabitLogEntity
import app.haven.data.local.entity.OnboardingChoiceEntity
import app.haven.data.local.entity.PendingClaimEntity
import app.haven.data.local.entity.StreakEntity
import app.haven.data.local.entity.TimelapseFrameEntity
import app.haven.data.local.entity.WorldSnapshotEntity

/**
 * The single offline Room database. Its underlying SQLite file is what the
 * local serialization manager (Section 5.3) zips and encrypts for backup, so
 * the schema is exported (see ksp room.schemaLocation) for migration safety.
 */
@Database(
    entities = [
        WorldSnapshotEntity::class,
        HabitLogEntity::class,
        PendingClaimEntity::class,
        EconomyLedgerEntity::class,
        StreakEntity::class,
        OnboardingChoiceEntity::class,
        BlockedAppEntity::class,
        BackupRecordEntity::class,
        TimelapseFrameEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class HavenDatabase : RoomDatabase() {
    abstract fun worldDao(): WorldDao
    abstract fun habitDao(): HabitDao
    abstract fun claimDao(): ClaimDao
    abstract fun economyDao(): EconomyDao
    abstract fun streakDao(): StreakDao
    abstract fun onboardingDao(): OnboardingDao
    abstract fun blockedAppDao(): BlockedAppDao
    abstract fun backupDao(): BackupDao
    abstract fun timelapseDao(): TimelapseDao

    companion object {
        const val DATABASE_NAME = "haven.db"
    }
}
