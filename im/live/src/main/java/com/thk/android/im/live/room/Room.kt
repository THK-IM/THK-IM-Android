package com.thk.android.im.live.room

import com.thk.android.im.live.LiveManager
import com.thk.im.android.base.LLog
import java.nio.ByteBuffer

class Room(
    val id: String,
    val uid: String,
    private val mode: Mode,
    role: Role,
    members: List<Member>?
) {
    private val observers: MutableList<RoomObserver> = ArrayList()
    private var localParticipant: LocalParticipant? = null
    private var remoteParticipants: MutableList<RemoteParticipant> = ArrayList()

    init {
        initLocalParticipant(role)
        initRemoteParticipant(members)
    }


    private fun initLocalParticipant(role: Role) {
        LiveManager.shared().selfId?.let {
            localParticipant = if (role == Role.Broadcaster) {
                LocalParticipant(
                    it,
                    id,
                    role,
                    audioEnable = mode.value >= Mode.Audio.value,
                    videoEnable = mode.value == Mode.Video.value
                )
            } else {
                LocalParticipant(
                    it, id, role, audioEnable = false, videoEnable = false
                )
            }
        }
    }

    private fun initRemoteParticipant(members: List<Member>?) {
        members?.let { ms ->
            for (m in ms) {
                val remoteParticipant = RemoteParticipant(m.uid, id, m.streamKey)
                this.remoteParticipants.add(remoteParticipant)
            }
        }
    }

    fun participantJoin(p: BaseParticipant) {
        LLog.d("participantJoin ${p.uid}")
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
            if (lp.getRole() != role) {
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
        return this.localParticipant?.getRole()
    }

    fun registerObserver(observer: RoomObserver) {
        observers.add(observer)
    }

    fun unRegisterObserver(observer: RoomObserver) {
        observers.remove(observer)
    }

    private fun notifyJoin(p: BaseParticipant) {
        LLog.d("notifyJoin, ${p.uid}")
        for (o in observers) {
            o.join(p)
        }
    }

    private fun notifyLeave(p: BaseParticipant) {
        LLog.d("notifyLeave, ${p.uid}")
        for (o in observers) {
            o.leave(p)
        }
    }

    fun sendMessage(text: String): Boolean {
        return if (localParticipant != null) {
            val success = localParticipant!!.sendMessage(text)
            if (success) {
                receivedDcMsg(uid, text)
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

    fun receivedDcMsg(uid: String, text: String) {
        observers.forEach {
            it.onTextMsgReceived(uid, text)
        }
    }

    fun receivedDcMsg(bb: ByteBuffer) {
        observers.forEach {
            it.onBufferMsgReceived(bb)
        }
    }

}