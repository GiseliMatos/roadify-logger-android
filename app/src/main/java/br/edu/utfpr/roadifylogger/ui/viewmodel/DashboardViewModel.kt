package br.edu.utfpr.roadifylogger.ui.viewmodel

import androidx.camera.view.PreviewView
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class DashboardViewModel(
    private val recordingRepository: RecordingRepository,
    private val settingsRepository: SettingsRepository,
    private val locationRepository: LocationRepository,
    private val cameraRepository: CameraRepository,
    private val audioRepository: AudioRepository,
) : ViewModel() {

    val recordingState: StateFlow<RecordingUiState> = recordingRepository.state

    val configuration: StateFlow<AppConfiguration> = settingsRepository.configuration
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppConfiguration())

    fun hasLocationPermission(): Boolean = locationRepository.hasLocationPermission()
    fun hasCameraPermission(): Boolean = cameraRepository.hasCameraPermission()
    fun hasMicrophonePermission(): Boolean = audioRepository.hasPermission()

    /** Call once ACCESS_FINE_LOCATION is granted so live GPS readings start without an app restart. */
    fun onLocationPermissionGranted() {
        recordingRepository.startGpsPreviewIfPermitted()
    }

    /** Call once CAMERA is granted so the live preview binds without an app restart. */
    fun onCameraPermissionGranted() {
        cameraRepository.onCameraPermissionGranted()
    }

    fun attachCameraPreview(previewView: PreviewView) {
        cameraRepository.attachPreviewSurface(previewView)
    }

    fun clearError() {
        recordingRepository.clearError()
    }

    fun toggleRecording() {
        if (recordingState.value.isRecording) {
            recordingRepository.stop()
        } else {
            recordingRepository.start(configuration.value)
        }
    }
}