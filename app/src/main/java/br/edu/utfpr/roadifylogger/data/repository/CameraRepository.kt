package br.edu.utfpr.roadifylogger.data.repository

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.ProcessLifecycleOwner
import java.io.File

private const val TAG = "CameraRepository"

/**
 * Wraps CameraX for the dashboard's live preview + video recording.
 *
 * Bound to [ProcessLifecycleOwner] rather than an Activity/Fragment lifecycle: the
 * repository is owned by the ViewModel layer (outliving individual Compose
 * recompositions/navigation), so it follows the app's foreground/background
 * lifecycle instead.
 */
class CameraRepository(private val context: Context) {

    private val appContext = context.applicationContext
    private var cameraProvider: ProcessCameraProvider? = null
    private var videoCapture: VideoCapture<Recorder>? = null
    private var activeRecording: Recording? = null
    private var pendingPreviewView: PreviewView? = null

    fun hasCameraPermission(): Boolean =
        ContextCompat.checkSelfPermission(appContext, Manifest.permission.CAMERA) ==
            PackageManager.PERMISSION_GRANTED

    /** Call once CAMERA is granted so a preview requested earlier can bind immediately. */
    fun onCameraPermissionGranted() {
        pendingPreviewView?.let { attachPreviewSurface(it) }
    }

    /** Binds the preview use case (and a video-capture use case, ready for [startRecording]) to [previewView]. */
    fun attachPreviewSurface(previewView: PreviewView) {
        pendingPreviewView = previewView
        if (!hasCameraPermission()) return

        val providerFuture = ProcessCameraProvider.getInstance(appContext)
        providerFuture.addListener(
            {
                try {
                    val provider = providerFuture.get()
                    cameraProvider = provider

                    val preview = Preview.Builder().build().also {
                        it.surfaceProvider = previewView.surfaceProvider
                    }

                    val recorder = Recorder.Builder().build()
                    val capture = VideoCapture.withOutput(recorder)

                    provider.unbindAll()
                    provider.bindToLifecycle(
                        ProcessLifecycleOwner.get(),
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview,
                        capture,
                    )
                    videoCapture = capture
                } catch (e: Exception) {
                    Log.e(TAG, "Falha ao vincular a câmera", e)
                }
            },
            ContextCompat.getMainExecutor(appContext),
        )
    }

    /** Starts recording video to [outputFile]. Returns false if the camera isn't ready or permission is missing. */
    fun startRecording(outputFile: File): Boolean {
        val capture = videoCapture ?: return false
        if (!hasCameraPermission()) return false

        return try {
            outputFile.parentFile?.mkdirs()
            val outputOptions = FileOutputOptions.Builder(outputFile).build()
            activeRecording = capture.output
                .prepareRecording(appContext, outputOptions)
                .start(ContextCompat.getMainExecutor(appContext)) { event ->
                    if (event is VideoRecordEvent.Finalize && event.hasError()) {
                        Log.e(TAG, "Erro ao finalizar a gravação de vídeo: ${event.error}")
                    }
                }
            true
        } catch (e: Exception) {
            Log.e(TAG, "Falha ao iniciar a gravação de vídeo", e)
            false
        }
    }

    fun stopRecording() {
        activeRecording?.stop()
        activeRecording = null
    }
}
