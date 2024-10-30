package com.thk.im.android.live.room

import com.thk.im.android.core.base.LLog
import com.thk.im.android.live.IMLiveManager
import com.thk.im.android.live.Mode
import com.thk.im.android.live.ParticipantVo
import com.thk.im.android.live.Role
import java.nio.ByteBuffer

class RTCRoom(
    val id: String,
    val uId: Long,
    val mode: Int,
    val ownerId: Long,
    val createTime: Long,
    role: Int,
    participantVos: List<ParticipantVo>? // 当前参与人
) {
    var rtcRoomProtocol: RTCRoomProtocol? = null
    private var localParticipant: LocalParticipant? = null
    private var remoteParticipants: MutableList<RemoteParticipant> = ArrayList()

    init {
        initLocalParticipant(role)
        initRemoteParticipant(participantVos)
    }

    fun audioEnable(): Boolean {
        return this.mode >= Mode.Audio.value
    }

    fun videoEnable(): Boolean {
        return this.mode == Mode.Video.value || this.mode == Mode.VideoRoom.value
    }

    private fun initLocalParticipant(role: Int) {
        val selfId = IMLiveManager.shared().selfId
        localParticipant = if (role == Role.Broadcaster.value) {
            LocalParticipant(
                selfId,
                id,
                role,
                audioEnable = audioEnable(),
                videoEnable = videoEnable()
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
                if (vo.uId != uId) {
                    val audioEnable = audioEnable()
                    val videoEnable = videoEnable()
                    val remoteParticipant =
                        RemoteParticipant(
                            vo.uId,
                            id,
                            vo.role,
                            vo.streamKey,
                            audioEnable,
                            videoEnable
                        )
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
        rtcRoomProtocol = null
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

    fun setRole(role: Int) {
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

    fun getRole(): Int? {
        return this.localParticipant?.role
    }

    private fun notifyJoin(p: BaseParticipant) {
        rtcRoomProtocol?.onParticipantJoin(p)
    }

    private fun notifyLeave(p: BaseParticipant) {
        rtcRoomProtocol?.onParticipantLeave(p)
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
        rtcRoomProtocol?.onTextMsgReceived(uId, text)
    }

    fun receivedDcMsg(bb: ByteBuffer) {
        rtcRoomProtocol?.onDataMsgReceived(uId, bb)
    }

    fun switchCamera() {
        localParticipant?.switchCamera()
    }

}