package com.thk.im.android.live.engine

import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Build
import com.thk.im.android.core.base.utils.AudioUtils
import com.thk.im.android.live.room.RTCRoomManager
import org.webrtc.DefaultVideoDecoderFactory
import org.webrtc.DefaultVideoEncoderFactory
import org.webrtc.EglBase
import org.webrtc.ExternalAudioProcessingFactory
import org.webrtc.ExternalAudioProcessingFactory.AudioProcessing
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

    fun init(app: Application) {
        this.app = app
        initEngine()
    }

    private fun initEngine() {
        PeerConnectionFactory.initialize(
            PeerConnectionFactory.InitializationOptions.builder(app).createInitializationOptions()
        )
        val eglBase = EglBase.create()
        val eglBaseContext = eglBase.eglBaseContext
        val options = PeerConnectionFactory.Options()
        val encoderFactory = DefaultVideoEncoderFactory(eglBaseContext, true, true)
        val decoderFactory = DefaultVideoDecoderFactory(eglBaseContext)
        audioProcessingFactory = ExternalAudioProcessingFactory()
        audioProcessingFactory.setBypassFlagForRenderPre(true)
        audioProcessingFactory.setBypassFlagForCapturePost(true)
        audioProcessingFactory.setCapturePostProcessing(audioCaptureProxy)
        audioProcessingFactory.setRenderPreProcessing(audioRenderProxy)
        audioDeviceModule = JavaAudioDeviceModule.builder(app)
            .setUseHardwareAcousticEchoCanceler(true)
            .setUseLowLatency(true)
            .setUseStereoInput(true)
            .setUseStereoOutput(true)
            .setSamplesReadyCallback {
                onAudioCapture(it)
            }.createAudioDeviceModule()
        eglBaseCtx = eglBaseContext
        factory = PeerConnectionFactory.builder().setOptions(options)
            .setVideoEncoderFactory(encoderFactory)
            .setVideoDecoderFactory(decoderFactory)
            .setAudioProcessingFactory(audioProcessingFactory)
            .setAudioDeviceModule(audioDeviceModule)
            .createPeerConnectionFactory()
        audioDeviceModule.setSpeakerMute(false)
        audioDeviceModule.setMicrophoneMute(false)
    }


    fun isSpeakerMuted(): Boolean {
        val audioManager = app.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        return !audioManager.isSpeakerphoneOn
    }

    fun muteSpeaker(mute: Boolean) {
        // 设置扬声器播放
        val audioManager = app.getSystemService(Context.AUDIO_SERVICE) as? AudioManager ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            app.packageManager.hasSystemFeature(PackageManager.FEATURE_AUDIO_OUTPUT)
        ) {
            if (!mute) {
                val devices = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
                for (device in devices) {
                    if (device.type == AudioDeviceInfo.TYPE_BUILTIN_SPEAKER) {
                        audioManager.setCommunicationDevice(device)
                    }
                }
            } else {
                audioManager.clearCommunicationDevice()
            }
        } else {
            audioManager.isSpeakerphoneOn = !mute
        }
        audioManager.mode = AudioManager.MODE_NORMAL
    }

    fun setMicrophoneMute(mute: Boolean) {
        audioDeviceModule.setMicrophoneMute(mute)
    }

    fun setSpeakerMute(mute: Boolean) {
        audioDeviceModule.setSpeakerMute(mute)
    }

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

    private fun onAudioCapture(samples: JavaAudioDeviceModule.AudioSamples) {
        val db = AudioUtils.calculateDecibel(samples.data)
        RTCRoomManager.shared().allRooms().forEach {
            it.sendMyVolume(db)
        }
    }
}