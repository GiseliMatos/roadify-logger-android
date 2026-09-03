package br.edu.utfpr.pb.dainf.medicaosensores.ui.sensordetail

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
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import br.edu.utfpr.roadifylogger.ui.components.scaleFloorFor
import java.util.Locale

@Composable
fun SensorDetailScreen(
    viewModel: SensorDetailViewModel,
    onBack: () -> Unit,
) {
    val state by viewModel.state.collectAsState()
    val titleRes = when (state.kind) {
        SensorKind.ACCELEROMETER -> R.string.sensor_title_accelerometer
        SensorKind.GYROSCOPE -> R.string.sensor_title_gyroscope
        SensorKind.MAGNETOMETER -> R.string.sensor_title_magnetometer
        else -> R.string.sensor_title_accelerometer
    }
    val descriptionRes = when (state.kind) {
        SensorKind.ACCELEROMETER -> R.string.sensor_detail_accel_description
        SensorKind.GYROSCOPE -> R.string.sensor_detail_gyro_description
        SensorKind.MAGNETOMETER -> R.string.sensor_detail_mag_description
        else -> R.string.sensor_detail_accel_description
    }
    val unitsRes = when (state.kind) {
        SensorKind.ACCELEROMETER -> R.string.sensor_detail_units_accel_body
        SensorKind.GYROSCOPE -> R.string.sensor_detail_units_gyro_body
        SensorKind.MAGNETOMETER -> R.string.sensor_detail_units_mag_body
        else -> R.string.sensor_detail_units_accel_body
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
                        if (state.history.isEmpty()) {
                            Text(stringResource(R.string.sensor_detail_unavailable))
                        } else {
                            TriAxisLineChart(history = state.history, scaleFloor = scaleFloorFor(state.kind))
                            Text(
                                "${state.history.size} ${stringResource(R.string.sensor_detail_samples)}",
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(top = 6.dp),
                            )
                        }
                    }
                }
            }

            item { SectionTitle(stringResource(R.string.sensor_detail_description_title)) }
            item { Text(stringResource(descriptionRes), style = MaterialTheme.typography.bodyMedium) }

            item { SectionTitle(stringResource(R.string.sensor_detail_notes_title)) }
            item { Text(stringResource(R.string.sensor_detail_high_freq_warning), style = MaterialTheme.typography.bodyMedium) }

            item { SectionTitle(stringResource(R.string.sensor_detail_shortcuts_title)) }
            item {
                Column {
                    ShortcutRow(Icons.Filled.Tune, stringResource(R.string.sensor_detail_change_rate))
                    ShortcutRow(Icons.Filled.Tune, stringResource(R.string.sensor_detail_record_extra))
                }
            }

            item { SectionTitle(stringResource(R.string.sensor_detail_units_title)) }
            item { Text(stringResource(unitsRes), style = MaterialTheme.typography.bodyMedium) }
            item {
                Column {
                    ShortcutRow(Icons.Filled.OpenInNew, stringResource(R.string.sensor_detail_view_units_reference))
                    ShortcutRow(Icons.Filled.OpenInNew, stringResource(R.string.sensor_detail_view_coordinates_reference))
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
                        Text("x: ${"%.4f".format(Locale.US, state.latest.x)}", fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
                        Text("y: ${"%.4f".format(Locale.US, state.latest.y)}", fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
                        Text("z: ${"%.4f".format(Locale.US, state.latest.z)}", fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
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
private fun ShortcutRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* wired up in a follow-up sprint */ }
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
