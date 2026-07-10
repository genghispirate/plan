package app.haven.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import app.haven.data.model.BackupStatus
import app.haven.data.model.BackupTargetType
import app.haven.data.model.ClaimStatus
import app.haven.data.model.DataSource
import app.haven.data.model.GateType
import app.haven.data.model.HabitCategory
import app.haven.data.model.RewardMetric

/**
 * Singleton row holding the serialized [app.haven.data.world.WorldState] JSON.
 * Kept as one blob (id is pinned to 1) so the world is read/written atomically;
 * the renderer observes this row and redraws on change.
 */
@Entity(tableName = "world_snapshot")
data class WorldSnapshotEntity(
    @PrimaryKey val id: Int = SINGLETON_ID,
    val worldJson: String,
    val schemaVersion: Int,
    val updatedAtEpochMillis: Long,
) {
    companion object { const val SINGLETON_ID = 1 }
}

/**
 * An atomic record of a real-world (or in-app focus) habit signal. `claimId`
 * ties it to the [PendingClaimEntity] it was rolled into; `claimed` guards the
 * Claim ritual so rewards are never auto-applied.
 */
@Entity(
    tableName = "habit_log",
    indices = [
        Index("occurredAtEpochMillis"),
        Index("category"),
        Index("claimed"),
    ],
)
data class HabitLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val category: HabitCategory,
    /** Raw measured amount (minutes, steps, ml, sleep-hours, …). */
    val amount: Double,
    val unit: String,
    val source: DataSource,
    val occurredAtEpochMillis: Long,
    val claimed: Boolean = false,
    val claimId: Long? = null,
)

/**
 * Rewards banked for the deliberate Claim ritual. Detected in the background,
 * surfaced as a tactile card, and only applied to the economy on claim.
 */
@Entity(
    tableName = "pending_claim",
    indices = [Index("status"), Index("detectedAtEpochMillis")],
)
data class PendingClaimEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val category: HabitCategory,
    val growthPoints: Long,
    val havenCoins: Long,
    /** Human-readable summary shown on the claim card ("Slept 7h 40m"). */
    val summary: String,
    val status: ClaimStatus = ClaimStatus.PENDING,
    val detectedAtEpochMillis: Long,
    val claimedAtEpochMillis: Long? = null,
    /** Pending claims may expire so stale rewards don't pile up indefinitely. */
    val expiresAtEpochMillis: Long? = null,
)

/**
 * Append-only double-entry ledger for the two-metric economy. The current
 * balance is `balanceAfter` of the latest row per metric — never mutated.
 */
@Entity(
    tableName = "economy_ledger",
    indices = [Index("metric"), Index("createdAtEpochMillis")],
)
data class EconomyLedgerEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val metric: RewardMetric,
    /** Signed change; positive = earned, negative = spent in the market. */
    val delta: Long,
    val balanceAfter: Long,
    val reason: String,
    /** Optional back-reference, e.g. the claim or market purchase id. */
    val refType: String? = null,
    val refId: Long? = null,
    val createdAtEpochMillis: Long,
)

/** Per-category streak tracking. `lastCompletedEpochDay` is days since epoch. */
@Entity(tableName = "streak")
data class StreakEntity(
    @PrimaryKey val category: HabitCategory,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val lastCompletedEpochDay: Long? = null,
    /** Consistency "freezes" earned to protect a streak without guilt. */
    val freezeTokens: Int = 0,
)

/**
 * A committed onboarding aesthetic choice (endowment effect). `locked` rows are
 * immutable; the day index enforces the "one per day for 3 days" cadence.
 */
@Entity(tableName = "onboarding_choice")
data class OnboardingChoiceEntity(
    @PrimaryKey val choiceKey: String,
    val choiceValue: String,
    val dayIndex: Int,
    val locked: Boolean = false,
    val committedAtEpochMillis: Long,
)

/** A screen-time interception target and its Action-Gate configuration. */
@Entity(tableName = "blocked_app")
data class BlockedAppEntity(
    @PrimaryKey val packageName: String,
    val label: String,
    val enabled: Boolean = true,
    val dailyLimitMinutes: Int = 0,
    val gateType: GateType = GateType.PHILOSOPHY_QUOTE,
    val addedAtEpochMillis: Long,
)

/** History of encrypted backup runs through the local serialization pipeline. */
@Entity(
    tableName = "backup_record",
    indices = [Index("createdAtEpochMillis")],
)
data class BackupRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fileName: String,
    val target: BackupTargetType,
    val sizeBytes: Long,
    /** SHA-256 of the encrypted archive for integrity verification. */
    val checksum: String?,
    val status: BackupStatus,
    val createdAtEpochMillis: Long,
    val completedAtEpochMillis: Long? = null,
    val errorMessage: String? = null,
)

/**
 * Metadata for a cached daily diorama snapshot used by the timelapse compiler.
 * The bitmap itself lives on disk at [filePath]; only the index is in Room.
 */
@Entity(
    tableName = "timelapse_frame",
    indices = [Index(value = ["epochDay"], unique = true)],
)
data class TimelapseFrameEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    /** Days since epoch, at the *start of the user's day* per circadian config. */
    val epochDay: Long,
    val filePath: String,
    val focusMinutes: Int,
    val capturedAtEpochMillis: Long,
)
