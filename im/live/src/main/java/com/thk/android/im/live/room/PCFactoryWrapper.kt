package com.thk.android.im.live.room

import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Build
import com.thk.android.im.live.LiveManager
import org.webrtc.EglBase
import org.webrtc.PeerConnectionFactory
import org.webrtc.audio.JavaAudioDeviceModule


class PCFactoryWrapper(
    val factory: PeerConnectionFactory,
    val eglCtx: EglBase.Context,
    private val audioDeviceModule: JavaAudioDeviceModule
) {

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
        if (LiveManager.shared().app == null) {
            return emptyArray()
        }
        val audioManager =
            LiveManager.shared().app!!.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val packageManager = LiveManager.shared().app!!.packageManager
        if (!packageManager.hasSystemFeature(PackageManager.FEATURE_AUDIO_OUTPUT)) {
            return emptyArray()
        }

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
        } else {
            emptyArray()
        }
    }
}