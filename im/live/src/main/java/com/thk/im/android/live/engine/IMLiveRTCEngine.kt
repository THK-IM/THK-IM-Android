package com.thk.im.android.live.engine

import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Build
import org.webrtc.DefaultVideoDecoderFactory
import org.webrtc.DefaultVideoEncoderFactory
import org.webrtc.EglBase
import org.webrtc.PeerConnectionFactory
import org.webrtc.audio.JavaAudioDeviceModule


class IMLiveRTCEngine {

    companion object {
        private var innerEngine: IMLiveRTCEngine? = null

        @Synchronized
        fun shared(): IMLiveRTCEngine {
            if (innerEngine == null) {
                innerEngine = IMLiveRTCEngine()
            }
            return innerEngine as IMLiveRTCEngine
        }
    }

    lateinit var factory: PeerConnectionFactory
    lateinit var eglBaseCtx: EglBase.Context
    private lateinit var audioDeviceModule: JavaAudioDeviceModule
    private lateinit var app: Application

    fun init(app: Application) {
        this.app = app
        PeerConnectionFactory.initialize(
            PeerConnectionFactory.InitializationOptions.builder(app).createInitializationOptions()
        )
        val eglBase = EglBase.create()
        val eglBaseContext = eglBase.eglBaseContext
        val options = PeerConnectionFactory.Options()
        val encoderFactory = DefaultVideoEncoderFactory(eglBaseContext, true, true)
        val decoderFactory = DefaultVideoDecoderFactory(eglBaseContext)
        audioDeviceModule = JavaAudioDeviceModule.builder(app)
            .setPlaybackSamplesReadyCallback {
                // TODO
            }
            .setSamplesReadyCallback {
                // TODO
            }.createAudioDeviceModule()
        eglBaseCtx = eglBaseContext
        factory = PeerConnectionFactory.builder().setOptions(options)
            .setVideoEncoderFactory(encoderFactory)
            .setVideoDecoderFactory(decoderFactory)
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
}