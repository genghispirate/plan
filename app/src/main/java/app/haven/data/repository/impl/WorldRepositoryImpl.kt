package app.haven.data.repository.impl

import app.haven.data.local.dao.WorldDao
import app.haven.data.local.entity.WorldSnapshotEntity
import app.haven.data.repository.WorldRepository
import app.haven.data.serialization.WorldSerializer
import app.haven.data.world.WorldState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

class WorldRepositoryImpl(
    private val worldDao: WorldDao,
    private val serializer: WorldSerializer,
) : WorldRepository {

    // A stable in-memory default so the renderer always has something to draw
    // before the first world is persisted. Persisted once via ensureInitialized.
    private val bootstrap: WorldState by lazy {
        WorldState.newWorld(
            worldId = UUID.randomUUID().toString(),
            seed = System.nanoTime(),
            nowEpochMillis = System.currentTimeMillis(),
        )
    }

    override fun observeWorld(): Flow<WorldState> =
        worldDao.observeSnapshot().map { snapshot ->
            snapshot?.let { serializer.decode(it.worldJson) } ?: bootstrap
        }

    override suspend fun currentWorld(): WorldState =
        worldDao.getSnapshot()?.let { serializer.decode(it.worldJson) } ?: ensureInitialized()

    override suspend fun persist(world: WorldState) {
        worldDao.upsert(
            WorldSnapshotEntity(
                worldJson = serializer.encode(world),
                schemaVersion = world.schemaVersion,
                updatedAtEpochMillis = System.currentTimeMillis(),
            ),
        )
    }

    override suspend fun ensureInitialized(): WorldState {
        worldDao.getSnapshot()?.let { return serializer.decode(it.worldJson) }
        persist(bootstrap)
        return bootstrap
    }
}
