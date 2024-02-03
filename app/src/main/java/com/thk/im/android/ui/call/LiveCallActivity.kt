package com.thk.im.android.ui.call

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import com.thk.android.im.live.IMLiveManager
import com.thk.android.im.live.Mode
import com.thk.android.im.live.Role
import com.thk.android.im.live.RoomObserver
import com.thk.android.im.live.room.BaseParticipant
import com.thk.android.im.live.room.LocalParticipant
import com.thk.android.im.live.room.RemoteParticipant
import com.thk.android.im.live.room.Room
import com.thk.im.android.core.base.BaseSubscriber
import com.thk.im.android.core.base.RxTransform
import com.thk.im.android.core.db.entity.User
import com.thk.im.android.databinding.ActvitiyLiveCallBinding
import com.thk.im.android.ui.base.BaseActivity
import java.nio.ByteBuffer

class LiveCallActivity : BaseActivity(), RoomObserver, LiveCallProtocol {

    companion object {
        fun startCallActivity(
            ctx: Context,
            mode: Mode,
            user: User,
            roomId: String?
        ) {
            val intent = Intent(ctx, LiveCallActivity::class.java)
            intent.putExtra("mode", mode.value)
            intent.putExtra("user", user)
            roomId?.let {
                intent.putExtra("roomId", it)
            }
            ctx.startActivity(intent)
        }
    }

    private lateinit var binding: ActvitiyLiveCallBinding
    private var user: User? = null
    private var mode: Int = 0
    private var room: Room? = null
    private var roomId: String? = null
    private var status: LiveCallStatus = LiveCallStatus.Init

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActvitiyLiveCallBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        user = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("user", User::class.java)
        } else {
            intent.getParcelableExtra("user")
        }
        user?.let {
            binding.llRequestCall.initUI(it, this)
            binding.llCalling.initUI(it, this)
            binding.llBeCalling.initUI(it, this)
        }
        mode = intent.getIntExtra("mode", 0)
        val roomId = intent.getStringExtra("roomId")
        status = if (roomId.isNullOrBlank()) {
            LiveCallStatus.RequestCall
        } else {
            this.roomId = roomId
            LiveCallStatus.BeRequestCall
        }
        checkPermission()
    }

    private fun checkPermission() {
        XXPermissions.with(this).permission(Permission.CAMERA, Permission.RECORD_AUDIO)
            .request(object : OnPermissionCallback {
                override fun onGranted(permissions: MutableList<String>, allGranted: Boolean) {
                    createOrJoinRoom()
                }

                override fun onDenied(permissions: MutableList<String>, doNotAskAgain: Boolean) {
                    super.onDenied(permissions, doNotAskAgain)
                }
            })
    }

    private fun createOrJoinRoom() {
        if (status == LiveCallStatus.BeRequestCall) {
            showBeCallingView()
            joinRoom()
        } else {
            showRequestCallView()
            createRoom()
        }
    }

    private fun createRoom() {
        user?.let {
            val subscriber = object : BaseSubscriber<Room>() {
                override fun onNext(t: Room?) {
                    t?.let {
                        room = t
                        initRoom()
                    }
                }

                override fun onComplete() {
                    super.onComplete()
                    removeDispose(this)
                }
            }
            IMLiveManager.shared().createRoom(mutableSetOf(it.id), Mode.Video)
                .compose(RxTransform.flowableToMain())
                .subscribe(subscriber)
            addDispose(subscriber)
        }
    }

    private fun joinRoom() {
        roomId?.let {
            val subscriber = object : BaseSubscriber<Room>() {
                override fun onNext(t: Room?) {
                    t?.let {
                        room = t
                        initRoom()
                    }
                }

                override fun onComplete() {
                    super.onComplete()
                    removeDispose(this)
                }
            }
            IMLiveManager.shared().joinRoom(it, Role.Broadcaster)
                .compose(RxTransform.flowableToMain())
                .subscribe(subscriber)
            addDispose(subscriber)
        }
    }

    private fun initRoom() {
        room?.let {
            it.getAllParticipants().forEach { p ->
                join(p)
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

    override fun join(p: BaseParticipant) {
        if (p is RemoteParticipant) {
            showCallingView()
            binding.participantLocal.setFullscreenMode(false)
            binding.participantRemote.setParticipant(p)
            binding.participantRemote.setFullscreenMode(true)
        } else {
            binding.participantLocal.setParticipant(p)
            binding.participantLocal.setFullscreenMode(true)
        }
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
        if (room != null) {
            val participants = room!!.getAllParticipants()
            for (p in participants) {
                if (p is LocalParticipant) {
                    return p.currentCamera()
                }
            }
        }
        return 1
    }

    override fun isCurrentCameraOpened(): Boolean {
        if (room != null) {
            val participants = room!!.getAllParticipants()
            for (p in participants) {
                if (p is LocalParticipant) {
                    return !p.getVideoMuted()
                }
            }
        }
        return false
    }

    override fun switchLocalCamera() {
        room?.let {
            it.getAllParticipants().forEach {p ->
                if(p is LocalParticipant) {
                    p.switchCamera()
                }
            }
        }
    }

    override fun openLocalCamera() {
        room?.let {
            it.getAllParticipants().forEach {p ->
                if(p is LocalParticipant) {
                    p.setVideoMuted(false)
                }
            }
        }
    }

    override fun closeLocalCamera() {
        room?.let {
            it.getAllParticipants().forEach {p ->
                if(p is LocalParticipant) {
                    p.setVideoMuted(true)
                }
            }
        }
    }

    override fun openRemoteVideo(user: User) {
        room?.let {
            it.getAllParticipants().forEach {p ->
                if(p is RemoteParticipant) {
                    if (user.id == p.uId) {
                        p.setVideoMuted(false)
                    }
                }
            }
        }
    }

    override fun closeRemoteVideo(user: User) {
        room?.let {
            it.getAllParticipants().forEach {p ->
                if(p is RemoteParticipant) {
                    if (user.id == p.uId) {
                        p.setVideoMuted(true)
                    }
                }
            }
        }
    }

    override fun openRemoteAudio(user: User) {
        room?.let {
            it.getAllParticipants().forEach {p ->
                if(p is RemoteParticipant) {
                    if (user.id == p.uId) {
                        p.setAudioMuted(false)
                    }
                }
            }
        }
    }

    override fun closeRemoteAudio(user: User) {
        room?.let {
            it.getAllParticipants().forEach {p ->
                if(p is RemoteParticipant) {
                    if (user.id == p.uId) {
                        p.setAudioMuted(true)
                    }
                }
            }
        }
    }

    override fun accept() {
        joinRoom()
    }

    override fun hangup() {
        finish()
    }

}