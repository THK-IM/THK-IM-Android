package com.thk.im.android.media.audio

import android.app.Application
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.os.Build
import android.os.Handler
import android.os.Looper
import com.thk.im.android.ui.protocol.AudioCallback
import com.thk.im.android.ui.protocol.AudioStatus
import top.oply.opuslib.OpusTool
import java.io.File
import java.nio.ByteBuffer

object OggOpusPlayer {
    private const val PLAYER_SAMPLE_RATE = 48000
    private const val PLAYER_CHANNELS = AudioFormat.CHANNEL_OUT_MONO
    private const val PLAYER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT

    private var audioCallback: AudioCallback? = null
    private var audioPath: String? = null

    @Volatile
    private var playing = false
    private var app: Application? = null
    private var startAtTimestampMs: Long = 0

    private val opusTool = OpusTool()
    private val handler = Handler(Looper.getMainLooper())

    fun initPlayer(app: Application) {
        this.app = app
    }

    fun isPlaying(): Boolean {
        return audioCallback != null
    }

    fun startPlay(path: String, audioCallback: AudioCallback? = null): Boolean {
        if (isPlaying()) {
            return false
        }
        val file = File(path)
        if (!file.exists()) {
            return false
        }
        val ret = opusTool.isOpusFile(path)
        if (ret <= 0) {
            return false
        }
        this.audioPath = path
        this.audioCallback = audioCallback
        return startPlay()
    }

    private fun startPlay(): Boolean {
        if (audioPath == null) {
            return false
        }
        try {
            val ret = opusTool.openOpusFile(audioPath!!)
            if (ret <= 0) {
                return false
            }
            val attrBuilder = AudioAttributes.Builder()
            attrBuilder.setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .setFlags(AudioAttributes.FLAG_AUDIBILITY_ENFORCED)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                attrBuilder.setAllowedCapturePolicy(AudioAttributes.ALLOW_CAPTURE_BY_SYSTEM)
            }
            val formatBuilder = AudioFormat.Builder()
            formatBuilder.setChannelMask(PLAYER_CHANNELS)
            formatBuilder.setSampleRate(PLAYER_SAMPLE_RATE)
            formatBuilder.setEncoding(PLAYER_AUDIO_ENCODING)
            val bufferSize = AudioTrack.getMinBufferSize(
                PLAYER_SAMPLE_RATE,
                PLAYER_CHANNELS,
                PLAYER_AUDIO_ENCODING
            )
            val audioTrack = AudioTrack(
                attrBuilder.build(), formatBuilder.build(), bufferSize,
                AudioTrack.MODE_STREAM, AudioManager.AUDIO_SESSION_ID_GENERATE
            )
            startWritePlaying(audioTrack, bufferSize)
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            audioCallback = null
            audioPath = null
            return false
        }
    }

    private fun startWritePlaying(audioTrack: AudioTrack, bufferSize: Int) {
        val buffer = ByteBuffer.allocateDirect(bufferSize)
        var lastNotify = 0L

        val task = Thread {
            kotlin.run {
                try {
                    audioTrack.play()
                    val ret = opusTool.openOpusFile(this.audioPath)
                    if (ret == 0) {
                        playing = false
                        onStop(AudioStatus.Exited)
                    } else {
                        playing = true
                    }
                    startAtTimestampMs = System.currentTimeMillis()
                    while (playing) {
                        buffer.rewind()
                        opusTool.readOpusFile(buffer, bufferSize)
                        val size = opusTool.size
                        if (size > 0) {
                            val data = ByteArray(size)
                            buffer.get(data)
                            audioTrack.write(data, 0, size)
                            val now = System.currentTimeMillis()
                            if (now - lastNotify > 100) {
                                val calLen = if (size > 256) {
                                    256
                                } else {
                                    size
                                }
                                val by = data.slice(IntRange(0, calLen - 1)).toByteArray()
                                val db = AudioUtils.calculateDecibel(by)
                                callback(db)
                                lastNotify = now
                            }
                        }
                        if (opusTool.finished != 0) {
                            playing = false
                        }
                    }
                    opusTool.stopPlaying()
                    opusTool.closeOpusFile()
                    audioTrack.stop()
                    audioTrack.release()
                    onStop(AudioStatus.Finished)
                } catch (e: Exception) {
                    e.printStackTrace()
                    onStop(AudioStatus.Exited)
                }
            }
        }
        task.start()
    }

    private fun callback(db: Double, status: AudioStatus = AudioStatus.Ing) {
        audioCallback?.let {
            audioPath?.let { path ->
                val end = System.currentTimeMillis()
                val duration = (end - startAtTimestampMs) / 1000 + 1
                handler.post {
                    it.audioData(path, duration.toInt(), db, status)
                }
            }
        }
    }

    private fun onStop(status: AudioStatus = AudioStatus.Finished) {
        callback(0.0, status)
        audioCallback = null
        audioPath = null
    }

    fun stopPlaying() {
        playing = false
    }
}