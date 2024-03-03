package com.thk.android.im.live

import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Build
import com.thk.android.im.live.room.PCFactoryWrapper
import com.thk.android.im.live.room.Room
import com.thk.android.im.live.vo.CreateRoomReqVo
import com.thk.android.im.live.vo.DelRoomVo
import com.thk.android.im.live.vo.JoinRoomReqVo
import com.thk.android.im.live.vo.RefuseJoinRoomVo
import com.thk.im.android.core.base.BaseSubscriber
import com.thk.im.android.core.base.RxTransform
import io.reactivex.Flowable
import org.webrtc.DefaultVideoDecoderFactory
import org.webrtc.DefaultVideoEncoderFactory
import org.webrtc.EglBase
import org.webrtc.PeerConnectionFactory
import org.webrtc.audio.JavaAudioDeviceModule


class IMLiveManager private constructor() {

    companion object {
        private var innerManager: IMLiveManager? = null

        @Synchronized
        fun shared(): IMLiveManager {
            if (innerManager == null) {
                innerManager = IMLiveManager()
            }
            return innerManager as IMLiveManager
        }
    }

    var app: Application? = null
    var selfId: Long = 0L
    lateinit var liveApi: LiveApi
    private var pcFactoryWrapper: PCFactoryWrapper? = null
    private var room: Room? = null

    fun init(app: Application) {
        this.app = app
        PeerConnectionFactory.initialize(
            PeerConnectionFactory.InitializationOptions
                .builder(app).createInitializationOptions()
        )
        muteSpeaker(false)
    }

    fun isSpeakerMuted(): Boolean {
        if (app == null) {
            return false
        }
        val audioManager = app!!.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        return !audioManager.isSpeakerphoneOn
    }

    fun muteSpeaker(mute: Boolean) {
        if (app == null) {
            return
        }
        // 设置扬声器播放
        val audioManager = app!!.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&  // Check FEATURE_AUDIO_OUTPUT to guard against false positives.
            app!!.packageManager.hasSystemFeature(PackageManager.FEATURE_AUDIO_OUTPUT)
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

    fun joinRoom(roomId: String, role: Role): Flowable<Room> {
        return liveApi.joinRoom(JoinRoomReqVo(roomId, this.selfId, role.value))
            .flatMap {
                val participantVos = mutableListOf<ParticipantVo>()
                it.participants?.let { ps ->
                    for (p in ps) {
                        if (p.uId != selfId) {
                            participantVos.add(p)
                        }
                    }
                }

                val mode = when (it.mode) {
                    1 -> Mode.Chat
                    2 -> Mode.Audio
                    3 -> Mode.Video
                    else -> Mode.Chat
                }
                val room = Room(
                    roomId, selfId, mode, it.members.toMutableSet(),
                    it.ownerId, it.createTime, role, participantVos
                )
                this@IMLiveManager.room = room
                Flowable.just(room)
            }
    }

    fun createRoom(ids: Set<Long>, mode: Mode): Flowable<Room> {
        return liveApi.createRoom(CreateRoomReqVo(this.selfId, mode.value, ids))
            .flatMap {
                val createMode = when (it.mode) {
                    1 -> Mode.Chat
                    2 -> Mode.Audio
                    3 -> Mode.Video
                    else -> Mode.Chat
                }
                val room = Room(
                    it.id, selfId, createMode, it.members.toMutableSet(),
                    it.ownerId, it.createTime, Role.Broadcaster, it.participantVos
                )
                this@IMLiveManager.room = room
                Flowable.just(room)
            }
    }

    fun leaveRoom() {
        if (room == null) {
            return
        }

        val subscriber = object : BaseSubscriber<Void>() {
            override fun onError(t: Throwable?) {
                super.onError(t)
                destroyRoom()
            }

            override fun onComplete() {
                super.onComplete()
                destroyRoom()
            }

            override fun onNext(t: Void?) {
            }
        }
        if (room!!.ownerId == selfId) {
            liveApi.delRoom(DelRoomVo(room!!.id, selfId))
                .compose(RxTransform.flowableToMain())
                .subscribe(subscriber)
        } else {
            liveApi.refuseJoinRoom(RefuseJoinRoomVo(room!!.id, selfId))
                .compose(RxTransform.flowableToMain())
                .subscribe(subscriber)
        }
    }

    fun onMemberHangup(roomId: String, uId: Long) {
        room?.let {
            if (it.id == roomId) {
                it.onMemberHangup(uId)
            }
        }
    }

    fun onEndCall(roomId: String) {
        room?.let {
            if (it.id == roomId) {
                it.onEndCall()
            }
        }
    }

    fun getRoom(): Room? {
        return room
    }

    fun destroyRoom() {
        room?.destroy()
        room = null
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