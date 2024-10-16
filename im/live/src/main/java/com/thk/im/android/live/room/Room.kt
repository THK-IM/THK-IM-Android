package com.thk.im.android.live.room

import com.thk.im.android.core.base.LLog
import com.thk.im.android.live.IMLiveManager
import com.thk.im.android.live.Mode
import com.thk.im.android.live.ParticipantVo
import com.thk.im.android.live.Role
import com.thk.im.android.live.RoomObserver
import java.nio.ByteBuffer

class Room(
    val id: String,
    val uId: Long,
    val mode: Mode,
    val members: MutableSet<Long>, // 房间内成员
    val ownerId: Long,
    val createTime: Long,
    role: Role,
    participantVos: List<ParticipantVo>? // 当前参与人
) {
    private val observers: MutableList<RoomObserver> = ArrayList()
    private var localParticipant: LocalParticipant? = null
    private var remoteParticipants: MutableList<RemoteParticipant> = ArrayList()

    init {
        initLocalParticipant(role)
        initRemoteParticipant(participantVos)
    }


    private fun initLocalParticipant(role: Role) {
        val selfId = IMLiveManager.shared().selfId
        localParticipant = if (role == Role.Broadcaster) {
            LocalParticipant(
                selfId,
                id,
                role,
                audioEnable = mode == Mode.Audio || mode == Mode.Video,
                videoEnable = mode == Mode.Video
            )
        } else {
            LocalParticipant(
                selfId, id, role, audioEnable = false, videoEnable = false
            )
        }
    }

    private fun initRemoteParticipant(participantVos: List<ParticipantVo>?) {
        participantVos?.let { vos ->
            for (vo in vos) {
                val role = when (vo.role) {
                    Role.Broadcaster.value -> Role.Broadcaster
                    else -> {
                        Role.Audience
                    }
                }
                if (vo.uId != uId) {
                    val audioEnable = mode == Mode.Audio || mode == Mode.Video
                    val videoEnable = mode == Mode.Video
                    val remoteParticipant =
                        RemoteParticipant(vo.uId, id, role, vo.streamKey, audioEnable, videoEnable)
                    this.remoteParticipants.add(remoteParticipant)
                }
            }
        }
    }

    fun participantJoin(p: BaseParticipant) {
        LLog.d("participantJoin ${p.uId}")
        if (p is RemoteParticipant) {
            if (!remoteParticipants.contains(p)) {
                remoteParticipants.add(p)
            }
            notifyJoin(p)
        } else if (p is LocalParticipant) {
            if (this.localParticipant != p) {
                this.localParticipant = p
            }
            notifyJoin(p)
        }
    }

    fun participantLeave(roomId: String, streamKey: String) {
        if (roomId == this.id) {
            var p: BaseParticipant? = null
            localParticipant?.let {
                if (it.pushStreamKey() == streamKey) {
                    p = it
                }
            }
            if (p == null) {
                for (rp in remoteParticipants) {
                    if (rp.pushStreamKey() == streamKey) {
                        p = rp
                        break
                    }
                }
            }

            p?.let {
                it.leave()
                onParticipantLeave(it)
            }
        }
    }

    fun onParticipantLeave(p: BaseParticipant) {
        if (p is LocalParticipant) {
            this.onLocalParticipantLeave(p)
        } else if (p is RemoteParticipant) {
            this.onRemoteParticipantLeave(p)
        }
    }

    private fun onLocalParticipantLeave(p: LocalParticipant) {
        if (localParticipant == p) {
            localParticipant = null
        }
        notifyLeave(p)
    }

    private fun onRemoteParticipantLeave(p: RemoteParticipant) {
        if (remoteParticipants.contains(p)) {
            remoteParticipants.remove(p)
        }
        notifyLeave(p)
    }

    fun destroy() {
        LLog.d("room destroy")
        for (p in remoteParticipants) {
            p.leave()
        }
        observers.clear()
        remoteParticipants.clear()
        localParticipant?.leave()
        localParticipant = null
    }

    fun getAllParticipants(): List<BaseParticipant> {
        val array = mutableListOf<BaseParticipant>()
        localParticipant?.let {
            array.add(it)
        }
        array.addAll(remoteParticipants)
        return array
    }

    fun setRole(role: Role) {
        LLog.v("setRole: $role")
        if (localParticipant != null) {
            val lp = localParticipant!!
            if (lp.role != role) {
                lp.onDisConnected()
                lp.leave()
                initLocalParticipant(role)
                localParticipant?.let {
                    participantJoin(it)
                }
            } else {
                return
            }
        }
        initLocalParticipant(role)
        localParticipant?.let {
            participantJoin(it)
        }
    }

    fun getRole(): Role? {
        return this.localParticipant?.role
    }

    fun registerObserver(observer: RoomObserver) {
        observers.add(observer)
    }

    fun unRegisterObserver(observer: RoomObserver) {
        observers.remove(observer)
    }

    private fun notifyJoin(p: BaseParticipant) {
        LLog.d("notifyJoin, ${p.uId}")
        for (o in observers) {
            o.join(p)
        }
    }

    private fun notifyLeave(p: BaseParticipant) {
        LLog.d("notifyLeave, ${p.uId}")
        for (o in observers) {
            o.leave(p)
        }
    }

    fun sendMessage(text: String): Boolean {
        return if (localParticipant != null) {
            val success = localParticipant!!.sendMessage(text)
            if (success) {
                receivedDcMsg(uId, text)
            }
            success
        } else {
            false
        }
    }

    fun sendBytes(ba: ByteArray): Boolean {
        return if (localParticipant != null) {
            localParticipant!!.sendBytes(ba)
        } else {
            false
        }
    }

    fun sendByteBuffer(bb: ByteBuffer): Boolean {
        return if (localParticipant != null) {
            localParticipant!!.sendByteBuffer(bb)
        } else {
            false
        }
    }

    fun receivedDcMsg(uId: Long, text: String) {
        observers.forEach {
            it.onTextMsgReceived(uId, text)
        }
    }

    fun receivedDcMsg(bb: ByteBuffer) {
        observers.forEach {
            it.onBufferMsgReceived(bb)
        }
    }

    fun switchCamera() {
        localParticipant?.switchCamera()
    }

    fun onMemberHangup(uId: Long) {
        for (o in observers) {
            o.onHangup(uId)
        }
    }

    fun onEndCall() {
        for (o in observers) {
            o.onEndCall()
        }

    }

}