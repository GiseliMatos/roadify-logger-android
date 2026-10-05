package br.edu.utfpr.roadifylogger.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Compress
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.GpsOff
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import br.edu.utfpr.roadifylogger.R
import br.edu.utfpr.roadifylogger.data.model.ColetaSummary
import br.edu.utfpr.roadifylogger.data.model.ConfiguracaoEntity
import br.edu.utfpr.roadifylogger.data.model.Posicao
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
                title = { Text(stringResource(id = R.string.summary_title)) },
                navigationIcon = {
                    IconButton(onClick = onVoltarClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(id = R.string.action_back))
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
                estadoUi.error != null -> {
                    Text(
                        text = estadoUi.error!!,
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
                            contentDescription = stringResource(id = R.string.summary_rename_file),
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
                        text = stringResource(id = if (resumo.temDadosGps) R.string.summary_location_enabled else R.string.summary_location_disabled),
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
                titulo = stringResource(id = R.string.summary_duration),
                valor = formatarDuracao(resumo.duracaoSegundos),
                icone = Icons.Filled.Timer,
                modifier = Modifier.weight(1f)
            )
            CartaoKpi(
                titulo = stringResource(id = R.string.summary_start_time),
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
                    Text(stringResource(id = R.string.summary_geo_title), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(12.dp))

                if (resumo.temDadosGps) {
                    val notRecorded = stringResource(id = R.string.summary_not_recorded)
                    val textoPontoInicial = if (resumo.latitudeInicio != null && resumo.longitudeInicio != null) {
                        "%.5f, %.5f".format(Locale.US, resumo.latitudeInicio, resumo.longitudeInicio)
                    } else {
                        notRecorded
                    }

                    val textoPontoFinal = if (resumo.latitudeFim != null && resumo.longitudeFim != null) {
                        "%.5f, %.5f".format(Locale.US, resumo.latitudeFim, resumo.longitudeFim)
                    } else {
                        notRecorded
                    }

                    LinhaDetalhe(stringResource(id = R.string.summary_direct_distance), "%.2f m".format(Locale.US, resumo.distanciaTotalMetros))
                    LinhaDetalhe(stringResource(id = R.string.summary_start_point), textoPontoInicial)
                    LinhaDetalhe(stringResource(id = R.string.summary_end_point), textoPontoFinal)
                } else {
                    Text(
                        text = stringResource(id = R.string.summary_no_gps),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Seção das Configurações Utilizadas na Coleta
        resumo.configuracao?.let { config ->
            SeacaoConfiguracoesUtilizadas(config = config)
        }
    }
}

@Composable
private fun SeacaoConfiguracoesUtilizadas(config: ConfiguracaoEntity) {
    val notInformed = stringResource(id = R.string.summary_not_informed)
    val notInformedFem = stringResource(id = R.string.summary_not_informed_f)

    // Dispositivo e Veículo
    CartaoSecaoResumo(
        titulo = stringResource(id = R.string.summary_device_vehicle),
        icone = Icons.Default.DirectionsCar
    ) {
        val posicaoDesc = Posicao.entries.find { it.name == config.posicaoTelefone }?.descricao ?: config.posicaoTelefone

        LinhaDetalhe(stringResource(id = R.string.summary_smartphone), "${config.marcaSmartphone} ${config.modeloSmartphone}".trim().ifEmpty { notInformed })
        LinhaDetalhe(stringResource(id = R.string.summary_mount_position), posicaoDesc.ifEmpty { notInformedFem })
        LinhaDetalhe(stringResource(id = R.string.summary_vehicle), "${config.marcaVeiculo} ${config.modeloVeiculo}".trim().ifEmpty { notInformed })
        LinhaDetalhe(stringResource(id = R.string.summary_mileage), if (config.quilometragemVeiculo > 0) "%.1f km".format(Locale.US, config.quilometragemVeiculo) else notInformedFem)
    }

    // Parâmetros de Amostragem
    CartaoSecaoResumo(
        titulo = stringResource(id = R.string.summary_sampling),
        icone = Icons.Default.Speed
    ) {
        val defaultRate = stringResource(id = R.string.summary_default)
        LinhaDetalhe(stringResource(id = R.string.summary_gps_rate), if (config.taxaGpsMs > 0) "${config.taxaGpsMs} ms" else defaultRate)
        LinhaDetalhe(stringResource(id = R.string.summary_sensor_rate), if (config.taxaSensoresHz > 0) "${config.taxaSensoresHz} Hz" else defaultRate)
    }

    // Status dos Sensores
    CartaoSecaoResumo(
        titulo = stringResource(id = R.string.summary_sensors_title),
        icone = Icons.Default.Settings
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            ItemSensorResumo(
                icone = Icons.Default.Vibration,
                nome = stringResource(id = R.string.sensor_title_accelerometer),
                ativo = config.acelerometro
            )
            ItemSensorResumo(
                icone = Icons.Default.ScreenRotation,
                nome = stringResource(id = R.string.sensor_title_gyroscope),
                ativo = config.giroscopio
            )
            ItemSensorResumo(
                icone = Icons.Default.LocationOn,
                nome = stringResource(id = R.string.summary_gps),
                ativo = config.gps
            )
            ItemSensorResumo(
                icone = Icons.Default.Compress,
                nome = stringResource(id = R.string.sensor_title_barometer),
                ativo = config.barometro
            )
            ItemSensorResumo(
                icone = Icons.Default.Videocam,
                nome = stringResource(id = R.string.sensor_camera_title),
                ativo = config.camera
            )
            ItemSensorResumo(
                icone = Icons.Default.Mic,
                nome = stringResource(id = R.string.sensor_microphone_title),
                ativo = config.microfone
            )
            ItemSensorResumo(
                icone = Icons.Default.Thermostat,
                nome = stringResource(id = R.string.summary_battery_temp),
                ativo = config.temperaturaBateria
            )
            ItemSensorResumo(
                icone = Icons.Default.BatteryFull,
                nome = stringResource(id = R.string.settings_battery_level),
                ativo = config.nivelBateria
            )
        }
    }
}

@Composable
private fun CartaoSecaoResumo(
    titulo: String,
    icone: ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 4.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icone,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Spacer(Modifier.width(12.dp))
                Text(
                    text = titulo,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }
            content()
        }
    }
}

@Composable
private fun ItemSensorResumo(
    icone: ImageVector,
    nome: String,
    ativo: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = icone,
                contentDescription = null,
                tint = if (ativo) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text = nome,
                style = MaterialTheme.typography.bodyMedium,
                color = if (ativo) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = if (ativo) Icons.Default.CheckCircle else Icons.Default.Cancel,
                contentDescription = null,
                tint = if (ativo) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = stringResource(id = if (ativo) R.string.summary_active else R.string.summary_inactive),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                color = if (ativo) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.7f)
            )
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
            .padding(vertical = 2.dp),
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