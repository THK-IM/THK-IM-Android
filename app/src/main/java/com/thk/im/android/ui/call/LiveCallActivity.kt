package com.thk.im.android.ui.call

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import com.thk.android.im.live.IMLiveManager
import com.thk.android.im.live.RoomObserver
import com.thk.android.im.live.room.BaseParticipant
import com.thk.android.im.live.room.LocalParticipant
import com.thk.android.im.live.room.RemoteParticipant
import com.thk.android.im.live.room.Room
import com.thk.im.android.core.IMCoreManager
import com.thk.im.android.core.base.BaseSubscriber
import com.thk.im.android.core.base.LLog
import com.thk.im.android.core.base.RxTransform
import com.thk.im.android.core.db.entity.User
import com.thk.im.android.databinding.ActvitiyLiveCallBinding
import com.thk.im.android.ui.base.BaseActivity
import java.nio.ByteBuffer

class LiveCallActivity : BaseActivity(), RoomObserver, LiveCallProtocol {

    companion object {
        fun startCallActivity(ctx: Context) {
            val intent = Intent(ctx, LiveCallActivity::class.java)
            ctx.startActivity(intent)
        }
    }

    private lateinit var binding: ActvitiyLiveCallBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActvitiyLiveCallBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        val room = IMLiveManager.shared().getRoom() ?: return
        room.registerObserver(this)
        initUserInfo(room)

        binding.llRequestCall.initCall(this)
        binding.llCalling.initCall(this)
        binding.llBeCalling.initCall(this)

        checkPermission()
    }

    private fun initUserInfo(room: Room) {
        room.members.forEach {
            if (it != IMLiveManager.shared().selfId) {
                val subscriber = object : BaseSubscriber<User>() {
                    override fun onNext(t: User?) {
                        t?.let { user ->
                            binding.llCallingInfo.setUserInfo(user)
                        }
                    }

                }
                IMCoreManager.userModule.queryUser(it)
                    .compose(RxTransform.flowableToMain())
                    .subscribe(subscriber)
                addDispose(subscriber)
            }
        }
    }

    private fun checkPermission() {
        XXPermissions.with(this).permission(Permission.CAMERA, Permission.RECORD_AUDIO)
            .request(object : OnPermissionCallback {
                override fun onGranted(permissions: MutableList<String>, allGranted: Boolean) {
                    initRoom()
                }

                override fun onDenied(permissions: MutableList<String>, doNotAskAgain: Boolean) {
                    super.onDenied(permissions, doNotAskAgain)
                }
            })
    }

    private fun initRoom() {
        val room = IMLiveManager.shared().getRoom()
        room?.let {
            var remoteParticipantCnt = 0
            it.getAllParticipants().forEach { p ->
                initParticipant(p)
                if (p is RemoteParticipant) {
                    remoteParticipantCnt ++
                }
            }
            if (remoteParticipantCnt > 0) {
                showCallingView()
            } else {
                if (it.ownerId == IMLiveManager.shared().selfId) {
                    showRequestCallView()
                } else {
                    showBeCallingView()
                }
            }
        }
    }

    private fun showBeCallingView() {
        binding.llRequestCall.visibility = View.GONE
        binding.llCalling.visibility = View.GONE
        binding.llBeCalling.visibility = View.VISIBLE
    }

    private fun showRequestCallView() {
        binding.llRequestCall.visibility = View.VISIBLE
        binding.llCalling.visibility = View.GONE
        binding.llBeCalling.visibility = View.GONE
    }

    private fun showCallingView() {
        binding.llRequestCall.visibility = View.GONE
        binding.llCalling.visibility = View.VISIBLE
        binding.llBeCalling.visibility = View.GONE
    }

    override fun onHangup(uId: Long) {
        IMLiveManager.shared().leaveRoom()
        finish()
    }

    override fun onEndCall() {
        IMLiveManager.shared().leaveRoom()
        finish()
    }

    private fun initParticipant(p: BaseParticipant) {
        if (p is RemoteParticipant) {
            binding.participantLocal.setFullscreenMode(false)
            binding.participantRemote.setParticipant(p)
            binding.participantRemote.setFullscreenMode(true)
            binding.participantRemote.startPeerConnection()
        } else {
            binding.participantLocal.setParticipant(p)
            binding.participantLocal.setFullscreenMode(true)
            val room = IMLiveManager.shared().getRoom() ?: return
            if (room.ownerId == IMLiveManager.shared().selfId) {
                binding.participantLocal.startPeerConnection()
            }
        }
    }

    override fun join(p: BaseParticipant) {
        LLog.v("LiveCallActivity join")
        showCallingView()
        initParticipant(p)
    }

    override fun leave(p: BaseParticipant) {
    }

    override fun onTextMsgReceived(uId: Long, text: String) {
    }

    override fun onBufferMsgReceived(bb: ByteBuffer) {
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.participantLocal.destroy()
        binding.participantRemote.destroy()
        IMLiveManager.shared().getRoom()?.destroy()
    }

    override fun currentLocalCamera(): Int {
        val room = IMLiveManager.shared().getRoom()
        if (room != null) {
            val participants = room.getAllParticipants()
            for (p in participants) {
                if (p is LocalParticipant) {
                    return p.currentCamera()
                }
            }
        }
        return 0
    }

    override fun isCurrentCameraOpened(): Boolean {
        val room = IMLiveManager.shared().getRoom()
        if (room != null) {
            val participants = room.getAllParticipants()
            for (p in participants) {
                if (p is LocalParticipant) {
                    return !p.getVideoMuted()
                }
            }
        }
        return false
    }

    override fun switchLocalCamera() {
        val room = IMLiveManager.shared().getRoom()
        room?.let {
            it.getAllParticipants().forEach { p ->
                if (p is LocalParticipant) {
                    p.switchCamera()
                }
            }
        }
    }

    override fun openLocalCamera() {
        val room = IMLiveManager.shared().getRoom()
        room?.let {
            it.getAllParticipants().forEach { p ->
                if (p is LocalParticipant) {
                    p.setVideoMuted(false)
                }
            }
        }
    }

    override fun closeLocalCamera() {
        val room = IMLiveManager.shared().getRoom()
        room?.let {
            it.getAllParticipants().forEach { p ->
                if (p is LocalParticipant) {
                    p.setVideoMuted(true)
                }
            }
        }
    }

    override fun openRemoteVideo(user: User) {
        val room = IMLiveManager.shared().getRoom()
        room?.let {
            it.getAllParticipants().forEach { p ->
                if (p is RemoteParticipant) {
                    if (user.id == p.uId) {
                        p.setVideoMuted(false)
                    }
                }
            }
        }
    }

    override fun closeRemoteVideo(user: User) {
        val room = IMLiveManager.shared().getRoom()
        room?.let {
            it.getAllParticipants().forEach { p ->
                if (p is RemoteParticipant) {
                    if (user.id == p.uId) {
                        p.setVideoMuted(true)
                    }
                }
            }
        }
    }

    override fun openRemoteAudio(user: User) {
        val room = IMLiveManager.shared().getRoom()
        room?.let {
            it.getAllParticipants().forEach { p ->
                if (p is RemoteParticipant) {
                    if (user.id == p.uId) {
                        p.setAudioMuted(false)
                    }
                }
            }
        }
    }

    override fun closeRemoteAudio(user: User) {
        val room = IMLiveManager.shared().getRoom()
        room?.let {
            it.getAllParticipants().forEach { p ->
                if (p is RemoteParticipant) {
                    if (user.id == p.uId) {
                        p.setAudioMuted(true)
                    }
                }
            }
        }
    }

    override fun accept() {
        val room = IMLiveManager.shared().getRoom()
        room?.let {
            showCallingView()
            binding.participantLocal.startPeerConnection()
            it.getAllParticipants().forEach { p ->
                if (p is RemoteParticipant) {
                    initParticipant(p)
                }
            }
        }
    }

    override fun hangup() {
        IMLiveManager.shared().leaveRoom()
        finish()
    }

}