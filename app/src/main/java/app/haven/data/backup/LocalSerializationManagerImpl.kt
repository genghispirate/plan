package app.haven.data.backup

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.security.MessageDigest
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

/**
 * Default [LocalSerializationManager]: bundles the Room SQLite file and the
 * serialized world JSON into a single zip, encrypts it with [BackupCrypto], and
 * stages the ciphertext under the app's private `backups/` directory.
 *
 * All disk/crypto work is dispatched to IO. Failures are returned as
 * [Result.failure] rather than thrown so the pipeline can record a FAILED run.
 */
class LocalSerializationManagerImpl(
    private val context: Context,
    private val crypto: BackupCrypto,
) : LocalSerializationManager {

    private val stagingDir: File
        get() = File(context.filesDir, "backups").apply { mkdirs() }

    override suspend fun pack(source: BackupSource): Result<BackupArchive> =
        withContext(Dispatchers.IO) {
            runCatching {
                val zipped = zip(source)
                val ciphertext = crypto.encrypt(zipped)

                val now = System.currentTimeMillis()
                val fileName = "haven-$now.hvn"
                val outFile = File(stagingDir, fileName)
                outFile.writeBytes(ciphertext)

                BackupArchive(
                    fileName = fileName,
                    localPath = outFile.absolutePath,
                    sizeBytes = outFile.length(),
                    checksum = sha256(ciphertext),
                    createdAtEpochMillis = now,
                )
            }
        }

    override suspend fun unpack(ciphertext: ByteArray): Result<RestoredBundle> =
        withContext(Dispatchers.IO) {
            runCatching {
                val zipped = crypto.decrypt(ciphertext)
                var worldJson: String? = null
                var dbBytes: ByteArray? = null

                ZipInputStream(zipped.inputStream()).use { zis ->
                    var entry: ZipEntry? = zis.nextEntry
                    while (entry != null) {
                        when (entry.name) {
                            ENTRY_WORLD -> worldJson = zis.readBytes().decodeToString()
                            ENTRY_DATABASE -> dbBytes = zis.readBytes()
                        }
                        zis.closeEntry()
                        entry = zis.nextEntry
                    }
                }

                RestoredBundle(
                    worldJson = requireNotNull(worldJson) { "Archive missing $ENTRY_WORLD" },
                    databaseBytes = requireNotNull(dbBytes) { "Archive missing $ENTRY_DATABASE" },
                )
            }
        }

    private fun zip(source: BackupSource): ByteArray {
        val buffer = ByteArrayOutputStream()
        ZipOutputStream(buffer).use { zos ->
            zos.putNextEntry(ZipEntry(ENTRY_WORLD))
            zos.write(source.worldJson.encodeToByteArray())
            zos.closeEntry()

            val dbFile = File(source.databaseFile)
            if (dbFile.exists()) {
                zos.putNextEntry(ZipEntry(ENTRY_DATABASE))
                dbFile.inputStream().use { it.copyTo(zos) }
                zos.closeEntry()
            }
        }
        return buffer.toByteArray()
    }

    private fun sha256(bytes: ByteArray): String =
        MessageDigest.getInstance("SHA-256")
            .digest(bytes)
            .joinToString("") { "%02x".format(it) }

    private companion object {
        const val ENTRY_WORLD = "world.json"
        const val ENTRY_DATABASE = "haven.db"
    }
}
