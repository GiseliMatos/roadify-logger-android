package br.edu.utfpr.roadifylogger.data.repository

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import java.io.File

private const val TAG = "AudioRepository"

/** Records ambient audio to an .m4a file while a session is running. */
class AudioRepository(private val context: Context) {

    private var recorder: MediaRecorder? = null

    fun hasPermission(): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
            PackageManager.PERMISSION_GRANTED

    /** Starts recording to [outputFile]. Returns false if permission is missing or the recorder fails to start. */
    fun start(outputFile: File): Boolean {
        if (!hasPermission()) return false
        if (recorder != null) return true

        return try {
            outputFile.parentFile?.mkdirs()
            @Suppress("DEPRECATION")
            val mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                MediaRecorder()
            }
            mediaRecorder.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(outputFile.absolutePath)
                prepare()
                start()
            }
            recorder = mediaRecorder
            true
        } catch (e: Exception) {
            Log.e(TAG, "Falha ao iniciar a gravação de áudio", e)
            recorder?.release()
            recorder = null
            false
        }
    }

    /** Peak amplitude since the last call, 0..32767. Safe to call even when not recording. */
    fun currentAmplitude(): Int = try {
        recorder?.maxAmplitude ?: 0
    } catch (e: IllegalStateException) {
        0
    }

    fun stop() {
        val current = recorder ?: return
        try {
            current.stop()
        } catch (e: RuntimeException) {
            Log.e(TAG, "Falha ao parar a gravação de áudio", e)
        } finally {
            current.release()
            recorder = null
        }
    }
}
