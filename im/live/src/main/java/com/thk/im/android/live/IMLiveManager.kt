package com.thk.im.android.live

import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Build
import com.thk.im.android.core.base.BaseSubscriber
import com.thk.im.android.core.base.RxTransform
import com.thk.im.android.live.api.vo.CallRoomMemberReqVo
import com.thk.im.android.live.api.vo.CreateRoomReqVo
import com.thk.im.android.live.api.vo.DelRoomVo
import com.thk.im.android.live.api.vo.InviteMemberReqVo
import com.thk.im.android.live.api.vo.JoinRoomReqVo
import com.thk.im.android.live.api.vo.RefuseJoinRoomVo
import com.thk.im.android.live.room.PCFactoryWrapper
import com.thk.im.android.live.room.RTCRoom
import com.thk.im.android.live.signal.LiveSignal
import com.thk.im.android.live.signal.LiveSignalProtocol
import io.reactivex.Flowable
import io.reactivex.disposables.CompositeDisposable
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
    var liveSignalProtocol: LiveSignalProtocol? = null
    lateinit var liveApi: LiveApi
    private var pcFactoryWrapper: PCFactoryWrapper? = null
    private var rtcRoom: RTCRoom? = null
    private var disposes = CompositeDisposable()

    fun init(app: Application) {
        this.app = app
        PeerConnectionFactory.initialize(
            PeerConnectionFactory.InitializationOptions.builder(app).createInitializationOptions()
        )
        muteSpeaker(false)
    }


    fun joinRoom(roomId: String, role: Role): Flowable<RTCRoom> {
        destroyRoom()
        return liveApi.joinRoom(JoinRoomReqVo(roomId, this.selfId, role.value)).flatMap {
            val participantVos = mutableListOf<ParticipantVo>()
            it.participants?.let { ps ->
                for (p in ps) {
                    if (p.uId != selfId) {
                        participantVos.add(p)
                    }
                }
            }
            val rtcRoom = RTCRoom(
                roomId, selfId, it.mode, it.ownerId, it.createTime, role, participantVos
            )
            this@IMLiveManager.rtcRoom = rtcRoom
            Flowable.just(rtcRoom)
        }
    }

    fun createRoom(mode: Mode): Flowable<RTCRoom> {
        destroyRoom()
        return liveApi.createRoom(CreateRoomReqVo(this.selfId, mode.value)).flatMap {
            val rtcRoom = RTCRoom(
                it.id,
                selfId,
                it.mode,
                it.ownerId,
                it.createTime,
                Role.Broadcaster,
                it.participantVos
            )
            this@IMLiveManager.rtcRoom = rtcRoom
            Flowable.just(rtcRoom)
        }
    }

    fun callRoomMember(msg: String, duration: Long, members: Set<Long>): Flowable<Void>? {
        val room = this.rtcRoom ?: return null
        val req = CallRoomMemberReqVo(room.id, selfId, duration, msg, members)
        return liveApi.callRoomMember(req)
    }

    fun inviteMember(uIds: Set<Long>, msg: String, duration: Long): Flowable<Void>? {
        val room = this.rtcRoom ?: return null
        val req = InviteMemberReqVo(room.id, selfId, uIds, duration, msg)
        return liveApi.inviteMember(req)
    }

    fun refuseToJoinRoom(roomId: String, msg: String) {
        val subscriber = object : BaseSubscriber<Void>() {
            override fun onNext(t: Void?) {
            }
        }
        val req = RefuseJoinRoomVo(roomId, selfId, msg)
        liveApi.refuseJoinRoom(req).compose(RxTransform.flowableToMain()).subscribe(subscriber)
        disposes.add(subscriber)
    }

    fun leaveRoom() {
        val room = rtcRoom ?: return
        if (room.ownerId == selfId) {
            val subscriber = object : BaseSubscriber<Void>() {
                override fun onNext(t: Void?) {
                }
            }
            liveApi.delRoom(DelRoomVo(room.id, selfId)).compose(RxTransform.flowableToMain())
                .subscribe(subscriber)
            disposes.add(subscriber)
        }
        destroyRoom()
    }

    fun getRoom(): RTCRoom? {
        return rtcRoom
    }

    fun destroyRoom() {
        rtcRoom?.destroy()
        rtcRoom = null
        disposes.clear()
    }

    fun onLiveSignalReceived(signal: LiveSignal) {
        val delegate = this.liveSignalProtocol ?: return
        delegate.onSignalReceived(signal)
    }

    fun getPCFactoryWrapper(): PCFactoryWrapper {
        synchronized(this) {
            if (this.pcFactoryWrapper == null) {
                val eglBase = EglBase.create()
                val eglBaseContext = eglBase.eglBaseContext
                val options = PeerConnectionFactory.Options()
                val encoderFactory = DefaultVideoEncoderFactory(eglBaseContext, true, true)
                val decoderFactory = DefaultVideoDecoderFactory(eglBaseContext)
                val audioDeviceModule =
                    JavaAudioDeviceModule.builder(this.app).setSamplesReadyCallback { }
                        .createAudioDeviceModule()
                val peerConnectionFactory = PeerConnectionFactory.builder().setOptions(options)
                    .setVideoEncoderFactory(encoderFactory).setVideoDecoderFactory(decoderFactory)
                    .setAudioDeviceModule(audioDeviceModule).createPeerConnectionFactory()
                audioDeviceModule.setSpeakerMute(false)
                audioDeviceModule.setMicrophoneMute(false)
                this.pcFactoryWrapper =
                    PCFactoryWrapper(peerConnectionFactory, eglBaseContext, audioDeviceModule)
            }
            return this.pcFactoryWrapper!!
        }
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

}