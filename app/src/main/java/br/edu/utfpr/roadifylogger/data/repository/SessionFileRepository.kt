package br.edu.utfpr.roadifylogger.data.repository

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
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

/** Manages the "sessions" folder on disk: one sub-folder per recording, holding its CSV log. */
class SessionFileRepository(
    private val context: Context,
    database: DatabaseInstance.AppDatabase,
) {

    private val coletaDao = database.coletaDao()

    private val sessionsRoot: File
        get() = File(context.getExternalFilesDir(null), "sessions").apply { mkdirs() }

    suspend fun listSessions(): List<RecordingSession> = withContext(Dispatchers.IO) {
        coletaDao.listarTodas()
            .sortedByDescending { it.dataHoraInicio }
            .mapNotNull { coleta -> toSession(coleta) }
    }

    suspend fun getColetaSummaryFromDb(databaseId: Long): ColetaSummary = withContext(Dispatchers.IO) {
        val coleta = coletaDao.buscarPorId(databaseId)
            ?: throw IllegalArgumentException("Registro de coleta não encontrado no banco de dados ID: $databaseId")

        val inicioTs = coleta.dataHoraInicio
        val fimTs = coleta.dataHoraFim ?: inicioTs
        val duracaoSegundos = if (fimTs > inicioTs) (fimTs - inicioTs) / 1000.0 else 0.0

        val temGpsValido = coleta.latitudeInicio != null && coleta.longitudeInicio != null &&
                coleta.latitudeFim != null && coleta.longitudeFim != null

        val distanciaTotal = if (temGpsValido) {
            calcularDistanciaHaversine(
                lat1 = coleta.latitudeInicio!!,
                lon1 = coleta.longitudeInicio!!,
                lat2 = coleta.latitudeFim!!,
                lon2 = coleta.longitudeFim!!
            )
        } else 0.0

        val velocidadeMediaKmH = if (duracaoSegundos > 0 && distanciaTotal > 0) {
            (distanciaTotal / duracaoSegundos) * 3.6
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
            velocidadeMediaKmH = velocidadeMediaKmH
        )
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
            throw IllegalArgumentException("Já existe um arquivo com esse nome nesta pasta.")
        }

        if (arquivoAntigo.exists()) {
            val renomeadoComSucesso = arquivoAntigo.renameTo(arquivoNovo)
            if (!renomeadoComSucesso) {
                throw IllegalStateException("Não foi possível renomear o arquivo no sistema de arquivos.")
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
            limpo = "coleta_sem_nome"
        }

        if (limpo.length > 100) {
            limpo = limpo.take(100).trimEnd()
        }

        return "$limpo.csv"
    }
}
