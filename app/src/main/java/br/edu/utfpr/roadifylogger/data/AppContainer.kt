package br.edu.utfpr.roadifylogger.data

import android.content.Context
import br.edu.utfpr.roadifylogger.data.repository.AudioRepository
import br.edu.utfpr.roadifylogger.data.repository.BatteryRepository
import br.edu.utfpr.roadifylogger.data.repository.CameraRepository
import br.edu.utfpr.roadifylogger.data.repository.LocationRepository
import br.edu.utfpr.roadifylogger.data.repository.MotionSensorRepository
import br.edu.utfpr.roadifylogger.data.repository.RecordingRepository
import br.edu.utfpr.roadifylogger.data.repository.SessionFileRepository
import br.edu.utfpr.roadifylogger.data.repository.SettingsRepository

/**
 * Simple manual DI container. Everything here is a process-wide singleton, created once
 * (see [br.edu.utfpr.roadifylogger.RoadifyLoggerApplication]) and shared across screens -
 * this matters most for [recordingRepository], since it starts live sensor/GPS/battery
 * observation in its `init` block and must not be recreated every time the dashboard
 * recomposes or the user switches tabs.
 */
class AppContainer(context: Context) {
    private val appContext = context.applicationContext

    val motionSensorRepository by lazy { MotionSensorRepository(appContext) }
    val locationRepository by lazy { LocationRepository(appContext) }
    val batteryRepository by lazy { BatteryRepository(appContext) }
    val audioRepository by lazy { AudioRepository(appContext) }
    val cameraRepository by lazy { CameraRepository(appContext) }
    val settingsRepository by lazy { SettingsRepository() }
    val sessionFileRepository by lazy { SessionFileRepository(appContext) }

    val recordingRepository by lazy {
        RecordingRepository(
            context = appContext,
            motionSensorRepository = motionSensorRepository,
            locationRepository = locationRepository,
            batteryRepository = batteryRepository,
            audioRepository = audioRepository,
            cameraRepository = cameraRepository,
        )
    }
}
