package com.thk.im.android.live.room

import com.google.gson.Gson
import com.thk.im.android.live.ParticipantVo
import com.thk.im.android.live.Role
import com.thk.im.android.live.RoomMode
import com.thk.im.android.live.VolumeMsg
import com.thk.im.android.live.VolumeMsgType
import com.thk.im.android.live.api.vo.MediaParams
import com.thk.im.android.live.engine.LiveRTCEngine
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
    var callback: RTCRoomCallBack? = null
    private var localParticipant: LocalParticipant? = null
    private var remoteParticipants = mutableListOf<RemoteParticipant>()

    init {
        initLocalParticipant(role)
        initRemoteParticipant(participantVos)
    }

    fun audioEnable(): Boolean {
        return this.mode >= RoomMode.Audio.value
    }

    fun videoEnable(): Boolean {
        return this.mode == RoomMode.Video.value || this.mode == RoomMode.VideoRoom.value
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

    fun getAllParticipants(): List<BaseParticipant> {
        val array = mutableListOf<BaseParticipant>()
        localParticipant?.let {
            array.add(it)
        }
        array.addAll(remoteParticipants)
        return array
    }

    fun remoteParticipants(): List<RemoteParticipant> {
        return remoteParticipants
    }

    fun localParticipant(): LocalParticipant? {
        return localParticipant
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
        callback?.onParticipantJoin(p)
    }

    private fun notifyLeave(p: BaseParticipant) {
        callback?.onParticipantLeave(p)
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
//        localParticipant?.sendMyVolume(volume)
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
            callback?.onParticipantVoice(volumeMsg.uId, volumeMsg.volume)
        }
        callback?.onTextMsgReceived(type, text)
    }

    fun receivedDcMsg(bb: ByteBuffer) {
        callback?.onDataMsgReceived(bb)
    }

    fun onConnectStatusChange(uId: Long, status: Int) {
        callback?.onConnectStatus(uId, status)
    }

    /**
     * 扬声器是否打开
     */
    fun isSpeakerMuted(): Boolean {
        return LiveRTCEngine.shared().isSpeakerMuted()
    }

    /**
     * 打开/关闭扬声器
     */
    fun muteSpeaker(mute: Boolean) {
        LiveRTCEngine.shared().muteSpeaker(mute)
    }

    /**
     * 获取本地摄像头: 0 未知, 1 后置, 2 前置
     */
    fun currentLocalCamera(): Int {
        return localParticipant?.currentCamera() ?: 0
    }

    /**
     * 切换本地摄像头
     */
    fun switchLocalCamera() {
        localParticipant?.switchCamera()
    }

    /**
     * 打开本地视频
     */
    fun muteLocalVideo(mute: Boolean) {
        localParticipant?.setVideoMuted(mute)
    }

    /**
     * 本地视频是否打开
     */
    fun isLocalVideoMuted(): Boolean {
        return localParticipant?.getVideoMuted() ?: false
    }

    /**
     * 打开/关闭本地音频
     */
    fun muteLocalAudio(mute: Boolean) {
        localParticipant?.setAudioMuted(mute)
    }

    /**
     * 本地音频是否关闭
     */
    fun isLocalAudioMuted(): Boolean {
        return localParticipant?.getAudioMuted() ?: false
    }

    /**
     * 打开/关闭远端音频
     */
    fun muteRemoteAudio(uId: Long, mute: Boolean) {
        for (p in remoteParticipants) {
            if (p.uId == uId) {
                p.setAudioMuted(mute)
            }
        }
    }

    /**
     * 打开/关闭远端音频
     */
    fun muteAllRemoteAudio(mute: Boolean) {
        for (p in remoteParticipants) {
            p.setAudioMuted(mute)
        }
    }

    /**
     * 远端音频是否关闭
     */
    fun isRemoteAudioMuted(uId: Long): Boolean {
        for (p in remoteParticipants) {
            if (p.uId == uId) {
                return p.getAudioMuted()
            }
        }
        return true
    }

    /**
     * 打开/关闭远端视频
     */
    fun muteRemoteVideo(uId: Long, mute: Boolean) {
        for (p in remoteParticipants) {
            if (p.uId == uId) {
                p.setAudioMuted(mute)
            }
        }
    }

    /**
     * 打开/关闭远端音频
     */
    fun muteAllRemoteVideo(mute: Boolean) {
        for (p in remoteParticipants) {
            p.setVideoMuted(mute)
        }
    }

    /**
     * 远端视频是否关闭
     */
    fun isRemoteVideoMuted(uId: Long): Boolean {
        for (p in remoteParticipants) {
            if (p.uId == uId) {
                return p.getVideoMuted()
            }
        }
        return true
    }

    /**
     * 销毁房间
     */
    fun destroy() {
        for (p in remoteParticipants) {
            p.leave()
        }
        callback = null
        remoteParticipants.clear()
        localParticipant?.leave()
        localParticipant = null
    }

}