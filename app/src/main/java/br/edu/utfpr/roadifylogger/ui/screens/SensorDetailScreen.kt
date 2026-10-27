package br.edu.utfpr.roadifylogger.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import br.edu.utfpr.roadifylogger.R
import br.edu.utfpr.roadifylogger.data.model.SensorKind
import br.edu.utfpr.roadifylogger.ui.components.TriAxisLineChart
import br.edu.utfpr.roadifylogger.ui.components.TrendLineChart
import br.edu.utfpr.roadifylogger.ui.components.scaleFloorFor
import br.edu.utfpr.roadifylogger.ui.viewmodel.SensorDetailViewModel
import java.util.Locale

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun SensorDetailScreen(
    viewModel: SensorDetailViewModel,
    onBack: () -> Unit,
    onOpenSettings: (SettingsSection) -> Unit,
) {
    val state by viewModel.state.collectAsState()
    val titleRes = when (state.kind) {
        SensorKind.ACCELEROMETER -> R.string.sensor_title_accelerometer
        SensorKind.GYROSCOPE -> R.string.sensor_title_gyroscope
        SensorKind.MAGNETOMETER -> R.string.sensor_title_magnetometer
        SensorKind.GPS -> R.string.sensor_title_gps
        SensorKind.BAROMETER -> R.string.sensor_title_barometer
        SensorKind.CAMERA -> R.string.sensor_camera_title
        SensorKind.MICROPHONE -> R.string.sensor_microphone_title
        SensorKind.BATTERY_LEVEL -> R.string.sensor_title_battery_level
        SensorKind.BATTERY_TEMPERATURE -> R.string.sensor_title_battery_temperature
    }
    val descriptionRes = when (state.kind) {
        SensorKind.ACCELEROMETER -> R.string.sensor_detail_accel_description
        SensorKind.GYROSCOPE -> R.string.sensor_detail_gyro_description
        SensorKind.MAGNETOMETER -> R.string.sensor_detail_mag_description
        SensorKind.GPS -> R.string.sensor_detail_gps_description
        SensorKind.BAROMETER -> R.string.sensor_detail_barometer_description
        SensorKind.CAMERA -> R.string.sensor_detail_camera_description
        SensorKind.MICROPHONE -> R.string.sensor_detail_microphone_description
        SensorKind.BATTERY_LEVEL -> R.string.sensor_detail_battery_level_description
        SensorKind.BATTERY_TEMPERATURE -> R.string.sensor_detail_battery_temperature_description
    }
    val unitsRes = when (state.kind) {
        SensorKind.ACCELEROMETER -> R.string.sensor_detail_units_accel_body
        SensorKind.GYROSCOPE -> R.string.sensor_detail_units_gyro_body
        SensorKind.MAGNETOMETER -> R.string.sensor_detail_units_mag_body
        SensorKind.GPS -> R.string.sensor_detail_units_gps_body
        SensorKind.BAROMETER -> R.string.sensor_detail_units_barometer_body
        SensorKind.CAMERA -> R.string.sensor_detail_units_camera_body
        SensorKind.MICROPHONE -> R.string.sensor_detail_units_microphone_body
        SensorKind.BATTERY_LEVEL -> R.string.sensor_detail_units_battery_level_body
        SensorKind.BATTERY_TEMPERATURE -> R.string.sensor_detail_units_battery_temperature_body
    }
    val notesRes = when (state.kind) {
        SensorKind.GPS -> R.string.sensor_detail_gps_note
        SensorKind.CAMERA -> R.string.sensor_detail_camera_note
        SensorKind.MICROPHONE -> R.string.sensor_detail_microphone_note
        SensorKind.BATTERY_LEVEL,
        SensorKind.BATTERY_TEMPERATURE -> R.string.sensor_detail_battery_note
        else -> R.string.sensor_detail_high_freq_warning
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.sensor_detail_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
                    }
                },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            item {
                Card(shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)) {
                    Column(Modifier.padding(16.dp)) {
                        Row {
                            Icon(Icons.Filled.Sensors, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.padding(4.dp))
                            Text(stringResource(titleRes), style = MaterialTheme.typography.titleLarge)
                        }
                        Spacer(Modifier.height(12.dp))
                        if (!state.isAvailable) {
                            Text(stringResource(R.string.sensor_detail_unavailable))
                        } else when (state.kind) {
                            SensorKind.ACCELEROMETER,
                            SensorKind.GYROSCOPE,
                            SensorKind.MAGNETOMETER -> {
                                if (state.history.isEmpty()) {
                                    Text(stringResource(R.string.sensor_detail_waiting))
                                } else {
                                    TriAxisLineChart(history = state.history, scaleFloor = scaleFloorFor(state.kind))
                                    Text(
                                        "${state.history.size} ${stringResource(R.string.sensor_detail_samples)}",
                                        style = MaterialTheme.typography.labelSmall,
                                        modifier = Modifier.padding(top = 6.dp),
                                    )
                                }
                            }
                            SensorKind.BAROMETER -> {
                                if (state.pressureHistory.size < 2) {
                                    Text(stringResource(R.string.sensor_barometer_waiting))
                                } else {
                                    TrendLineChart(history = state.pressureHistory)
                                    Text(
                                        stringResource(R.string.sensor_barometer_pressure, state.pressure.hectopascals),
                                        style = MaterialTheme.typography.labelSmall,
                                        modifier = Modifier.padding(top = 6.dp),
                                    )
                                }
                            }
                            SensorKind.GPS -> Text(
                                if (state.gps == null) stringResource(R.string.dashboard_gps_waiting)
                                else stringResource(R.string.sensor_detail_gps_active),
                            )
                            SensorKind.CAMERA -> Text(
                                if (state.isRecordingVideo) stringResource(R.string.sensor_camera_recording)
                                else stringResource(R.string.sensor_camera_status),
                            )
                            SensorKind.MICROPHONE -> {
                                Text(
                                    if (state.isRecordingAudio) stringResource(R.string.sensor_microphone_recording)
                                    else stringResource(R.string.sensor_microphone_idle),
                                )
                                LinearProgressIndicator(
                                    progress = { (state.microphoneAmplitude / 32767f).coerceIn(0f, 1f) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 10.dp),
                                )
                            }
                            SensorKind.BATTERY_LEVEL -> Text("${state.battery.levelPercent}%")
                            SensorKind.BATTERY_TEMPERATURE -> Text(
                                "${"%.1f".format(Locale.US, state.battery.temperatureCelsius)} °C",
                            )
                        }
                    }
                }
            }

            item { SectionTitle(stringResource(R.string.sensor_detail_description_title)) }
            item { Text(stringResource(descriptionRes), style = MaterialTheme.typography.bodyMedium) }

            item { SectionTitle(stringResource(R.string.sensor_detail_notes_title)) }
            item { Text(stringResource(notesRes), style = MaterialTheme.typography.bodyMedium) }

            item { SectionTitle(stringResource(R.string.sensor_detail_shortcuts_title)) }
            item {
                Column {
                    ShortcutRow(
                        Icons.Filled.Tune,
                        stringResource(R.string.sensor_detail_change_rate),
                        onClick = { onOpenSettings(SettingsSection.SAMPLING) },
                    )
                    ShortcutRow(
                        Icons.Filled.Tune,
                        stringResource(R.string.sensor_detail_configure_sensor),
                        onClick = {
                            onOpenSettings(
                                if (state.kind == SensorKind.CAMERA || state.kind == SensorKind.MICROPHONE) {
                                    SettingsSection.MEDIA
                                } else {
                                    SettingsSection.SENSORS
                                },
                            )
                        },
                    )
                }
            }

            item { SectionTitle(stringResource(R.string.sensor_detail_units_title)) }
            item { Text(stringResource(unitsRes), style = MaterialTheme.typography.bodyMedium) }
            item {
                Column {
                    Text(
                        stringResource(R.string.sensor_detail_chart_filter_note),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            item { SectionTitle(stringResource(R.string.sensor_detail_recent_readings)) }
            item { Text(stringResource(R.string.sensor_detail_recent_readings_body), style = MaterialTheme.typography.bodyMedium) }
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                ) {
                    Column(Modifier.padding(14.dp)) {
                        val mono = androidx.compose.ui.text.font.FontFamily.Monospace
                        when (state.kind) {
                            SensorKind.ACCELEROMETER,
                            SensorKind.GYROSCOPE,
                            SensorKind.MAGNETOMETER -> {
                                Text("x: ${"%.4f".format(Locale.US, state.latest.x)}", fontFamily = mono)
                                Text("y: ${"%.4f".format(Locale.US, state.latest.y)}", fontFamily = mono)
                                Text("z: ${"%.4f".format(Locale.US, state.latest.z)}", fontFamily = mono)
                            }
                            SensorKind.BAROMETER -> {
                                Text("pressão: ${"%.2f".format(Locale.US, state.pressure.hectopascals)} hPa", fontFamily = mono)
                                Text("altitude: ${"%.1f".format(Locale.US, state.pressure.altitudeMeters)} m", fontFamily = mono)
                            }
                            SensorKind.GPS -> {
                                val gps = state.gps
                                if (gps == null) {
                                    Text(stringResource(R.string.dashboard_gps_waiting))
                                } else {
                                    Text("latitude: ${"%.6f".format(Locale.US, gps.latitude)}", fontFamily = mono)
                                    Text("longitude: ${"%.6f".format(Locale.US, gps.longitude)}", fontFamily = mono)
                                    Text("precisão: ${"%.1f".format(Locale.US, gps.accuracyMeters)} m", fontFamily = mono)
                                    Text("velocidade: ${"%.2f".format(Locale.US, gps.speedMps)} m/s", fontFamily = mono)
                                }
                            }
                            SensorKind.CAMERA -> Text(
                                if (state.isRecordingVideo) stringResource(R.string.sensor_camera_recording)
                                else stringResource(R.string.sensor_camera_status),
                            )
                            SensorKind.MICROPHONE -> Text("amplitude: ${state.microphoneAmplitude}", fontFamily = mono)
                            SensorKind.BATTERY_LEVEL -> Text("nível: ${state.battery.levelPercent}%", fontFamily = mono)
                            SensorKind.BATTERY_TEMPERATURE -> Text(
                                "temperatura: ${"%.1f".format(Locale.US, state.battery.temperatureCelsius)} °C",
                                fontFamily = mono,
                            )
                        }
                    }
                }
            }

            state.hardwareInfo?.let { info ->
                item { SectionTitle(stringResource(R.string.sensor_detail_info_title)) }
                item { Text(stringResource(R.string.sensor_detail_info_body), style = MaterialTheme.typography.bodyMedium) }
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                    ) {
                        Column(Modifier.padding(14.dp)) {
                            val mono = androidx.compose.ui.text.font.FontFamily.Monospace
                            Text("name: ${info.name}", fontFamily = mono)
                            Text("wake up: ${info.wakeUpSensor}", fontFamily = mono)
                            Text("vendor: ${info.vendor}", fontFamily = mono)
                            Text("version: ${info.version}", fontFamily = mono)
                            Text("max value: ${info.maxRange}", fontFamily = mono)
                            Text("min delay (us): ${info.minDelayUs}", fontFamily = mono)
                            Text("resolution: ${info.resolution}", fontFamily = mono)
                            Text("power use: ${info.power}mA", fontFamily = mono)
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(8.dp)) }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, style = MaterialTheme.typography.titleMedium)
}

@Composable
private fun ShortcutRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row {
            Icon(icon, contentDescription = null, modifier = Modifier.height(20.dp))
            Spacer(Modifier.padding(4.dp))
            Text(label)
        }
        Icon(Icons.Filled.ChevronRight, contentDescription = null)
    }
}
