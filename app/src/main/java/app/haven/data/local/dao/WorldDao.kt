package app.haven.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import app.haven.data.local.entity.WorldSnapshotEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WorldDao {

    /** Reactive stream of the singleton world snapshot; drives the renderer. */
    @Query("SELECT * FROM world_snapshot WHERE id = :id LIMIT 1")
    fun observeSnapshot(id: Int = WorldSnapshotEntity.SINGLETON_ID): Flow<WorldSnapshotEntity?>

    @Query("SELECT * FROM world_snapshot WHERE id = :id LIMIT 1")
    suspend fun getSnapshot(id: Int = WorldSnapshotEntity.SINGLETON_ID): WorldSnapshotEntity?

    /** Atomic overwrite of the world blob. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(snapshot: WorldSnapshotEntity)
}
