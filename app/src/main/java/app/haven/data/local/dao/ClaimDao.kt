package app.haven.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import app.haven.data.local.entity.PendingClaimEntity
import app.haven.data.model.ClaimStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface ClaimDao {

    @Insert
    suspend fun insert(claim: PendingClaimEntity): Long

    @Query("SELECT * FROM pending_claim WHERE status = :status ORDER BY detectedAtEpochMillis ASC")
    fun observeByStatus(status: ClaimStatus = ClaimStatus.PENDING): Flow<List<PendingClaimEntity>>

    @Query("SELECT COUNT(*) FROM pending_claim WHERE status = 'PENDING'")
    fun observePendingCount(): Flow<Int>

    @Query("SELECT * FROM pending_claim WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): PendingClaimEntity?

    @Query(
        """
        UPDATE pending_claim
        SET status = :status, claimedAtEpochMillis = :claimedAtEpochMillis
        WHERE id = :id
        """,
    )
    suspend fun updateStatus(id: Long, status: ClaimStatus, claimedAtEpochMillis: Long?)

    /** Sweep stale pending rewards to EXPIRED so the claim card stays honest. */
    @Query(
        """
        UPDATE pending_claim SET status = 'EXPIRED'
        WHERE status = 'PENDING'
          AND expiresAtEpochMillis IS NOT NULL
          AND expiresAtEpochMillis < :nowEpochMillis
        """,
    )
    suspend fun expireStale(nowEpochMillis: Long): Int
}
