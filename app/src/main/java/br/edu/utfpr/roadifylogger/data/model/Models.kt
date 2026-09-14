package br.edu.utfpr.roadifylogger.data.model

/** Which physical/virtual sensors the user can toggle on for a recording. */
enum class SensorKind {
    ACCELEROMETER,
    GYROSCOPE,
    GPS,
    MAGNETOMETER,
    BAROMETER,
    CAMERA,
    MICROPHONE,
}

/** A single reading from a 3-axis motion sensor (accelerometer or gyroscope). */
data class MotionSample(
    val x: Float = 0f,
    val y: Float = 0f,
    val z: Float = 0f,
    val timestampMs: Long = 0L,
)

/** A single barometer reading, plus an altitude estimate derived from it. */
data class PressureSample(
    val hectopascals: Float = 0f,
    val altitudeMeters: Float = 0f,
    val timestampMs: Long = 0L,
)

/** A single GPS fix. */
data class GpsSample(
    val latitude: Double,
    val longitude: Double,
    val accuracyMeters: Float,
    val speedMps: Float,
    val timestampMs: Long,
)

/** Battery diagnostics, refreshed via ACTION_BATTERY_CHANGED. */
data class BatteryStatus(
    val levelPercent: Int = 0,
    val temperatureCelsius: Float = 0f,
)

/** Compass heading in degrees, derived from the magnetometer + accelerometer. */
data class CompassReading(
    val degrees: Float = 0f,
)

enum class PhonePosition { RETRATO, PAISAGEM  }

/** Metadata for a saved recording, shown on the Files screen. */
data class RecordingSession(
    val databaseId: Long,
    val id: String,
    val fileName: String,
    val startedAtMillis: Long,
    val locationLabel: String?,
    val sizeBytes: Long,
    val csvFilePath: String,
)

val ConfiguracaoEntity.enabledSensors: Set<SensorKind>
    get() = buildSet {
        if (acelerometro) add(SensorKind.ACCELEROMETER)
        if (giroscopio) add(SensorKind.GYROSCOPE)
        if (gps) add(SensorKind.GPS)
        if (camera) add(SensorKind.CAMERA)
        if (microfone) add(SensorKind.MICROPHONE)
        if (barometro) add(SensorKind.BAROMETER)
    }

val ConfiguracaoEntity.phonePositionEnum: PhonePosition
    get() = runCatching { PhonePosition.valueOf(posicaoTelefone) }
        .getOrDefault(PhonePosition.RETRATO)
