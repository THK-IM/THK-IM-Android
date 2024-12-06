package com.thk.im.android.live.engine

import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.AudioDeviceInfo
import android.media.AudioFormat
import android.media.AudioManager
import android.media.MediaRecorder
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.thk.im.android.core.base.LLog
import com.thk.im.android.core.base.utils.AudioUtils
import com.thk.im.android.live.player.RTCMediaPlayer
import com.thk.im.android.live.room.RTCRoomManager
import org.webrtc.DefaultVideoDecoderFactory
import org.webrtc.DefaultVideoEncoderFactory
import org.webrtc.EglBase
import org.webrtc.ExternalAudioProcessingFactory
import org.webrtc.ExternalAudioProcessingFactory.AudioProcessing
import org.webrtc.Loggable
import org.webrtc.Logging
import org.webrtc.PeerConnectionFactory
import org.webrtc.VideoProcessor
import org.webrtc.audio.JavaAudioDeviceModule


class LiveRTCEngine {

    companion object {
        private var innerEngine: LiveRTCEngine? = null

        @Synchronized
        fun shared(): LiveRTCEngine {
            if (innerEngine == null) {
                innerEngine = LiveRTCEngine()
            }
            return innerEngine as LiveRTCEngine
        }
    }

    lateinit var factory: PeerConnectionFactory
    lateinit var eglBaseCtx: EglBase.Context
    private lateinit var audioDeviceModule: JavaAudioDeviceModule
    private lateinit var app: Application
    private var videoProcessor: VideoProcessor? = LiveVideoCaptureProxy()
    private var audioCaptureProxy: AudioProcessing = LiveAudioCaptureProxy()
    private var audioRenderProxy: AudioProcessing = LiveAudioRenderProxy()
    private lateinit var audioProcessingFactory: ExternalAudioProcessingFactory
    private var speakerMuted = false
    private var microphoneMuted = false

    private var lastCaptureTime: Long = 0
    private val handler = Handler(Looper.getMainLooper())

    var mediaPlayer: RTCMediaPlayer? = null

    fun init(app: Application) {
        this.app = app
        this.mediaPlayer = RTCMediaPlayer()
        initEngine()
    }

    private fun initEngine() {
        val logger = Loggable { p0, p1, p2 ->
            LLog.d("LiveRTCEngine", "$p0, $p1, $p2")
        }
        PeerConnectionFactory.initialize(
            PeerConnectionFactory.InitializationOptions.builder(app)
                .setInjectableLogger(logger, Logging.Severity.LS_INFO)
                .createInitializationOptions()
        )
        val eglBase = EglBase.create()
        val eglBaseContext = eglBase.eglBaseContext
        val options = PeerConnectionFactory.Options()
        val encoderFactory = DefaultVideoEncoderFactory(eglBaseContext, true, true)
        val decoderFactory = DefaultVideoDecoderFactory(eglBaseContext)
        audioProcessingFactory = ExternalAudioProcessingFactory()
        audioProcessingFactory.setBypassFlagForRenderPre(false)
        audioProcessingFactory.setBypassFlagForCapturePost(false)
        audioProcessingFactory.setCapturePostProcessing(audioCaptureProxy)
        audioProcessingFactory.setRenderPreProcessing(audioRenderProxy)
        val audioAttributes = AudioAttributes.Builder()
            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
            .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
            .build()
        audioDeviceModule = JavaAudioDeviceModule.builder(app)
            .setUseHardwareAcousticEchoCanceler(false)
            .setUseHardwareNoiseSuppressor(false)
            .setAudioAttributes(audioAttributes)
            .setAudioSource(MediaRecorder.AudioSource.VOICE_COMMUNICATION)
            .setUseLowLatency(true)
            .setUseStereoInput(true)
            .setUseStereoOutput(true)
            .setSamplesReadyCallback {
                val current = System.currentTimeMillis()
                if (current - lastCaptureTime > 500) {
                    captureOriginAudio(it.data)
                    lastCaptureTime = current
                }
            }
            .setAudioBufferCallback { byteBuffer, format, channelCount, sampleRate, bytesRead, l ->
                val timeBefore = System.nanoTime()
                mediaPlayer?.mixBuffer(byteBuffer, bytesRead)
                val after = System.nanoTime() - timeBefore
                return@setAudioBufferCallback l + after
            }
            .createAudioDeviceModule()
        eglBaseCtx = eglBaseContext
        factory = PeerConnectionFactory.builder().setOptions(options)
            .setVideoEncoderFactory(encoderFactory)
            .setVideoDecoderFactory(decoderFactory)
//            .setAudioProcessingFactory(audioProcessingFactory)
            .setAudioDeviceModule(audioDeviceModule)
            .createPeerConnectionFactory()
        audioDeviceModule.setSpeakerMute(speakerMuted)
        audioDeviceModule.setMicrophoneMute(microphoneMuted)

    }

    /**
     * 扬声器外放是否打开
     */
    fun isSpeakerOn(): Boolean {
        val audioManager = app.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        return audioManager.isSpeakerphoneOn
    }

    /**
     * 打开扬声器外放
     */
    fun setSpeakerOn(on: Boolean) {
        // 设置扬声器播放
        val audioManager = app.getSystemService(Context.AUDIO_SERVICE) as? AudioManager ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            app.packageManager.hasSystemFeature(PackageManager.FEATURE_AUDIO_OUTPUT)
        ) {
            if (on) {
                val devices = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
                for (device in devices) {
                    if (device.type == AudioDeviceInfo.TYPE_BUILTIN_SPEAKER) {
                        val res = audioManager.setCommunicationDevice(device)
                        LLog.d("LiveRTCEngine", "setSpeakerOn $res")
                    }
                }
            } else {
                val devices = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
                for (device in devices) {
                    if (device.type != AudioDeviceInfo.TYPE_BUILTIN_SPEAKER) {
                        val res = audioManager.setCommunicationDevice(device)
                        LLog.d("LiveRTCEngine", "setSpeakerOn $res")
                    }
                }
            }
        } else {
            audioManager.isSpeakerphoneOn = on
            audioManager.mode = AudioManager.MODE_NORMAL
        }
    }

    /**
     * rtc音频外放是否禁止
     */
    fun isSpeakerMuted(): Boolean {
        return speakerMuted
    }

    /**
     * 禁止/打开rtc音频外放
     */
    fun muteSpeaker(mute: Boolean) {
        speakerMuted = mute
        audioDeviceModule.setSpeakerMute(mute)
    }

    /**
     * rtc音频输入是否禁止
     */
    fun isMicrophoneMuted(): Boolean {
        return microphoneMuted
    }

    /**
     * 禁止/打开rtc音频输入
     */
    fun setMicrophoneMuted(mute: Boolean) {
        microphoneMuted = mute
        audioDeviceModule.setMicrophoneMute(mute)
    }


    /**
     * 设置音频录入设备
     */
    fun setPreferredInputDevice(preferredInputDevice: AudioDeviceInfo) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            audioDeviceModule.setPreferredInputDevice(preferredInputDevice)
        }
    }

    fun getAudioInputDevice(): Array<AudioDeviceInfo> {
        val audioManager = app.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        if (!app.packageManager.hasSystemFeature(PackageManager.FEATURE_AUDIO_OUTPUT)) {
            return emptyArray()
        }

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
        } else {
            emptyArray()
        }
    }

    fun videoCaptureProxy(): VideoProcessor? {
        return videoProcessor
    }

    fun clearVideoProxy() {
        videoProcessor?.setSink(null)
    }

    fun updateVideoProxy(proxy: VideoProcessor?) {
        videoProcessor = proxy
    }

    fun updateAudioCaptureDelegate(delegate: AudioProcessing) {
        this.audioCaptureProxy = delegate
        this.audioProcessingFactory.setCapturePostProcessing(delegate)
    }

    fun updateAudioRenderDelegate(delegate: AudioProcessing) {
        this.audioRenderProxy = delegate
        this.audioProcessingFactory.setRenderPreProcessing(delegate)
    }

    private fun captureOriginAudio(ba: ByteArray) {
        handler.post {
            val db = AudioUtils.calculateDecibel(ba)
            LLog.d("LiveRTCEngine", "captureOriginAudio $db")
            if (db > 0) {
                RTCRoomManager.shared().allRooms().forEach {
                    it.sendMyVolume(db)
                }
            }
        }

    }

}