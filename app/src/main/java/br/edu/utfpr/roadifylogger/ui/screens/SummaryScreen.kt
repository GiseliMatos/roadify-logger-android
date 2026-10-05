package br.edu.utfpr.roadifylogger.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.GpsOff
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import br.edu.utfpr.roadifylogger.data.model.ColetaSummary
import br.edu.utfpr.roadifylogger.ui.components.RenameSessionDialog
import br.edu.utfpr.roadifylogger.ui.viewmodel.SummaryViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SummaryScreen(
    viewModel: SummaryViewModel,
    onVoltarClick: () -> Unit
) {
    val estadoUi by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var exibindoDialogoRenomear by remember { mutableStateOf(false) }

    // Efeito para erros pontuais de ações (ex: renomear)
    LaunchedEffect(estadoUi.error) {
        estadoUi.error?.let { mensagem ->
            snackbarHostState.showSnackbar(
                message = mensagem,
                withDismissAction = true
            )
            viewModel.clearErrorMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Resumo da Coleta") },
                navigationIcon = {
                    IconButton(onClick = onVoltarClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { espacamentoEntrada ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(espacamentoEntrada)
        ) {
            when {
                estadoUi.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                estadoUi.fatalError != null -> {
                    Text(
                        text = estadoUi.fatalError!!,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                estadoUi.summary != null -> {
                    ConteudoResumo(
                        resumo = estadoUi.summary!!,
                        onRenameClick = { exibindoDialogoRenomear = true }
                    )
                }
            }
        }
    }

    if (exibindoDialogoRenomear) {
        RenameSessionDialog(
            initialName = estadoUi.summary?.nomeArquivoColeta ?: "",
            onDismissRequest = { exibindoDialogoRenomear = false },
            onConfirm = { novoNome ->
                viewModel.rename(novoNome)
            }
        )
    }
}

@Composable
private fun ConteudoResumo(
    resumo: ColetaSummary,
    onRenameClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Cabeçalho do arquivo
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = resumo.nomeArquivoColeta,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onRenameClick) {
                        Icon(
                            imageVector = Icons.Filled.EditNote,
                            contentDescription = "Renomear arquivo",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (resumo.temDadosGps) Icons.Filled.GpsFixed else Icons.Filled.GpsOff,
                        contentDescription = null,
                        tint = if (resumo.temDadosGps) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = if (resumo.temDadosGps) "Localização Ativada" else "Localização Desativada",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (resumo.temDadosGps) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    )
                }
            }
        }

        // Duração e Hora de Início
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CartaoKpi(
                titulo = "Duração da Gravação",
                valor = formatarDuracao(resumo.duracaoSegundos),
                icone = Icons.Filled.Timer,
                modifier = Modifier.weight(1f)
            )
            CartaoKpi(
                titulo = "Início da Gravação",
                valor = formatarHora(resumo.dataHoraInicio),
                icone = Icons.Filled.Schedule,
                modifier = Modifier.weight(1f)
            )
        }

        // Detalhes Geográficos e Trajeto
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Map, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(8.dp))
                    Text("Dados Geográficos e Trajeto", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(12.dp))

                if (resumo.temDadosGps) {
                    LinhaDetalhe("Distância Direta (Linha Reta)", "%.2f m".format(Locale.US, resumo.distanciaTotalMetros))
                    LinhaDetalhe("Velocidade Média Estimada", "%.1f km/h".format(Locale.US, resumo.velocidadeMediaKmH))
                    LinhaDetalhe("Ponto Inicial", "%.5f, %.5f".format(Locale.US, resumo.latitudeInicio, resumo.longitudeInicio))
                    LinhaDetalhe("Ponto Final", "%.5f, %.5f".format(Locale.US, resumo.latitudeFim, resumo.longitudeFim))
                } else {
                    Text(
                        text = "Sem registros de GPS para esta coleta.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun CartaoKpi(
    titulo: String,
    valor: String,
    icone: ImageVector,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier, shape = RoundedCornerShape(12.dp)) {
        Column(modifier = Modifier.padding(12.dp)) {
            Icon(icone, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            Spacer(Modifier.height(4.dp))
            Text(titulo, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(valor, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun LinhaDetalhe(rotulo: String, valor: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(rotulo, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(valor, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
    }
}

private fun formatarDuracao(segundos: Double): String {
    val totalSegundos = segundos.toInt()
    val minutos = totalSegundos / 60
    val segs = totalSegundos % 60
    return "%02d:%02d".format(Locale.US, minutos, segs)
}

private fun formatarHora(timestampMs: Long?): String {
    if (timestampMs == null || timestampMs == 0L) return "--:--"
    val date = Date(timestampMs)
    val formatter = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    return formatter.format(date)
}
