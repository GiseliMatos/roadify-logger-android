package br.edu.utfpr.roadifylogger.data.repository

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import br.edu.utfpr.roadifylogger.data.model.MotionSample
import br.edu.utfpr.roadifylogger.data.model.PressureSample
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * Thin wrapper around [SensorManager] exposing raw hardware sensors as cold [Flow]s.
 * Kept separate from [RecordingRepository] so any screen (dashboard, sensor detail,
 * calibration) can independently observe live readings without starting a recording.
 */
class MotionSensorRepository(context: Context) {

    private val sensorManager = context.applicationContext
        .getSystemService(Context.SENSOR_SERVICE) as SensorManager

    /** Gravity-compensated acceleration - used for motion/vibration readouts (dashboard, CSV, sensor detail). */
    fun accelerometer(): Sensor? =
        sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)
            ?: sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    /** Raw accelerometer, gravity included - needed for tilt/roll-pitch math (mount calibration). */
    fun rawAccelerometer(): Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    fun gyroscope(): Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
    fun magnetometer(): Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
    fun barometer(): Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE)

    /** Emits a [MotionSample] every time the given sensor reports new values. */
    fun observe(sensor: Sensor?, samplingPeriodUs: Int = SensorManager.SENSOR_DELAY_GAME): Flow<MotionSample> =
        callbackFlow {
            if (sensor == null) {
                close()
                return@callbackFlow
            }
            val listener = object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent) {
                    trySend(
                        MotionSample(
                            x = event.values.getOrElse(0) { 0f },
                            y = event.values.getOrElse(1) { 0f },
                            z = event.values.getOrElse(2) { 0f },
                            timestampMs = System.currentTimeMillis(),
                        ),
                    )
                }

                override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) = Unit
            }
            sensorManager.registerListener(listener, sensor, samplingPeriodUs)
            awaitClose { sensorManager.unregisterListener(listener) }
        }

    /**
     * Emits a [PressureSample] (hPa + an altitude estimate) every time the barometer
     * reports a new value. Altitude uses the standard sea-level reference pressure,
     * so it's a relative/trend estimate rather than a GPS-grade absolute altitude.
     */
    fun observePressure(sensor: Sensor?, samplingPeriodUs: Int = SensorManager.SENSOR_DELAY_NORMAL): Flow<PressureSample> =
        callbackFlow {
            if (sensor == null) {
                close()
                return@callbackFlow
            }
            val listener = object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent) {
                    val hpa = event.values.getOrElse(0) { 0f }
                    val altitude = SensorManager.getAltitude(SensorManager.PRESSURE_STANDARD_ATMOSPHERE, hpa)
                    trySend(
                        PressureSample(
                            hectopascals = hpa,
                            altitudeMeters = altitude,
                            timestampMs = System.currentTimeMillis(),
                        ),
                    )
                }

                override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) = Unit
            }
            sensorManager.registerListener(listener, sensor, samplingPeriodUs)
            awaitClose { sensorManager.unregisterListener(listener) }
        }
}
