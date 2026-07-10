package app.haven.data.repository.impl

import android.content.Context
import app.haven.data.backup.BackupArchive
import app.haven.data.backup.BackupSource
import app.haven.data.backup.BackupTarget
import app.haven.data.backup.LocalSerializationManager
import app.haven.data.local.HavenDatabase
import app.haven.data.local.dao.BackupDao
import app.haven.data.local.entity.BackupRecordEntity
import app.haven.data.model.BackupStatus
import app.haven.data.repository.BackupRepository
import app.haven.data.repository.WorldRepository
import app.haven.data.serialization.WorldSerializer
import kotlinx.coroutines.flow.Flow
import java.io.File

/**
 * Orchestrates a backup run: checkpoint the WAL, serialize the world, hand the
 * bundle to the [LocalSerializationManager] to zip+encrypt, then push the
 * archive through the caller-selected [BackupTarget]. Every stage transition is
 * recorded so the UI can show honest progress and failures.
 */
class BackupRepositoryImpl(
    private val context: Context,
    private val db: HavenDatabase,
    private val backupDao: BackupDao,
    private val worldRepository: WorldRepository,
    private val serializer: WorldSerializer,
    private val serializationManager: LocalSerializationManager,
) : BackupRepository {

    override fun observeHistory(): Flow<List<BackupRecordEntity>> = backupDao.observeRecent()

    override suspend fun backupNow(target: BackupTarget): Result<BackupArchive> = runCatching {
        val now = System.currentTimeMillis()
        val recordId = backupDao.insert(
            BackupRecordEntity(
                fileName = "(pending)",
                target = target.type,
                sizeBytes = 0,
                checksum = null,
                status = BackupStatus.QUEUED,
                createdAtEpochMillis = now,
            ),
        )

        try {
            // Flush WAL so the on-disk SQLite file is a consistent snapshot.
            checkpointWal()

            val worldJson = serializer.encode(worldRepository.currentWorld())
            val source = BackupSource(databaseFile = databasePath(), worldJson = worldJson)

            updateStatus(recordId, target, now, BackupStatus.SERIALIZING)
            val archive = serializationManager.pack(source).getOrThrow()

            updateStatus(recordId, target, now, BackupStatus.TRANSFERRING, archive)
            target.upload(archive).getOrThrow()

            backupDao.upsert(
                BackupRecordEntity(
                    id = recordId,
                    fileName = archive.fileName,
                    target = target.type,
                    sizeBytes = archive.sizeBytes,
                    checksum = archive.checksum,
                    status = BackupStatus.COMPLETE,
                    createdAtEpochMillis = now,
                    completedAtEpochMillis = System.currentTimeMillis(),
                ),
            )
            archive
        } catch (t: Throwable) {
            backupDao.upsert(
                BackupRecordEntity(
                    id = recordId,
                    fileName = "(failed)",
                    target = target.type,
                    sizeBytes = 0,
                    checksum = null,
                    status = BackupStatus.FAILED,
                    createdAtEpochMillis = now,
                    completedAtEpochMillis = System.currentTimeMillis(),
                    errorMessage = t.message,
                ),
            )
            throw t
        }
    }

    override suspend fun restore(target: BackupTarget, fileName: String): Result<Unit> =
        runCatching {
            val ciphertext = target.download(fileName).getOrThrow()
            val bundle = serializationManager.unpack(ciphertext).getOrThrow()

            // Immediate visual restore: rehydrate the world from the archive.
            worldRepository.persist(serializer.decode(bundle.worldJson))

            // Stage the SQLite payload for an atomic swap on next cold start; the
            // live Room handle can't be replaced underneath open connections.
            File(context.filesDir, PENDING_DB_RESTORE).writeBytes(bundle.databaseBytes)
        }

    private suspend fun updateStatus(
        id: Long,
        target: BackupTarget,
        createdAt: Long,
        status: BackupStatus,
        archive: BackupArchive? = null,
    ) {
        backupDao.upsert(
            BackupRecordEntity(
                id = id,
                fileName = archive?.fileName ?: "(pending)",
                target = target.type,
                sizeBytes = archive?.sizeBytes ?: 0,
                checksum = archive?.checksum,
                status = status,
                createdAtEpochMillis = createdAt,
            ),
        )
    }

    private fun databasePath(): String =
        context.getDatabasePath(HavenDatabase.DATABASE_NAME).absolutePath

    private fun checkpointWal() {
        db.openHelper.writableDatabase
            .query("PRAGMA wal_checkpoint(TRUNCATE)")
            .use { /* drain cursor */ it.moveToFirst() }
    }

    private companion object {
        const val PENDING_DB_RESTORE = "pending_restore.db"
    }
}
