package br.edu.utfpr.pb.dainf.medicaosensores.ui.dashboard

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Battery5Bar
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import br.edu.utfpr.pb.dainf.medicaosensores.R
import br.edu.utfpr.pb.dainf.medicaosensores.data.model.MotionSample
import br.edu.utfpr.pb.dainf.medicaosensores.data.model.SensorKind
import br.edu.utfpr.pb.dainf.medicaosensores.ui.components.MiniMagnitudeBarChart
import br.edu.utfpr.pb.dainf.medicaosensores.ui.components.TrendLineChart
import br.edu.utfpr.pb.dainf.medicaosensores.ui.components.scaleFloorFor
import br.edu.utfpr.pb.dainf.medicaosensores.ui.theme.RoadifyRed
import br.edu.utfpr.pb.dainf.medicaosensores.ui.theme.RoadifyRedContainer
import br.edu.utfpr.roadifylogger.ui.viewmodel.DashboardViewModel
import java.util.Locale

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onOpenSensorDetail: (SensorKind) -> Unit,
) {
    val recording by viewModel.recordingState.collectAsState()
    val config by viewModel.configuration.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted -> if (granted) viewModel.onLocationPermissionGranted() }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted -> if (granted) viewModel.onCameraPermissionGranted() }

    val micPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { /* AudioRepository re-checks permission the next time recording starts */ }

    LaunchedEffect(recording.lastError) {
        recording.lastError?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name), fontWeight = FontWeight.Bold) },
                actions = {
                    if (recording.isRecording) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(end = 16.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(RoadifyRed, CircleShape),
                            )
                            Spacer(Modifier.padding(3.dp))
                            Text(formatElapsed(recording.elapsedSeconds), color = RoadifyRed, fontWeight = FontWeight.SemiBold)
                        }
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
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                RecordingStatusBanner(isRecording = recording.isRecording)
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Filled.Battery5Bar,
                        label = stringResource(R.string.dashboard_battery),
                        value = "${recording.battery.levelPercent}%",
                    )
                    StatCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Filled.Thermostat,
                        label = stringResource(R.string.dashboard_temperature),
                        value = "${"%.0f".format(Locale.US, recording.battery.temperatureCelsius)}°C",
                    )
                }
            }

            item {
                Text(
                    stringResource(R.string.dashboard_active_sensors),
                    style = MaterialTheme.typography.titleMedium,
                )
            }

            val activeSensors = if (recording.isRecording) recording.activeSensors else config.enabledSensors

            if (SensorKind.GPS in activeSensors) {
                item {
                    GpsCard(
                        gps = recording.gps,
                        hasPermission = viewModel.hasLocationPermission(),
                        onRequestPermission = { permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION) },
                    )
                }
            }

            if (SensorKind.ACCELEROMETER in activeSensors) {
                item {
                    MotionSensorCard(
                        icon = Icons.Filled.Vibration,
                        title = stringResource(R.string.sensor_title_accelerometer),
                        kind = SensorKind.ACCELEROMETER,
                        sample = recording.accel,
                        history = recording.accelHistory,
                        onClick = { onOpenSensorDetail(SensorKind.ACCELEROMETER) },
                    )
                }
            }

            if (SensorKind.GYROSCOPE in activeSensors) {
                item {
                    MotionSensorCard(
                        icon = Icons.Filled.Explore,
                        title = stringResource(R.string.sensor_title_gyroscope),
                        kind = SensorKind.GYROSCOPE,
                        sample = recording.gyro,
                        history = recording.gyroHistory,
                        onClick = { onOpenSensorDetail(SensorKind.GYROSCOPE) },
                    )
                }
            }

            if (SensorKind.MAGNETOMETER in activeSensors) {
                item {
                    MotionSensorCard(
                        icon = Icons.Filled.Explore,
                        title = stringResource(R.string.sensor_title_magnetometer),
                        kind = SensorKind.MAGNETOMETER,
                        sample = recording.magnetometer,
                        history = recording.magnetometerHistory,
                        onClick = { onOpenSensorDetail(SensorKind.MAGNETOMETER) },
                    )
                }
            }

            if (SensorKind.BAROMETER in activeSensors) {
                item {
                    BarometerCard(pressure = recording.pressure, history = recording.pressureHistory)
                }
            }

            if (SensorKind.CAMERA in activeSensors) {
                item {
                    CameraCard(
                        viewModel = viewModel,
                        isRecordingVideo = recording.isRecordingVideo,
                        hasPermission = viewModel.hasCameraPermission(),
                        onRequestPermission = { cameraPermissionLauncher.launch(Manifest.permission.CAMERA) },
                    )
                }
            }

            if (SensorKind.MICROPHONE in activeSensors) {
                item {
                    MicrophoneCard(
                        isRecording = recording.isRecording,
                        isRecordingAudio = recording.isRecordingAudio,
                        amplitude = recording.micAmplitude,
                        hasPermission = viewModel.hasMicrophonePermission(),
                        onRequestPermission = { micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO) },
                    )
                }
            }

            item { Spacer(Modifier.height(4.dp)) }

            item {
                Button(
                    onClick = { viewModel.toggleRecording() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(26.dp),
                    colors = if (recording.isRecording) {
                        ButtonDefaults.buttonColors(containerColor = RoadifyRed)
                    } else {
                        ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    },
                ) {
                    Icon(
                        if (recording.isRecording) Icons.Filled.Stop else Icons.Filled.PlayArrow,
                        contentDescription = null,
                    )
                    Spacer(Modifier.padding(4.dp))
                    Text(
                        if (recording.isRecording) {
                            stringResource(R.string.dashboard_stop_recording)
                        } else {
                            stringResource(R.string.dashboard_start_recording)
                        },
                    )
                }
            }

            if (recording.isRecording) {
                item {
                    OutlinedButton(
                        onClick = { /* Note/marker capture is a follow-up sprint item */ },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(26.dp),
                    ) {
                        Icon(Icons.Filled.LocationOn, contentDescription = null)
                        Spacer(Modifier.padding(4.dp))
                        Text(stringResource(R.string.dashboard_add_note))
                    }
                }
            }
        }
    }
}

@Composable
private fun RecordingStatusBanner(isRecording: Boolean) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isRecording) RoadifyRedContainer else MaterialTheme.colorScheme.surfaceVariant,
        ),
        shape = RoundedCornerShape(16.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(if (isRecording) RoadifyRed else Color.Gray, CircleShape),
                )
                Spacer(Modifier.padding(6.dp))
                Text(
                    if (isRecording) stringResource(R.string.dashboard_recording_active) else stringResource(R.string.dashboard_recording_stopped),
                    fontWeight = FontWeight.SemiBold,
                    color = if (isRecording) RoadifyRed else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (isRecording) {
                Box(
                    modifier = Modifier
                        .background(RoadifyRed, RoundedCornerShape(50))
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                ) {
                    Text(stringResource(R.string.dashboard_rec), color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun StatCard(
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.padding(3.dp))
                Text(label, style = MaterialTheme.typography.labelSmall)
            }
            Spacer(Modifier.height(6.dp))
            Text(value, style = MaterialTheme.typography.headlineSmall.copy(fontFamily = androidx.compose.ui.text.font.FontFamily.Default))
        }
    }
}

@Composable
private fun GpsCard(
    gps: br.edu.utfpr.pb.dainf.medicaosensores.data.model.GpsSample?,
    hasPermission: Boolean,
    onRequestPermission: () -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.padding(4.dp))
                    Text("GPS", fontWeight = FontWeight.SemiBold)
                }
                if (gps != null) {
                    Text(
                        stringResource(R.string.dashboard_accuracy, gps.accuracyMeters),
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
            }
            Spacer(Modifier.height(10.dp))
            when {
                !hasPermission -> {
                    Text(stringResource(R.string.dashboard_location_permission_needed), style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(onClick = onRequestPermission) {
                        Text(stringResource(R.string.dashboard_grant_permission))
                    }
                }
                gps == null -> Text(stringResource(R.string.dashboard_gps_waiting), style = MaterialTheme.typography.bodyMedium)
                else -> {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("Latitude", style = MaterialTheme.typography.labelSmall)
                            Text("%.6f".format(Locale.US, gps.latitude))
                        }
                        Column {
                            Text("Longitude", style = MaterialTheme.typography.labelSmall)
                            Text("%.6f".format(Locale.US, gps.longitude))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BarometerCard(
    pressure: br.edu.utfpr.pb.dainf.medicaosensores.data.model.PressureSample,
    history: List<Float>,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Speed, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.padding(4.dp))
                Text(stringResource(R.string.sensor_title_barometer), fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.height(8.dp))
            if (history.size < 2) {
                Text(stringResource(R.string.sensor_barometer_waiting), style = MaterialTheme.typography.bodyMedium)
            } else {
                TrendLineChart(history = history)
                Spacer(Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(
                        stringResource(R.string.sensor_barometer_pressure, pressure.hectopascals),
                        style = MaterialTheme.typography.labelSmall,
                    )
                    Text(
                        stringResource(R.string.sensor_barometer_altitude, pressure.altitudeMeters),
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
            }
        }
    }
}

@Composable
private fun MotionSensorCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    kind: SensorKind,
    sample: MotionSample,
    history: List<MotionSample>,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.padding(4.dp))
                Text(title, fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.height(8.dp))
            MiniMagnitudeBarChart(history = history, scaleFloor = scaleFloorFor(kind))
            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("X: ${"%.2f".format(Locale.US, sample.x)}", style = MaterialTheme.typography.labelSmall)
                Text("Y: ${"%.2f".format(Locale.US, sample.y)}", style = MaterialTheme.typography.labelSmall)
                Text("Z: ${"%.2f".format(Locale.US, sample.z)}", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
private fun CameraCard(
    viewModel: DashboardViewModel,
    isRecordingVideo: Boolean,
    hasPermission: Boolean,
    onRequestPermission: () -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Videocam, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.padding(4.dp))
                    Text(stringResource(R.string.sensor_camera_title), fontWeight = FontWeight.SemiBold)
                }
                if (isRecordingVideo) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(8.dp).background(RoadifyRed, CircleShape))
                        Spacer(Modifier.padding(3.dp))
                        Text(stringResource(R.string.dashboard_rec), color = RoadifyRed, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
            if (!hasPermission) {
                Text(stringResource(R.string.sensor_camera_permission_needed), style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(8.dp))
                OutlinedButton(onClick = onRequestPermission) {
                    Text(stringResource(R.string.dashboard_grant_permission))
                }
            } else {
                androidx.compose.ui.viewinterop.AndroidView(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    factory = { context ->
                        androidx.camera.view.PreviewView(context).also { previewView ->
                            viewModel.attachCameraPreview(previewView)
                        }
                    },
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    if (isRecordingVideo) stringResource(R.string.sensor_camera_recording) else stringResource(R.string.sensor_camera_status),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun MicrophoneCard(
    isRecording: Boolean,
    isRecordingAudio: Boolean,
    amplitude: Int,
    hasPermission: Boolean,
    onRequestPermission: () -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Mic, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.padding(4.dp))
                    Text(stringResource(R.string.sensor_microphone_title), fontWeight = FontWeight.SemiBold)
                }
                if (isRecordingAudio) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(8.dp).background(RoadifyRed, CircleShape))
                        Spacer(Modifier.padding(3.dp))
                        Text(stringResource(R.string.dashboard_rec), color = RoadifyRed, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
            when {
                !hasPermission -> {
                    Text(stringResource(R.string.sensor_microphone_permission_needed), style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(onClick = onRequestPermission) {
                        Text(stringResource(R.string.dashboard_grant_permission))
                    }
                }
                isRecordingAudio -> {
                    // Peak amplitude from MediaRecorder maxes out around 32767.
                    val level = (amplitude / 32767f).coerceIn(0f, 1f)
                    androidx.compose.foundation.layout.Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(5.dp)),
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(level)
                                .height(10.dp)
                                .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(5.dp)),
                        )
                    }
                }
                isRecording -> Text(stringResource(R.string.sensor_microphone_status), style = MaterialTheme.typography.bodyMedium)
                else -> Text(stringResource(R.string.sensor_microphone_idle), style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

private fun formatElapsed(totalSeconds: Long): String {
    val h = totalSeconds / 3600
    val m = (totalSeconds % 3600) / 60
    val s = totalSeconds % 60
    return "%02d:%02d:%02d".format(Locale.US, h, m, s)
}
