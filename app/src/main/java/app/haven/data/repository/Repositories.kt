package app.haven.data.repository

import app.haven.data.backup.BackupArchive
import app.haven.data.backup.BackupTarget
import app.haven.data.local.entity.BackupRecordEntity
import app.haven.data.local.entity.OnboardingChoiceEntity
import app.haven.data.local.entity.PendingClaimEntity
import app.haven.data.model.DataSource
import app.haven.data.model.HabitCategory
import app.haven.data.world.WorldState
import kotlinx.coroutines.flow.Flow

/**
 * Clean data-layer contracts. UI/domain code depends only on these interfaces;
 * concrete Room/file wiring is injected by Hilt (see [app.haven.di.DataModule]).
 */

/** Reads and persists the serialized diorama world. */
interface WorldRepository {
    /** Reactive world; emits a fresh new-world default if none exists yet. */
    fun observeWorld(): Flow<WorldState>
    suspend fun currentWorld(): WorldState
    suspend fun persist(world: WorldState)
    /** Ensures a world exists, creating a pristine one on first launch. */
    suspend fun ensureInitialized(): WorldState
}

/** Records raw habit signals (Health Connect, manual, focus, gate completions). */
interface HabitRepository {
    fun observeUnclaimed(): Flow<List<HabitLog>>
    fun observeHistory(startEpochMillis: Long, endEpochMillis: Long): Flow<List<HabitLog>>
    suspend fun log(
        category: HabitCategory,
        amount: Double,
        unit: String,
        source: DataSource,
        occurredAtEpochMillis: Long,
    ): Long
    suspend fun total(category: HabitCategory, startEpochMillis: Long, endEpochMillis: Long): Double
}

/** Lightweight domain view of a habit log, decoupled from the Room entity. */
data class HabitLog(
    val id: Long,
    val category: HabitCategory,
    val amount: Double,
    val unit: String,
    val source: DataSource,
    val occurredAtEpochMillis: Long,
    val claimed: Boolean,
)

/**
 * The "Claim" ritual (Section 1.2). Rewards are banked as pending and applied
 * to the economy only through [claim], which is a single atomic transaction.
 */
interface ClaimRepository {
    fun observePending(): Flow<List<PendingClaimEntity>>
    fun observePendingCount(): Flow<Int>
    /** Bank a detected reward without touching balances. */
    suspend fun bank(
        category: HabitCategory,
        growthPoints: Long,
        havenCoins: Long,
        summary: String,
        logIds: List<Long>,
        detectedAtEpochMillis: Long,
        expiresAtEpochMillis: Long?,
    ): Long
    /** Apply a pending reward to the ledger; the visual dopamine hit trigger. */
    suspend fun claim(claimId: Long): Result<ClaimOutcome>
    suspend fun expireStale(nowEpochMillis: Long)
}

data class ClaimOutcome(
    val growthPointsAfter: Long,
    val havenCoinsAfter: Long,
    val category: HabitCategory,
)

/** The two-metric economy ledger (append-only). */
interface EconomyRepository {
    fun observeGrowthPoints(): Flow<Long>
    fun observeHavenCoins(): Flow<Long>
    suspend fun spendHavenCoins(amount: Long, reason: String): Result<Long>
}

/** Endowment-effect onboarding choices (Section 1.3). */
interface OnboardingRepository {
    fun observeChoices(): Flow<List<OnboardingChoiceEntity>>
    suspend fun commitChoice(key: String, value: String, dayIndex: Int)
    suspend fun isComplete(): Boolean
}

/**
 * File data export (Section 5.3). Orchestrates the local serialization manager
 * and a chosen [BackupTarget]; concrete transport stays behind [BackupTarget].
 */
interface BackupRepository {
    fun observeHistory(): Flow<List<BackupRecordEntity>>
    /** Serialize + encrypt the world & DB and push to [target]. */
    suspend fun backupNow(target: BackupTarget): Result<BackupArchive>
    /** Fetch + decrypt an archive and rehydrate the world/database. */
    suspend fun restore(target: BackupTarget, fileName: String): Result<Unit>
}
