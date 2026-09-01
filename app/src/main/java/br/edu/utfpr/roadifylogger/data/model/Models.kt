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

/** Persisted app configuration (Settings screen). */
data class AppConfiguration(
    val phoneBrand: String = "",
    val phoneModel: String = "",
    val phonePosition: PhonePosition = PhonePosition.PORTRAIT,
    val vehicleBrandModel: String = "",
    val vehicleMileageKm: Int = 0,
    val gpsUpdateRateMs: Int = 1000,
    val sensorUpdateRateHz: Int = 50,
    val enabledSensors: Set<SensorKind> = setOf(
        SensorKind.ACCELEROMETER,
        SensorKind.GYROSCOPE,
        SensorKind.GPS,
    ),
    val mountCalibrationRollDeg: Float = 0f,
    val mountCalibrationPitchDeg: Float = 0f,
)

enum class PhonePosition { PORTRAIT, LANDSCAPE }

/** Metadata for a saved recording, shown on the Files screen. */
data class RecordingSession(
    val id: String,
    val startedAtMillis: Long,
    val locationLabel: String?,
    val sizeBytes: Long,
    val csvFilePath: String,
)
