package br.edu.utfpr.roadifylogger.data.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.math.atan2
import kotlin.math.sqrt

// Responsável por ler o acelerômetro e calcular os valores de Roll e Pitch
class OrientationSensor(context: Context) : SensorEventListener {

    private val sensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    private val accelerometer: Sensor? =
        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private var onOrientationChanged: ((roll: Float, pitch: Float) -> Unit)? = null

    private var filteredRoll = 0f
    private var filteredPitch = 0f

    private val smoothingFactor = 0.15f

    fun startListening(
        onOrientationChanged: (roll: Float, pitch: Float) -> Unit
    ): Boolean {
        val sensor = accelerometer ?: return false

        this.onOrientationChanged = onOrientationChanged

        return sensorManager.registerListener(
            this,
            sensor,
            SensorManager.SENSOR_DELAY_GAME
        )
    }

    fun stopListening() {
        sensorManager.unregisterListener(this)
        onOrientationChanged = null
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type != Sensor.TYPE_ACCELEROMETER) {
            return
        }

        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]

        val roll = Math.toDegrees(
            atan2(y.toDouble(), z.toDouble())
        ).toFloat()

        val pitch = Math.toDegrees(
            atan2(
                -x.toDouble(),
                sqrt((y * y + z * z).toDouble())
            )
        ).toFloat()

        filteredRoll += smoothingFactor * (roll - filteredRoll)
        filteredPitch += smoothingFactor * (pitch - filteredPitch)

        onOrientationChanged?.invoke(
            filteredRoll,
            filteredPitch
        )
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}