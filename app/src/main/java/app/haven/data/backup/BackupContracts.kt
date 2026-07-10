package app.haven.data.backup

import android.net.Uri
import app.haven.data.model.BackupTargetType

/**
 * An immutable descriptor of a produced encrypted archive. Returned by the
 * serialization manager and handed to a [BackupTarget] for transfer.
 */
data class BackupArchive(
    val fileName: String,
    /** Local staging location of the encrypted archive on disk. */
    val localPath: String,
    val sizeBytes: Long,
    /** SHA-256 of the ciphertext, for integrity verification on restore. */
    val checksum: String,
    val createdAtEpochMillis: Long,
)

/** The bundle handed to the serialization manager to pack into one archive. */
data class BackupSource(
    /** Absolute path to the Room SQLite file (plus -wal/-shm siblings). */
    val databaseFile: String,
    /** The serialized world JSON at the moment of backup. */
    val worldJson: String,
)

/** Result of a restore attempt, so the world can be rehydrated transactionally. */
data class RestoredBundle(
    val worldJson: String,
    val databaseBytes: ByteArray,
) {
    // ByteArray needs structural equality overrides to behave as a value type.
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is RestoredBundle) return false
        return worldJson == other.worldJson && databaseBytes.contentEquals(other.databaseBytes)
    }

    override fun hashCode(): Int = 31 * worldJson.hashCode() + databaseBytes.contentHashCode()
}

/**
 * A destination an encrypted archive can be written to and read back from.
 * Implementations are the ONLY components that touch a transport; the rest of
 * the pipeline is transport-agnostic. Both concrete targets are local/self-
 * hosted — no third-party cloud SDK is involved.
 */
interface BackupTarget {
    val type: BackupTargetType

    /** Push the archive to this destination. */
    suspend fun upload(archive: BackupArchive): Result<Unit>

    /** List available archive file names, newest first. */
    suspend fun list(): Result<List<String>>

    /** Pull an archive's ciphertext by file name for restore. */
    suspend fun download(fileName: String): Result<ByteArray>
}

/** SAF (Storage Access Framework) document-tree target for local backups. */
interface SafBackupTarget : BackupTarget {
    override val type: BackupTargetType get() = BackupTargetType.LOCAL_SAF

    /** The user-picked persistable document-tree Uri backups are written under. */
    val treeUri: Uri
}

/** Personal self-hosted WebDAV directory target. */
interface WebDavBackupTarget : BackupTarget {
    override val type: BackupTargetType get() = BackupTargetType.WEBDAV
}

/** User-supplied WebDAV connection settings (stored via DataStore, not here). */
data class WebDavConfig(
    val baseUrl: String,
    val username: String,
    val password: String,
    /** Sub-directory under [baseUrl] where Haven writes archives. */
    val remoteDirectory: String = "haven",
)

/**
 * The local serialization manager (Section 5.3). Zips the Room SQLite file and
 * the serialized world JSON, then encrypts the result into one archive — and
 * the inverse for restore. Concrete transport is delegated to [BackupTarget].
 */
interface LocalSerializationManager {
    /** Serialize + zip + encrypt into a staged [BackupArchive]. */
    suspend fun pack(source: BackupSource): Result<BackupArchive>

    /** Decrypt + unzip a downloaded archive back into its parts. */
    suspend fun unpack(ciphertext: ByteArray): Result<RestoredBundle>
}
