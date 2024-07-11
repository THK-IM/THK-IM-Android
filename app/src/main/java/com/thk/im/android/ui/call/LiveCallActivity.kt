package com.thk.im.android.ui.call

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import com.thk.im.android.live.IMLiveManager
import com.thk.im.android.live.RoomObserver
import com.thk.im.android.live.room.BaseParticipant
import com.thk.im.android.live.room.RemoteParticipant
import com.thk.im.android.live.room.Room
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

        binding.participantLocal.setOnClickListener {
            if (!binding.participantLocal.isFullScreen()) {
                binding.participantRemote.bringToFront()
                binding.llCallingInfo.bringToFront()
                binding.llCalling.bringToFront()
                binding.participantRemote.setFullscreenMode(false)
                binding.participantLocal.setFullscreenMode(true)
            }
        }

        binding.participantRemote.setOnClickListener {
            LLog.d("onClick: participantRemote")
            if (!binding.participantRemote.isFullScreen()) {
                binding.participantLocal.bringToFront()
                binding.llCallingInfo.bringToFront()
                binding.llCalling.bringToFront()
                binding.participantLocal.setFullscreenMode(false)
                binding.participantRemote.setFullscreenMode(true)
            }
        }

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
                IMCoreManager.userModule.queryUser(it).compose(RxTransform.flowableToMain())
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
                    remoteParticipantCnt++
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
        IMLiveManager.shared().leaveRoom()
        finish()
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

    override fun isSpeakerMuted(): Boolean {
        return IMLiveManager.shared().isSpeakerMuted()
    }

    override fun muteSpeaker(mute: Boolean) {
        IMLiveManager.shared().muteSpeaker(mute)
    }

    override fun currentLocalCamera(): Int {
        return binding.participantLocal.currentCamera()
    }

    override fun switchLocalCamera() {
        binding.participantLocal.switchCamera()
    }

    override fun muteLocalVideo(mute: Boolean) {
        binding.participantLocal.muteVideo(mute)
    }

    override fun isLocalVideoMuted(): Boolean {
        return binding.participantLocal.isVideoMuted()
    }

    override fun muteLocalAudio(mute: Boolean) {
        binding.participantLocal.muteAudio(mute)
    }

    override fun isLocalAudioMuted(): Boolean {
        return binding.participantLocal.isAudioMuted()
    }

    override fun muteRemoteAudio(uId: Long, mute: Boolean) {
        binding.participantRemote.muteAudio(mute)
    }

    override fun isRemoteAudioMuted(uId: Long): Boolean {
        return binding.participantRemote.isAudioMuted()
    }

    override fun muteRemoteVideo(uId: Long, mute: Boolean) {
        binding.participantRemote.muteVideo(mute)
    }

    override fun isRemoteVideoMuted(uId: Long): Boolean {
        return binding.participantRemote.isVideoMuted()
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