package com.thk.android.im.live

import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Build
import com.thk.android.im.live.api.RoomApi
import com.thk.android.im.live.vo.CreateRoomReqVo
import com.thk.android.im.live.vo.JoinRoomReqVo
import com.thk.android.im.live.room.PCFactoryWrapper
import com.thk.android.im.live.room.Room
import io.reactivex.Flowable
import org.webrtc.DefaultVideoDecoderFactory
import org.webrtc.DefaultVideoEncoderFactory
import org.webrtc.EglBase
import org.webrtc.PeerConnectionFactory
import org.webrtc.audio.JavaAudioDeviceModule
import org.webrtc.audio.JavaAudioDeviceModule.SamplesReadyCallback


class LiveManager private constructor() {

    companion object {
        private var innerManager: LiveManager? = null

        @Synchronized
        fun shared(): LiveManager {
            if (innerManager == null) {
                innerManager = LiveManager()
            }
            return innerManager as LiveManager
        }
    }

    var app: Application? = null
    var selfId: Long = 0L
    lateinit var liveApi: LiveApi
    private var pcFactoryWrapper: PCFactoryWrapper? = null
    private var _room: Room? = null

    fun init(app: Application) {
        this.app = app
        PeerConnectionFactory.initialize(
            PeerConnectionFactory.InitializationOptions
                .builder(app).createInitializationOptions()
        )
        // 设置扬声器播放
        val audioManager = app.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&  // Check FEATURE_AUDIO_OUTPUT to guard against false positives.
            app.packageManager.hasSystemFeature(PackageManager.FEATURE_AUDIO_OUTPUT)
        ) {
            val devices = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
            for (device in devices) {
                if (device.type == AudioDeviceInfo.TYPE_BUILTIN_SPEAKER) {
                    audioManager.setCommunicationDevice(device)
                }
            }
        } else {
            audioManager.isSpeakerphoneOn = true
        }
        audioManager.mode = AudioManager.MODE_NORMAL
    }

    fun joinRoom(roomId: String, role: Role): Flowable<Room> {
        _room?.destroy()
        return liveApi.joinRoom(JoinRoomReqVo(roomId, this.selfId, role.value))
            .flatMap {
                val members = mutableListOf<Member>()
                for (m in it.members) {
                    if (m.uId != selfId) {
                        members.add(m)
                    }
                }
                val mode = when (it.mode) {
                    1 -> Mode.Chat
                    2 -> Mode.Audio
                    3 -> Mode.Video
                    else -> Mode.Chat
                }
                val room = Room(roomId, selfId, mode, role, members)
                _room = room
                Flowable.just(room)
            }
    }

    fun createRoom(mode: Mode): Flowable<Room> {
        _room?.destroy()
        return liveApi.createRoom(CreateRoomReqVo(this.selfId, mode.value))
            .flatMap {
                val room = Room(it.id, selfId, mode, Role.Broadcaster, it.members)
                _room = room
                Flowable.just(room)
            }
    }

    fun getRoom(): Room? {
        return _room
    }

    fun destroyRoom() {
        _room?.destroy()
        _room = null
    }

    fun getPCFactoryWrapper(): PCFactoryWrapper {
        synchronized(this) {
            if (this.pcFactoryWrapper == null) {
                val eglBase = EglBase.create()
                val eglBaseContext = eglBase.eglBaseContext
                val options = PeerConnectionFactory.Options()
                val encoderFactory = DefaultVideoEncoderFactory(eglBaseContext, true, true)
                val decoderFactory = DefaultVideoDecoderFactory(eglBaseContext)
                val audioDeviceModule = JavaAudioDeviceModule.builder(this.app)
                    .setSamplesReadyCallback { }
                    .createAudioDeviceModule()
                val peerConnectionFactory = PeerConnectionFactory.builder().setOptions(options)
                    .setVideoEncoderFactory(encoderFactory).setVideoDecoderFactory(decoderFactory)
                    .setAudioDeviceModule(audioDeviceModule)
                    .createPeerConnectionFactory()
                audioDeviceModule.setSpeakerMute(false)
                audioDeviceModule.setMicrophoneMute(false)
                this.pcFactoryWrapper =
                    PCFactoryWrapper(peerConnectionFactory, eglBaseContext, audioDeviceModule)
            }
            return this.pcFactoryWrapper!!
        }
    }

}