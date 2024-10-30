package com.thk.im.android.live

import android.app.Application
import com.thk.im.android.core.base.BaseSubscriber
import com.thk.im.android.core.base.RxTransform
import com.thk.im.android.live.api.vo.CallRoomMemberReqVo
import com.thk.im.android.live.api.vo.CreateRoomReqVo
import com.thk.im.android.live.api.vo.DelRoomVo
import com.thk.im.android.live.api.vo.InviteMemberReqVo
import com.thk.im.android.live.api.vo.JoinRoomReqVo
import com.thk.im.android.live.api.vo.RefuseJoinRoomVo
import com.thk.im.android.live.room.RTCRoom
import com.thk.im.android.live.signal.LiveSignal
import com.thk.im.android.live.signal.LiveSignalProtocol
import io.reactivex.Flowable
import io.reactivex.disposables.CompositeDisposable
import org.webrtc.PeerConnectionFactory


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
    private var rtcRoom: RTCRoom? = null
    private var disposes = CompositeDisposable()

    fun init(app: Application) {
        this.app = app
    }


    fun joinRoom(roomId: String, role: Int): Flowable<RTCRoom> {
        destroyRoom()
        return liveApi.joinRoom(JoinRoomReqVo(roomId, this.selfId, role)).flatMap {
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
                Role.Broadcaster.value,
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

}