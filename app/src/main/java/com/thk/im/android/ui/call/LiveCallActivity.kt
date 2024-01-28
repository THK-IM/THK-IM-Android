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
import com.thk.android.im.live.room.Room
import com.thk.im.android.core.base.BaseSubscriber
import com.thk.im.android.core.base.RxTransform
import com.thk.im.android.core.db.entity.User
import com.thk.im.android.databinding.ActvitiyLiveCallBinding
import com.thk.im.android.ui.base.BaseActivity
import java.nio.ByteBuffer

class LiveCallActivity : BaseActivity(), RoomObserver {

    companion object {
        fun startCallActivity(
            ctx: Context,
            mode: Mode,
            user: User
        ) {
            val intent = Intent(ctx, LiveCallActivity::class.java)
            intent.putExtra("mode", mode.value)
            intent.putExtra("user", user)
            ctx.startActivity(intent)
        }
    }

    private lateinit var binding: ActvitiyLiveCallBinding
    private var user: User? = null
    private var mode: Int = 0
    private var room: Room? = null
    private var roomId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActvitiyLiveCallBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        user = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("user", User::class.java)
        } else {
            intent.getParcelableExtra("user")
        }
        mode = intent.getIntExtra("mode", 0)
        checkPermission()
    }

    private fun checkPermission() {
        XXPermissions.with(this).permission(Permission.CAMERA, Permission.RECORD_AUDIO)
            .request(object : OnPermissionCallback {
                override fun onGranted(permissions: MutableList<String>, allGranted: Boolean) {
                    createRoom()
                }

                override fun onDenied(permissions: MutableList<String>, doNotAskAgain: Boolean) {
                    super.onDenied(permissions, doNotAskAgain)
                }
            })
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
                if (p is LocalParticipant) {
                    binding.participantFirst.setParticipant(p)
                } else {
                    binding.participantSecond.setParticipant(p)
                }
            }
        }
    }

    fun showRequestCallView() {
        binding.llRequestCall.visibility = View.VISIBLE
        binding.llCalling.visibility = View.GONE
        binding.llBeCalling.visibility = View.GONE
    }

    fun showCallingView() {
        binding.llRequestCall.visibility = View.GONE
        binding.llCalling.visibility = View.VISIBLE
        binding.llBeCalling.visibility = View.GONE
    }

    fun showBeCallingView() {
        binding.llRequestCall.visibility = View.GONE
        binding.llCalling.visibility = View.GONE
        binding.llBeCalling.visibility = View.VISIBLE
    }

    override fun join(p: BaseParticipant) {
        if (p is LocalParticipant) {
            binding.participantFirst.setParticipant(p)
            binding.participantFirst.setMoveByTouch(true)
        } else {
            binding.participantSecond.setParticipant(p)
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
        binding.participantFirst.destroy()
        binding.participantSecond.destroy()
    }

}