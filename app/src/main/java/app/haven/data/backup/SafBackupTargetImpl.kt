package app.haven.data.backup

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import app.haven.data.model.BackupTargetType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Writes encrypted archives to a user-chosen document tree via the Storage
 * Access Framework — a local folder, SD card, or any DocumentsProvider the
 * user selected. Fully offline; the user owns the destination.
 */
class SafBackupTargetImpl(
    private val context: Context,
    override val treeUri: Uri,
) : SafBackupTarget {

    override val type: BackupTargetType = BackupTargetType.LOCAL_SAF

    private fun tree(): DocumentFile =
        DocumentFile.fromTreeUri(context, treeUri)
            ?: error("SAF tree Uri is no longer accessible: $treeUri")

    override suspend fun upload(archive: BackupArchive): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                val dir = tree()
                dir.findFile(archive.fileName)?.delete()
                val doc = dir.createFile(MIME_HAVEN, archive.fileName)
                    ?: error("Could not create SAF document ${archive.fileName}")
                val out = context.contentResolver.openOutputStream(doc.uri)
                    ?: error("Could not open SAF output stream")
                out.use { stream ->
                    File(archive.localPath).inputStream().use { it.copyTo(stream) }
                }
                Unit
            }
        }

    override suspend fun list(): Result<List<String>> =
        withContext(Dispatchers.IO) {
            runCatching {
                tree().listFiles()
                    .mapNotNull { it.name }
                    .filter { it.endsWith(".hvn") }
                    .sortedDescending()
            }
        }

    override suspend fun download(fileName: String): Result<ByteArray> =
        withContext(Dispatchers.IO) {
            runCatching {
                val doc = tree().findFile(fileName)
                    ?: error("Archive not found in SAF tree: $fileName")
                context.contentResolver.openInputStream(doc.uri)?.use { it.readBytes() }
                    ?: error("Could not open SAF input stream")
            }
        }

    private companion object {
        const val MIME_HAVEN = "application/octet-stream"
    }
}
