package com.thk.im.android.live.room

import com.thk.im.android.core.base.BaseSubscriber
import com.thk.im.android.core.base.RxTransform
import com.thk.im.android.live.LiveApi
import com.thk.im.android.live.Mode
import com.thk.im.android.live.ParticipantVo
import com.thk.im.android.live.Role
import com.thk.im.android.live.api.vo.CallRoomMemberReqVo
import com.thk.im.android.live.api.vo.CreateRoomReqVo
import com.thk.im.android.live.api.vo.DelRoomVo
import com.thk.im.android.live.api.vo.InviteMemberReqVo
import com.thk.im.android.live.api.vo.JoinRoomReqVo
import com.thk.im.android.live.api.vo.MediaParams
import com.thk.im.android.live.api.vo.RefuseJoinRoomVo
import io.reactivex.Flowable
import io.reactivex.disposables.CompositeDisposable

class RTCRoomManager private constructor() {

    companion object {
        private var shareManager: RTCRoomManager? = null

        @Synchronized
        fun shared(): RTCRoomManager {
            if (shareManager == null) {
                shareManager = RTCRoomManager()
            }
            return shareManager as RTCRoomManager
        }
    }

    var myUId: Long = 0L
    lateinit var liveApi: LiveApi
    private var rtcRoom: RTCRoom? = null
    private var disposes = CompositeDisposable()

    fun currentRoom(): RTCRoom? {
        return rtcRoom
    }

    fun joinRoom(roomId: String, role: Int): Flowable<RTCRoom> {
        destroyRoom()
        return liveApi.joinRoom(JoinRoomReqVo(roomId, this.myUId, role)).flatMap {
            val participantVos = mutableListOf<ParticipantVo>()
            it.participants?.let { ps ->
                for (p in ps) {
                    if (p.uId != myUId) {
                        participantVos.add(p)
                    }
                }
            }
            val rtcRoom = RTCRoom(
                roomId, it.mode, it.ownerId, it.createTime, role,
                it.mediaParams, participantVos
            )
            this.rtcRoom = rtcRoom
            Flowable.just(rtcRoom)
        }
    }

    /**
     * 创建房间
     */
    fun createRoom(mode: Mode, mediaParams: MediaParams): Flowable<RTCRoom> {
        destroyRoom()
        val req = CreateRoomReqVo(
            myUId, mode.value,
            mediaParams.videoMaxBitrate, mediaParams.audioMaxBitrate,
            mediaParams.videoWidth, mediaParams.videoHeight, mediaParams.videoFps,
        )
        return liveApi.createRoom(req).flatMap {
            val rtcRoom = RTCRoom(
                it.id, it.mode, it.ownerId, it.createTime, Role.Broadcaster.value,
                it.mediaParams, it.participantVos
            )
            this.rtcRoom = rtcRoom
            Flowable.just(rtcRoom)
        }
    }

    /**
     * 向房间成员发送呼叫
     */
    fun callRoomMember(msg: String, duration: Long, members: Set<Long>): Flowable<Void>? {
        val room = this.rtcRoom ?: return null
        val req = CallRoomMemberReqVo(room.id, myUId, duration, msg, members)
        return liveApi.callRoomMember(req)
    }

    /**
     * 邀请新成员
     */
    fun inviteNewMembers(uIds: Set<Long>, msg: String, duration: Long): Flowable<Void>? {
        val room = this.rtcRoom ?: return null
        val req = InviteMemberReqVo(room.id, myUId, uIds, duration, msg)
        return liveApi.inviteMember(req)
    }

    /**
     * 拒绝加入房间(拒绝电话)
     */
    fun refuseToJoinRoom(roomId: String, msg: String) {
        val subscriber = object : BaseSubscriber<Void>() {
            override fun onNext(t: Void?) {
            }
        }
        val req = RefuseJoinRoomVo(roomId, myUId, msg)
        liveApi.refuseJoinRoom(req).compose(RxTransform.flowableToMain()).subscribe(subscriber)
        disposes.add(subscriber)
    }

    /**
     * 离开房间
     */
    fun leaveRoom() {
        rtcRoom?.destroy()
        rtcRoom = null
        disposes.clear()
    }

    /**
     * 销毁房间
     */
    fun destroyRoom() {
        val room = rtcRoom ?: return
        if (room.ownerId == myUId) {
            val subscriber = object : BaseSubscriber<Void>() {
                override fun onNext(t: Void?) {
                }
            }
            liveApi.delRoom(DelRoomVo(room.id, myUId)).compose(RxTransform.flowableToMain())
                .subscribe(subscriber)
            disposes.add(subscriber)
        }
        leaveRoom()
    }


}