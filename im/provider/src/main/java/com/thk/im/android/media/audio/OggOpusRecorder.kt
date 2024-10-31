package com.thk.im.android.media.audio

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Handler
import android.os.Looper
import androidx.core.app.ActivityCompat
import com.thk.im.android.core.base.utils.AudioUtils
import com.thk.im.android.ui.protocol.AudioCallback
import com.thk.im.android.ui.protocol.AudioStatus
import top.oply.opuslib.OpusTool
import java.nio.ByteBuffer

object OggOpusRecorder {

    private const val RECORDER_SAMPLE_RATE = 16000
    private const val RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_MONO
    private const val RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT

    @Volatile
    private var recording = false
    private var app: Application? = null
    private var audioCallback: AudioCallback? = null
    private var startAtTimestampMs: Long = 0
    private var audioPath: String? = null
    private var maxDurationMs: Int = 0
    private val handler = Handler(Looper.getMainLooper())


    private val opusTool = OpusTool()

    fun initRecorder(app: Application) {
        this.app = app
    }

    fun startRecord(
        path: String,
        maxDurationMs: Int = 60 * 1000,
        audioCallback: AudioCallback?
    ): Boolean {
        if (app == null) {
            return false
        }
        if (isRecording()) {
            return false
        }
        this.audioPath = path
        this.audioCallback = audioCallback
        this.maxDurationMs = maxDurationMs
        return startRecord()
    }

    private fun startRecord(): Boolean {
        if (ActivityCompat.checkSelfPermission(
                app!!,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return false
        }
        if (audioPath == null) {
            return false
        }
        try {
            val minBufferSize = AudioRecord.getMinBufferSize(
                RECORDER_SAMPLE_RATE,
                RECORDER_CHANNELS,
                RECORDER_AUDIO_ENCODING
            )
            val bufferSize = (minBufferSize / 1920 + 1) * 1920
            val recorder = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                RECORDER_SAMPLE_RATE, RECORDER_CHANNELS,
                RECORDER_AUDIO_ENCODING,
                bufferSize
            )
            startReadRecording(recorder, bufferSize)
        } catch (e: Exception) {
            e.printStackTrace()
            audioCallback = null
            audioPath = null
            return false
        }
        return true
    }

    private fun startReadRecording(recorder: AudioRecord, bufferSize: Int) {
        val buffer = ByteBuffer.allocateDirect(bufferSize)
        var lastNotify = 0L
        val task = Thread {
            kotlin.run {
                try {
                    recorder.startRecording()
                    val ret = opusTool.startRecording(audioPath!!)
                    if (ret != 1) {
                        onStop(0.0, AudioStatus.Exited)
                        return@run
                    }
                    recording = true
                    startAtTimestampMs = System.currentTimeMillis()
                    while (recording) {
                        buffer.rewind()
                        val len = recorder.read(buffer, bufferSize)
                        opusTool.writeFrame(buffer, len)
                        val now = System.currentTimeMillis()
                        if (now - lastNotify > 100) {
                            val calLen = if (len > 256) {
                                256
                            } else {
                                len
                            }
                            val by = ByteArray(calLen)
                            buffer.get(by, 0, calLen)
                            val db = AudioUtils.calculateDecibel(by)
                            callback(db)
                            lastNotify = now
                        }
                        if (now - startAtTimestampMs >= maxDurationMs) {
                            recording = false
                        }
                        buffer.rewind()
                    }
                    opusTool.stopRecording()
                    opusTool.closeOpusFile()
                    recorder.stop()
                    recorder.release()
                    onStop(0.0, AudioStatus.Finished)
                } catch (e: Exception) {
                    e.printStackTrace()
                    onStop(0.0, AudioStatus.Exited)
                }
            }
        }
        task.start()
    }

    fun isRecording(): Boolean {
        return audioCallback != null
    }

    private fun callback(db: Double, status: AudioStatus = AudioStatus.Ing) {
        audioCallback?.let {
            audioPath?.let { path ->
                handler.post {
                    val durationMs = System.currentTimeMillis() - startAtTimestampMs
                    if (durationMs < 500) {
                        it.audioData(path, 0, db, status)
                    } else {
                        val duration = durationMs / 1000 + 1
                        it.audioData(path, duration.toInt(), db, status)
                    }
                }
            }
        }
    }

    private fun onStop(db: Double, status: AudioStatus = AudioStatus.Ing) {
        callback(db, status)
        audioCallback = null
        audioPath = null
    }

    fun stopRecording() {
        recording = false
    }

}