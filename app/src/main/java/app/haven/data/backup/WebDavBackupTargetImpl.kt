package app.haven.data.backup

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Credentials
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

/**
 * WebDAV transport against the user's OWN self-hosted server. This is the only
 * networked backup path and it never contacts a third-party cloud vendor.
 *
 * Uses plain OkHttp verbs (MKCOL / PUT / GET / PROPFIND) with Basic auth over
 * the user-configured HTTPS endpoint.
 */
class WebDavBackupTargetImpl(
    private val config: WebDavConfig,
    private val client: OkHttpClient,
) : WebDavBackupTarget {

    private val credentials = Credentials.basic(config.username, config.password)

    private fun dirUrl() = buildString {
        append(config.baseUrl.trimEnd('/'))
        append('/')
        append(config.remoteDirectory.trim('/'))
        append('/')
    }

    override suspend fun upload(archive: BackupArchive): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                ensureDirectory()
                val body = File(archive.localPath).readBytes()
                    .toRequestBody(OCTET_STREAM)
                val request = Request.Builder()
                    .url(dirUrl() + archive.fileName)
                    .header("Authorization", credentials)
                    .put(body)
                    .build()
                client.newCall(request).execute().use { response ->
                    check(response.isSuccessful) { "WebDAV PUT failed: ${response.code}" }
                }
            }
        }

    override suspend fun list(): Result<List<String>> =
        withContext(Dispatchers.IO) {
            runCatching {
                val request = Request.Builder()
                    .url(dirUrl())
                    .header("Authorization", credentials)
                    .header("Depth", "1")
                    .method("PROPFIND", ByteArray(0).toRequestBody(XML))
                    .build()
                val xml = client.newCall(request).execute().use { response ->
                    check(response.isSuccessful) { "WebDAV PROPFIND failed: ${response.code}" }
                    response.body?.string().orEmpty()
                }
                HREF_REGEX.findAll(xml)
                    .map { it.groupValues[1].substringAfterLast('/') }
                    .filter { it.endsWith(".hvn") }
                    .distinct()
                    .sortedDescending()
                    .toList()
            }
        }

    override suspend fun download(fileName: String): Result<ByteArray> =
        withContext(Dispatchers.IO) {
            runCatching {
                val request = Request.Builder()
                    .url(dirUrl() + fileName)
                    .header("Authorization", credentials)
                    .get()
                    .build()
                client.newCall(request).execute().use { response ->
                    check(response.isSuccessful) { "WebDAV GET failed: ${response.code}" }
                    response.body?.bytes() ?: error("Empty WebDAV response body")
                }
            }
        }

    /** MKCOL the backup directory; a 405 means it already exists — that's fine. */
    private fun ensureDirectory() {
        val request = Request.Builder()
            .url(dirUrl())
            .header("Authorization", credentials)
            .method("MKCOL", null)
            .build()
        client.newCall(request).execute().use { /* ignore 201 or 405 */ }
    }

    private companion object {
        val OCTET_STREAM = "application/octet-stream".toMediaType()
        val XML = "application/xml".toMediaType()
        // Extract each <d:href>…</d:href> regardless of namespace prefix.
        val HREF_REGEX = Regex("<[a-zA-Z]*:?href>([^<]+)</[a-zA-Z]*:?href>")
    }

    // baseUrl is validated eagerly so a malformed endpoint fails at config time.
    init {
        config.baseUrl.toHttpUrl()
    }
}
