package br.edu.utfpr.roadifylogger.data.repository

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import br.edu.utfpr.roadifylogger.data.model.RecordingSession
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Manages the "sessions" folder on disk: one sub-folder per recording, holding its CSV log. */
class SessionFileRepository(private val context: Context) {

    private val sessionsRoot: File
        get() = File(context.getExternalFilesDir(null), "sessions").apply { mkdirs() }

    suspend fun listSessions(): List<RecordingSession> = withContext(Dispatchers.IO) {
        sessionsRoot.listFiles { file -> file.isDirectory }
            ?.sortedByDescending { it.lastModified() }
            ?.mapNotNull { dir -> toSession(dir) }
            ?: emptyList()
    }

    private fun toSession(dir: File): RecordingSession? {
        val csv = dir.listFiles { f -> f.extension == "csv" }?.firstOrNull() ?: return null
        val sizeBytes = dir.walkTopDown().filter { it.isFile }.sumOf { it.length() }
        return RecordingSession(
            id = dir.name,
            startedAtMillis = dir.lastModified(),
            locationLabel = null,
            sizeBytes = sizeBytes,
            csvFilePath = csv.absolutePath,
        )
    }

    suspend fun delete(session: RecordingSession) = withContext(Dispatchers.IO) {
        File(sessionsRoot, session.id).deleteRecursively()
    }

    suspend fun deleteAll() = withContext(Dispatchers.IO) {
        sessionsRoot.listFiles()?.forEach { it.deleteRecursively() }
    }

    fun shareIntent(session: RecordingSession): Intent {
        val file = File(session.csvFilePath)
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        return Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }
}
