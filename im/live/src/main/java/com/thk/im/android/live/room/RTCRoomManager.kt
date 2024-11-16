package com.thk.im.android.live.room

import com.thk.im.android.core.base.BaseSubscriber
import com.thk.im.android.core.base.RxTransform
import com.thk.im.android.live.LiveApi
import com.thk.im.android.live.Mode
import com.thk.im.android.live.ParticipantVo
import com.thk.im.android.live.Role
import com.thk.im.android.live.api.vo.CallRoomMemberReqVo
import com.thk.im.android.live.api.vo.CancelCallRoomMemberReqVo
import com.thk.im.android.live.api.vo.CreateRoomReqVo
import com.thk.im.android.live.api.vo.DelRoomVo
import com.thk.im.android.live.api.vo.InviteMemberReqVo
import com.thk.im.android.live.api.vo.JoinRoomReqVo
import com.thk.im.android.live.api.vo.KickoffMemberReqVo
import com.thk.im.android.live.api.vo.LeaveRoomReqVo
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
    private var rtcRooms = mutableListOf<RTCRoom>()
    private var disposes = CompositeDisposable()

    fun allRooms(): List<RTCRoom> {
        return rtcRooms
    }

    fun addRoom(room: RTCRoom) {
        rtcRooms.add(room)
    }

    fun getRoomById(id: String): RTCRoom? {
        return rtcRooms.firstOrNull {
            it.id == id
        }
    }

    /**
     * 创建房间
     */
    fun createRoom(mode: Mode, mediaParams: MediaParams): Flowable<RTCRoom> {
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
            Flowable.just(rtcRoom)
        }
    }

    /**
     * 查询房间
     */
    fun queryRoom(id: String): Flowable<RTCRoom> {
        return liveApi.queryRoom(id).flatMap {
            val rtcRoom = RTCRoom(
                it.id, it.mode, it.ownerId, it.createTime, Role.Broadcaster.value,
                it.mediaParams, it.participantVos
            )
            Flowable.just(rtcRoom)
        }
    }

    /**
     * 加入房间
     */
    fun joinRoom(roomId: String, role: Int): Flowable<RTCRoom> {
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
            Flowable.just(rtcRoom)
        }
    }

    /**
     * 向房间成员发送呼叫
     */
    fun callRoomMembers(
        id: String,
        msg: String,
        duration: Long,
        members: Set<Long>
    ): Flowable<Void> {
        val req = CallRoomMemberReqVo(id, myUId, duration, msg, members)
        return liveApi.callRoomMember(req)
    }

    /**
     * 取消向房间成员发送呼叫
     */
    fun cancelCallRoomMembers(
        id: String,
        msg: String,
        members: Set<Long>
    ): Flowable<Void> {
        val req = CancelCallRoomMemberReqVo(id, myUId, msg, members)
        return liveApi.cancelCallRoomMember(req)
    }

    /**
     * 拒绝加入房间(拒绝电话)
     */
    fun refuseToJoinRoom(id: String, msg: String): Flowable<Void> {
        val req = RefuseJoinRoomVo(id, myUId, msg)
        return liveApi.refuseJoinRoom(req)
    }

    /**
     * 邀请新成员
     */
    fun inviteNewMembers(
        id: String,
        uIds: Set<Long>,
        msg: String,
        duration: Long
    ): Flowable<Void> {
        val req = InviteMemberReqVo(id, myUId, uIds, duration, msg)
        return liveApi.inviteMember(req)
    }

    /**
     * 踢出成员
     */
    fun kickoffMembers(
        id: String,
        uIds: Set<Long>,
        msg: String
    ): Flowable<Void> {
        val req = KickoffMemberReqVo(id, myUId, msg, uIds)
        return liveApi.kickRoomMember(req)
    }

    /**
     * 离开房间, 如果是房主，在删除房间
     */
    fun leaveRoom(id: String, delRoom: Boolean) {
        val room = getRoomById(id) ?: return
        rtcRooms.removeAll {
            it.id == id
        }
        room.destroy()
        val subscriber = object : BaseSubscriber<Void>() {
            override fun onNext(t: Void?) {

            }
        }
        if (room.ownerId == myUId && delRoom) {
            liveApi.delRoom(DelRoomVo(id, myUId)).compose(RxTransform.flowableToMain())
                .subscribe(subscriber)
            disposes.add(subscriber)
        } else {
            liveApi.leaveRoom(LeaveRoomReqVo(id, myUId, "")).compose(RxTransform.flowableToMain())
                .subscribe(subscriber)
            disposes.add(subscriber)
        }
    }

}