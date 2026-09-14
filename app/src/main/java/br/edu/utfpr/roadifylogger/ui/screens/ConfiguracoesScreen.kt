package br.edu.utfpr.roadifylogger.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.Compress
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import br.edu.utfpr.roadifylogger.data.model.Posicao
import br.edu.utfpr.roadifylogger.ui.theme.RoadifyLoggerTheme
import br.edu.utfpr.roadifylogger.ui.viewmodel.ConfiguracoesEvent
import br.edu.utfpr.roadifylogger.ui.viewmodel.ConfiguracoesState
import br.edu.utfpr.roadifylogger.ui.viewmodel.ConfiguracoesViewModel

// Wrapper com estado
@Composable
fun ConfiguracoesScreen(
    onLevelClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ConfiguracoesViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    ConfiguracoesScreen(
        state = uiState,
        onEvent = viewModel::onEvent,
        onLevelClick = onLevelClick,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfiguracoesScreen(
    state: ConfiguracoesState,
    onEvent: (ConfiguracoesEvent) -> Unit,
    onLevelClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var dropdownExpanded by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.mensagemFeedback) {
        state.mensagemFeedback?.let { mensagem ->
            snackbarHostState.showSnackbar(mensagem)
            onEvent(ConfiguracoesEvent.MensagemExibida)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Configurações",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { paddingValues ->
        if (state.isLoading && state.marcaSmartphone.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Grupo 1: Dispositivo e Fixação
                ConfigSectionCard(
                    title = "Dispositivo e Fixação",
                    icon = Icons.Default.Smartphone
                ) {
                    StyledFormField(
                        label = "Marca do smartphone",
                        value = state.marcaSmartphone,
                        onValueChange = { onEvent(ConfiguracoesEvent.MarcaSmartphoneChanged(it)) }
                    )
                    StyledFormField(
                        label = "Modelo do smartphone",
                        value = state.modeloSmartphone,
                        onValueChange = { onEvent(ConfiguracoesEvent.ModeloSmartphoneChanged(it)) }
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Posição do telefone",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        ExposedDropdownMenuBox(
                            expanded = dropdownExpanded,
                            onExpandedChange = { dropdownExpanded = !dropdownExpanded }
                        ) {
                            val currentPosicao = Posicao.entries.find { it.name == state.posicaoTelefone }
                            val labelText = currentPosicao?.descricao ?: if (state.posicaoTelefone.isNotEmpty()) state.posicaoTelefone else "Retrato"

                            TextField(
                                value = labelText,
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded) },
                                modifier = Modifier
                                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                    .fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.3f),
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.2f),
                                    focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                                    unfocusedIndicatorColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                )
                            )

                            ExposedDropdownMenu(
                                expanded = dropdownExpanded,
                                onDismissRequest = { dropdownExpanded = false }
                            ) {
                                Posicao.entries.forEach { posicao ->
                                    DropdownMenuItem(
                                        text = { Text(posicao.descricao) },
                                        onClick = {
                                            onEvent(ConfiguracoesEvent.PosicaoTelefoneChanged(posicao.name))
                                            dropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // Grupo 2: Veículo (Campos Marca e Modelo separados)
                ConfigSectionCard(
                    title = "Veículo",
                    icon = Icons.Default.DirectionsCar
                ) {
                    StyledFormField(
                        label = "Marca do veículo",
                        value = state.marcaVeiculo,
                        onValueChange = { onEvent(ConfiguracoesEvent.MarcaVeiculoChanged(it)) }
                    )
                    StyledFormField(
                        label = "Modelo do veículo",
                        value = state.modeloVeiculo,
                        onValueChange = { onEvent(ConfiguracoesEvent.ModeloVeiculoChanged(it)) }
                    )
                    StyledFormField(
                        label = "Quilometragem (km)",
                        value = state.quilometragemVeiculo,
                        onValueChange = { onEvent(ConfiguracoesEvent.QuilometragemVeiculoChanged(it)) },
                        keyboardType = KeyboardType.Number
                    )
                }

                // Grupo 3: Amostragem
                ConfigSectionCard(
                    title = "Amostragem",
                    icon = Icons.Default.Speed
                ) {
                    StyledFormField(
                        label = "Taxa de atualização GPS (ms)",
                        value = if (state.taxaGpsMs == 0) "" else state.taxaGpsMs.toString(),
                        onValueChange = { input ->
                            if (input.isEmpty()) {
                                onEvent(ConfiguracoesEvent.TaxaGpsChanged(0))
                            } else {
                                input.toIntOrNull()?.let { onEvent(ConfiguracoesEvent.TaxaGpsChanged(it)) }
                            }
                        },
                        keyboardType = KeyboardType.Number
                    )
                    StyledFormField(
                        label = "Taxa de atualização dos sensores (Hz)",
                        value = if (state.taxaSensoresHz == 0) "" else state.taxaSensoresHz.toString(),
                        onValueChange = { input ->
                            if (input.isEmpty()) {
                                onEvent(ConfiguracoesEvent.TaxaSensoresChanged(0))
                            } else {
                                input.toIntOrNull()?.let { onEvent(ConfiguracoesEvent.TaxaSensoresChanged(it)) }
                            }
                        },
                        keyboardType = KeyboardType.Number
                    )
                }

                // Grupo 4: Seleção de Sensores
                ConfigSectionCard(
                    title = "Seleção de Sensores",
                    subtitle = "Ative os sensores desejados para a coleta de dados.",
                    icon = Icons.Default.Settings
                ) {
                    Text(
                        text = "SENSORES INERCIAIS",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp)
                    )

                    SensorToggleItem(
                        icon = Icons.Default.Vibration,
                        title = "Acelerômetro",
                        description = "Força g nos eixos x, y, z",
                        checked = state.acelerometro,
                        onCheckedChange = { onEvent(ConfiguracoesEvent.Acelerometro(it)) }
                    )

                    SensorToggleItem(
                        icon = Icons.Default.ScreenRotation,
                        title = "Giroscópio",
                        description = "Rotação nos eixos x, y, z",
                        checked = state.giroscopio,
                        onCheckedChange = { onEvent(ConfiguracoesEvent.Giroscopio(it)) }
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                    Text(
                        text = "POSICIONAMENTO E PRESSÃO",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )

                    SensorToggleItem(
                        icon = Icons.Default.LocationOn,
                        title = "GPS",
                        description = "Latitude, longitude e velocidade",
                        checked = state.gps,
                        onCheckedChange = { onEvent(ConfiguracoesEvent.Gps(it)) }
                    )

                    SensorToggleItem(
                        icon = Icons.Default.Compress,
                        title = "Barômetro",
                        description = "Pressão atmosférica e altitude",
                        checked = state.barometro,
                        onCheckedChange = { onEvent(ConfiguracoesEvent.Barometro(it)) }
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                    Text(
                        text = "MÍDIA E CAPTURA",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )

                    SensorToggleItem(
                        icon = Icons.Default.Videocam,
                        title = "Câmera (Vídeo/Foto)",
                        description = "Captura visual da rota",
                        checked = state.camera,
                        onCheckedChange = { onEvent(ConfiguracoesEvent.Camera(it)) }
                    )

                    SensorToggleItem(
                        icon = Icons.Default.Mic,
                        title = "Microfone",
                        description = "Nível de ruído ambiente (dB)",
                        checked = state.microfone,
                        onCheckedChange = { onEvent(ConfiguracoesEvent.Microfone(it)) }
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                    Text(
                        text = "DIAGNÓSTICO DO SISTEMA",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )

                    SensorToggleItem(
                        icon = Icons.Default.Thermostat,
                        title = "Temperatura da Bateria",
                        description = "Monitoramento térmico do dispositivo",
                        checked = state.temperaturaBateria,
                        onCheckedChange = { onEvent(ConfiguracoesEvent.TemperaturaBateria(it)) }
                    )

                    SensorToggleItem(
                        icon = Icons.Default.BatteryFull,
                        title = "Nível da Bateria",
                        description = "Percentual de carga do sistema",
                        checked = state.nivelBateria,
                        onCheckedChange = { onEvent(ConfiguracoesEvent.NivelBateria(it)) }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = onLevelClick,
                    enabled = !state.isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = CircleShape,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.25f),
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        text = "Calibração de montagem",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                }

                Button(
                    onClick = { onEvent(ConfiguracoesEvent.Salvar) },
                    enabled = !state.isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = "Salvar",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun StyledIconBadge(icon: ImageVector) {
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
        modifier = Modifier.size(40.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun ConfigSectionCard(
    title: String,
    subtitle: String? = null,
    icon: ImageVector,
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 4.dp)
            ) {
                StyledIconBadge(icon = icon)
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (subtitle != null) {
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            content()
        }
    }
}

@Composable
private fun StyledFormField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            shape = RoundedCornerShape(8.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.3f),
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.2f),
                focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                unfocusedIndicatorColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            )
        )
    }
}

@Composable
private fun SensorToggleItem(
    icon: ImageVector,
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        StyledIconBadge(icon = icon)

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Preview(showBackground = true, heightDp = 1990)
@Composable
private fun ConfiguracoesScreenPreview() {
    RoadifyLoggerTheme {
        ConfiguracoesScreen(
            state = ConfiguracoesState(),
            onEvent = {},
            onLevelClick = {}
        )
    }
}