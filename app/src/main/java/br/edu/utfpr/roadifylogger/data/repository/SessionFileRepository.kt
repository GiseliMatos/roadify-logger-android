package br.edu.utfpr.roadifylogger.data.repository

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import br.edu.utfpr.roadifylogger.R
import br.edu.utfpr.roadifylogger.data.database.DatabaseInstance
import br.edu.utfpr.roadifylogger.data.model.ColetaEntity
import br.edu.utfpr.roadifylogger.data.model.ColetaSummary
import br.edu.utfpr.roadifylogger.data.model.RecordingSession
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import android.content.ContentUris
import android.content.ContentValues
import android.os.Build
import android.os.Environment
import android.provider.MediaStore

/** Manages the "sessions" folder on disk: one sub-folder per recording, holding its CSV log. */
class SessionFileRepository(
    private val context: Context,
    database: DatabaseInstance.AppDatabase,
) {

    private val coletaDao = database.coletaDao()
    private val configuracaoDao = database.configuracaoDao()

    private val sessionsRoot: File
        get() = File(context.getExternalFilesDir(null), "sessions").apply { mkdirs() }

    suspend fun listSessions(): List<RecordingSession> = withContext(Dispatchers.IO) {
        coletaDao.listarTodas()
            .sortedByDescending { it.dataHoraInicio }
            .mapNotNull { coleta -> toSession(coleta) }
    }

    suspend fun getColetaSummaryFromDb(databaseId: Long): ColetaSummary = withContext(Dispatchers.IO) {
        val coleta = coletaDao.buscarPorId(databaseId)
            ?: throw IllegalArgumentException(context.getString(R.string.error_session_not_found, databaseId))

        val configuracao = configuracaoDao.buscarPorId(coleta.configuracaoId)

        val inicioTs = coleta.dataHoraInicio
        val fimTs = coleta.dataHoraFim ?: inicioTs
        val duracaoSegundos = if (fimTs > inicioTs) (fimTs - inicioTs) / 1000.0 else 0.0

        val temGpsInicio = verificaCoordenada(coleta.latitudeInicio, coleta.longitudeInicio)
        val temGpsFim = verificaCoordenada(coleta.latitudeFim, coleta.longitudeFim)
        val temGpsValido = temGpsInicio

        val distanciaTotal = if (temGpsInicio && temGpsFim) {
            calcularDistanciaHaversine(
                lat1 = coleta.latitudeInicio!!,
                lon1 = coleta.longitudeInicio!!,
                lat2 = coleta.latitudeFim!!,
                lon2 = coleta.longitudeFim!!
            )
        } else 0.0

        val pastaColeta = File(coleta.caminhoPastaGravacao)
        val arquivoCsv = File(pastaColeta, coleta.nomeArquivoColeta)

        ColetaSummary(
            arquivo = arquivoCsv,
            nomeArquivoColeta = coleta.nomeArquivoColeta,
            caminhoPastaGravacao = coleta.caminhoPastaGravacao,
            dataHoraInicio = inicioTs,
            dataHoraFim = fimTs,
            latitudeInicio = coleta.latitudeInicio,
            longitudeInicio = coleta.longitudeInicio,
            latitudeFim = coleta.latitudeFim,
            longitudeFim = coleta.longitudeFim,
            duracaoSegundos = duracaoSegundos,
            temDadosGps = temGpsValido,
            distanciaTotalMetros = distanciaTotal,
            configuracao = configuracao
        )
    }

    private fun verificaCoordenada(lat: Double?, lon: Double?): Boolean {
        return lat != null && lon != null && (lat != 0.0 || lon != 0.0)
    }

    private fun toSession(coleta: ColetaEntity): RecordingSession? {
        val dir = File(coleta.caminhoPastaGravacao)
        val csv = File(dir, coleta.nomeArquivoColeta)
        if (!csv.isFile) return null
        val sizeBytes = dir.walkTopDown().filter { it.isFile }.sumOf { it.length() }
        return RecordingSession(
            databaseId = coleta.id,
            id = dir.name,
            fileName = csv.name,
            startedAtMillis = coleta.dataHoraInicio,
            locationLabel = LocationNameResolver.resolve(
                context = context,
                latitude = coleta.latitudeInicio,
                longitude = coleta.longitudeInicio,
            ),
            sizeBytes = sizeBytes,
            csvFilePath = csv.absolutePath,
        )
    }

    suspend fun delete(session: RecordingSession) = withContext(Dispatchers.IO) {
        File(sessionsRoot, session.id).deleteRecursively()
        coletaDao.excluirPorId(session.databaseId)
    }

    suspend fun deleteAll() = withContext(Dispatchers.IO) {
        sessionsRoot.listFiles()?.forEach { it.deleteRecursively() }
        coletaDao.listarTodas().forEach { coletaDao.excluirPorId(it.id) }
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

    private fun calcularDistanciaHaversine(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val raioTerraMetros = 6371000.0
        val deltaLat = Math.toRadians(lat2 - lat1)
        val deltaLon = Math.toRadians(lon2 - lon1)
        val a = sin(deltaLat / 2).pow(2.0) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(deltaLon / 2).pow(2.0)
        return raioTerraMetros * 2 * atan2(sqrt(a), sqrt(1 - a))
    }

    suspend fun renameSession(databaseId: Long, newName: String) = withContext(Dispatchers.IO) {
        val coleta = coletaDao.buscarPorId(databaseId) ?: return@withContext

        val nomeSanitizado = sanitizarNomeArquivo(newName)
        val pastaColeta = File(coleta.caminhoPastaGravacao)
        val arquivoAntigo = File(pastaColeta, coleta.nomeArquivoColeta)
        val arquivoNovo = File(pastaColeta, nomeSanitizado)

        if (arquivoAntigo.name == arquivoNovo.name) return@withContext

        if (arquivoNovo.exists()) {
            throw IllegalArgumentException(context.getString(R.string.error_file_exists))
        }

        if (arquivoAntigo.exists()) {
            val renomeadoComSucesso = arquivoAntigo.renameTo(arquivoNovo)
            if (!renomeadoComSucesso) {
                throw IllegalStateException(context.getString(R.string.error_rename_failed))
            }
        }

        coletaDao.atualizar(coleta.copy(nomeArquivoColeta = nomeSanitizado))
    }

    private fun sanitizarNomeArquivo(nomeBruto: String): String {
        var limpo = nomeBruto.trim()

        if (limpo.endsWith(".csv", ignoreCase = true)) {
            limpo = limpo.dropLast(4)
        }

        limpo = limpo.replace(Regex("[\\\\/:*?\"<>|\\x00-\\x1F]"), "_")

        if (limpo.isBlank() || limpo.all { it == '.' }) {
            limpo = context.getString(R.string.default_session_name)
        }

        if (limpo.length > 100) {
            limpo = limpo.take(100).trimEnd()
        }

        return "$limpo.csv"
    }


    fun exportSession(
        session: RecordingSession,
        destinationTreeUri: Uri
    ) {
        val sourceFolder = File(session.csvFilePath).parentFile
            ?: throw IllegalStateException("Pasta da coleta não encontrada.")

        require(sourceFolder.isDirectory) {
            "A pasta original da coleta não existe."
        }

        val destinationRoot =
            DocumentFile.fromTreeUri(context, destinationTreeUri)
                ?: throw IllegalStateException("Pasta de destino inválida.")



        val roadifyFolder =
            if (destinationRoot.name.equals("RoadifyLogger", ignoreCase = true)) {
                destinationRoot
            } else {
                destinationRoot.findFile("RoadifyLogger")
                    ?: destinationRoot.createDirectory("RoadifyLogger")
                    ?: throw IllegalStateException(
                        "Não foi possível criar a pasta RoadifyLogger."
                    )
            }


        val sessionFolder =
            roadifyFolder.findFile(sourceFolder.name)
                ?: roadifyFolder.createDirectory(sourceFolder.name)
                ?: throw IllegalStateException(
                    "Não foi possível criar a pasta da coleta."
                )

        require(sessionFolder.isDirectory) {
            "O destino da coleta não é uma pasta."
        }

        fun copyFolder(source: File, destination: DocumentFile) {
            source.listFiles()?.forEach { item ->
                if (item.isDirectory) {
                    val childFolder = destination.findFile(item.name)
                        ?: destination.createDirectory(item.name)
                        ?: error("Erro ao criar ${item.name}")

                    require(childFolder.isDirectory)

                    copyFolder(item, childFolder)
                } else if (item.isFile) {
                    destination.findFile(item.name)?.let { existing ->
                        if (!existing.delete()) {
                            error("Não foi possível substituir ${item.name}")
                        }
                    }

                    val newFile = destination.createFile(
                        getMimeType(item),
                        item.name
                    ) ?: error("Erro ao criar ${item.name}")

                    val output = context.contentResolver
                        .openOutputStream(newFile.uri)
                        ?: error("Erro ao gravar ${item.name}")

                    output.use { stream ->
                        item.inputStream().use { input ->
                            input.copyTo(stream)
                        }
                    }
                }
            }
        }

        copyFolder(sourceFolder, sessionFolder)
    }


    fun exportSessionToDownloads(session: RecordingSession) {
        require(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)

        val sourceFolder = File(session.csvFilePath).parentFile
            ?: error("Pasta original não encontrada.")

        require(sourceFolder.isDirectory) {
            "A pasta original não existe."
        }

        val files = sourceFolder.walkTopDown()
            .filter { it.isFile }
            .toList()

        require(files.isNotEmpty()) {
            "A coleta não possui arquivos para exportar."
        }

        val resolver = context.contentResolver
        val collection = MediaStore.Downloads.getContentUri(
            MediaStore.VOLUME_EXTERNAL_PRIMARY
        )

        files.forEach { file ->
            val subfolder = file.parentFile!!
                .relativeTo(sourceFolder)
                .path.replace(File.separatorChar, '/')

            val relativePath = buildString {
                append(Environment.DIRECTORY_DOWNLOADS)
                append("/RoadifyLogger/")
                append(sourceFolder.name)
                append("/")
                if (subfolder.isNotEmpty()) {
                    append(subfolder)
                    append("/")
                }
            }

            // Procura uma cópia já exportada pelo app.
            val existingUri = resolver.query(
                collection,
                arrayOf(MediaStore.Downloads._ID),
                "${MediaStore.MediaColumns.DISPLAY_NAME} = ? AND " +
                        "${MediaStore.MediaColumns.RELATIVE_PATH} = ?",
                arrayOf(file.name, relativePath),
                null
            )?.use { cursor ->
                if (cursor.moveToFirst()) {
                    ContentUris.withAppendedId(
                        collection,
                        cursor.getLong(0)
                    )
                } else null
            }

            val values = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, file.name)
                put(MediaStore.MediaColumns.MIME_TYPE, getMimeType(file))
                put(MediaStore.MediaColumns.RELATIVE_PATH, relativePath)
                if (existingUri == null) {
                    put(MediaStore.MediaColumns.IS_PENDING, 1)
                }
            }

            val uri = existingUri
                ?: resolver.insert(collection, values)
                ?: error("Não foi possível criar ${file.name}")

            try {
                val output = resolver.openOutputStream(uri, "wt")
                    ?: error("Não foi possível gravar ${file.name}")

                output.use {
                    file.inputStream().use { input ->
                        input.copyTo(it)
                    }
                }

                if (existingUri == null) {
                    resolver.update(
                        uri,
                        ContentValues().apply {
                            put(MediaStore.MediaColumns.IS_PENDING, 0)
                        },
                        null,
                        null
                    )
                }
            } catch (e: Exception) {
                if (existingUri == null) {
                    resolver.delete(uri, null, null)
                }
                throw e
            }
        }
    }

    private fun getMimeType(file: File): String {
        return when (file.extension.lowercase()) {
            "csv" -> "text/csv"
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            "mp4" -> "video/mp4"
            "wav" -> "audio/wav"
            "mp3" -> "audio/mpeg"
            else -> "application/octet-stream"
        }
    }
}
