package com.thk.im.android.live.room

import com.google.gson.Gson
import com.thk.im.android.live.Mode
import com.thk.im.android.live.ParticipantVo
import com.thk.im.android.live.Role
import com.thk.im.android.live.VolumeMsg
import com.thk.im.android.live.VolumeMsgType
import com.thk.im.android.live.api.vo.MediaParams
import java.nio.ByteBuffer

class RTCRoom(
    val id: String,
    val mode: Int,
    val ownerId: Long,
    val createTime: Long,
    private val role: Int,
    private val mediaParams: MediaParams,
    participantVos: List<ParticipantVo>? // 当前参与人
) {
    var delegate: RTCRoomProtocol? = null
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
        val selfId = RTCRoomManager.shared().myUId
        localParticipant = if (role == Role.Broadcaster.value) {
            LocalParticipant(
                selfId, id, role, mediaParams, audioEnable(), videoEnable()
            )
        } else {
            LocalParticipant(
                selfId, id, role, mediaParams, audioEnable = false, videoEnable = false
            )
        }
    }

    private fun initRemoteParticipant(participantVos: List<ParticipantVo>?) {
        participantVos?.let { vos ->
            for (vo in vos) {
                if (vo.uId != RTCRoomManager.shared().myUId) {
                    val remoteParticipant = RemoteParticipant(
                        vo.uId, id, vo.role, vo.streamKey, audioEnable(), videoEnable()
                    )
                    this.remoteParticipants.add(remoteParticipant)
                }
            }
        }
    }

    fun participantJoin(p: BaseParticipant) {
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
        for (p in remoteParticipants) {
            p.leave()
        }
        delegate = null
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

    fun updateMyRole(role: Int) {
        localParticipant?.let {
            if (it.role != role) {
                it.leave()
                it.onDisConnected()
            } else {
                return
            }
        }
        initLocalParticipant(role)
        localParticipant?.let {
            participantJoin(it)
        }
    }

    fun getMyRole(): Int? {
        return this.localParticipant?.role
    }

    private fun notifyJoin(p: BaseParticipant) {
        delegate?.onParticipantJoin(p)
    }

    private fun notifyLeave(p: BaseParticipant) {
        delegate?.onParticipantLeave(p)
    }

    fun sendMessage(type: Int, text: String): Boolean {
        return if (localParticipant != null) {
            val success = localParticipant!!.sendMessage(type, text)
            if (success) {
                receivedDcMsg(type, text)
            }
            success
        } else {
            false
        }
    }

    fun sendMyVolume(volume: Double) {
        localParticipant?.sendMyVolume(volume)
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

    fun receivedDcMsg(type: Int, text: String) {
        if (type == VolumeMsgType) {
            val volumeMsg = Gson().fromJson(text, VolumeMsg::class.java)
            delegate?.onParticipantVoice(volumeMsg.uId, volumeMsg.volume)
        }
        delegate?.onTextMsgReceived(type, text)
    }

    fun receivedDcMsg(bb: ByteBuffer) {
        delegate?.onDataMsgReceived(bb)
    }

    fun onConnectStatusChange(uId: Long, status: Int) {
        delegate?.onConnectStatus(uId, status)
    }

    fun switchCamera() {
        localParticipant?.switchCamera()
    }

}