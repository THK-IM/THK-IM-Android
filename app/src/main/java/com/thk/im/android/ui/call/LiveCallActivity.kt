package com.thk.im.android.ui.call

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import com.thk.im.android.core.IMCoreManager
import com.thk.im.android.core.base.BaseSubscriber
import com.thk.im.android.core.base.RxTransform
import com.thk.im.android.core.db.entity.User
import com.thk.im.android.databinding.ActvitiyLiveCallBinding
import com.thk.im.android.live.CallType
import com.thk.im.android.live.Mode
import com.thk.im.android.live.room.BaseParticipant
import com.thk.im.android.live.room.RTCRoom
import com.thk.im.android.live.room.RTCRoomCallBack
import com.thk.im.android.live.room.RTCRoomManager
import com.thk.im.android.live.room.RemoteParticipant
import com.thk.im.android.ui.base.BaseActivity
import java.nio.ByteBuffer

class LiveCallActivity : BaseActivity(), RTCRoomCallBack, LiveCallProtocol {

    companion object {

        fun startCallActivity(
            ctx: Context,
            roomId: String,
            callType: CallType,
            members: Array<Long>
        ) {
            val intent = Intent(ctx, LiveCallActivity::class.java)
            intent.putExtra("callType", callType.value)
            intent.putExtra("roomId", roomId)
            intent.putExtra("members", members)
            ctx.startActivity(intent)
        }
    }

    private lateinit var binding: ActvitiyLiveCallBinding
    private lateinit var room: RTCRoom

    private fun callType(): Int {
        return intent.getIntExtra("callType", 1)
    }

    private fun roomId(): String {
        return intent.getStringExtra("roomId") ?: ""
    }

    private fun members(): Array<Long> {
        return intent.getLongArrayExtra("members")?.toTypedArray() ?: emptyArray()
    }

    private fun rtcRoom(): RTCRoom? {
        return RTCRoomManager.shared().getRoomById(roomId())
    }

    private lateinit var rtcRoom: RTCRoom

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActvitiyLiveCallBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        val room = RTCRoomManager.shared().getRoomById(roomId())
        if (room == null) {
            finish()
            return
        }
        rtcRoom = room
        setContentView(binding.root)
        initView()
        initUserInfo()
        checkPermission()
    }

    private fun initView() {
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
            if (!binding.participantRemote.isFullScreen()) {
                binding.participantLocal.bringToFront()
                binding.llCallingInfo.bringToFront()
                binding.llCalling.bringToFront()
                binding.participantLocal.setFullscreenMode(false)
                binding.participantRemote.setFullscreenMode(true)
            }
        }
    }

    private fun initUserInfo() {
        rtcRoom.getAllParticipants().forEach {
            if (it.uId != RTCRoomManager.shared().myUId) {
                val subscriber = object : BaseSubscriber<User>() {
                    override fun onNext(t: User?) {
                        t?.let { user ->
                            binding.llCallingInfo.setUserInfo(user)
                        }
                    }

                }
                IMCoreManager.userModule.queryUser(it.uId).compose(RxTransform.flowableToMain())
                    .subscribe(subscriber)
                addDispose(subscriber)
            }
        }
    }

    private fun checkPermission() {
        XXPermissions.with(this).permission(Permission.CAMERA, Permission.RECORD_AUDIO)
            .request(object : OnPermissionCallback {
                override fun onGranted(permissions: MutableList<String>, allGranted: Boolean) {
                    initRoomUI()
                }

                override fun onDenied(permissions: MutableList<String>, doNotAskAgain: Boolean) {
                    super.onDenied(permissions, doNotAskAgain)
                }
            })
    }

    private fun initRoomUI() {
        var remoteParticipantCnt = 0
        rtcRoom.getAllParticipants().forEach { p ->
            initParticipant(p)
            if (p is RemoteParticipant) {
                remoteParticipantCnt++
            }
        }
        if (remoteParticipantCnt > 0) {
            showCallingView()
        } else {
            if (callType() == CallType.RequestCalling.value) {
                showRequestCallView()
            } else {
                showBeCallingView()
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

    private fun initParticipant(p: BaseParticipant) {
        if (p is RemoteParticipant) {
            binding.participantLocal.setFullscreenMode(false)
            binding.participantRemote.setParticipant(p)
            binding.participantRemote.setFullscreenMode(true)
            binding.participantRemote.startPeerConnection()
        } else {
            binding.participantLocal.setParticipant(p)
            binding.participantLocal.setFullscreenMode(true)
            if (rtcRoom.ownerId == RTCRoomManager.shared().myUId) {
                binding.participantLocal.startPeerConnection()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.participantLocal.destroy()
        binding.participantRemote.destroy()
        rtcRoom.destroy()
    }

    override fun room(): RTCRoom {
        return room
    }

    override fun requestCalling(mode: Mode, members: Set<Long>) {
        // TODO
    }

    override fun cancelCalling() {
        val subscriber = object : BaseSubscriber<Void>() {
            override fun onNext(t: Void?) {
                finish()
            }
        }
        RTCRoomManager.shared().cancelCallRoomMembers(room.id, "", members().toSet())
            .compose(RxTransform.flowableToMain())
            .subscribe(subscriber)
        addDispose(subscriber)
    }

    override fun acceptCalling() {
        showCallingView()
        binding.participantLocal.startPeerConnection()
        room.getAllParticipants().forEach { p ->
            if (p is RemoteParticipant) {
                initParticipant(p)
            }
        }
    }

    override fun rejectCalling() {
        val subscriber = object : BaseSubscriber<Void>() {
            override fun onNext(t: Void?) {
                finish()
            }
        }
        RTCRoomManager.shared().refuseToJoinRoom(room.id, "")
            .compose(RxTransform.flowableToMain())
            .subscribe(subscriber)
        addDispose(subscriber)
    }

    override fun hangupCalling() {
        rtcRoom.destroy()
        finish()
    }

    override fun onRemoteAcceptedCalling(roomId: String, uId: Long) {
    }

    override fun onRemoteRejectedCalling(roomId: String, uId: Long, msg: String) {
    }

    override fun onRemoteHangupCalling(roomId: String, uId: Long, msg: String) {
    }

    override fun onMemberKickedOff(roomId: String, uIds: Set<Long>) {
    }

    override fun onCallEnded(roomId: String) {
    }

    override fun onParticipantJoin(p: BaseParticipant) {
        showCallingView()
        initParticipant(p)
    }

    override fun onParticipantLeave(p: BaseParticipant) {
    }

    override fun onTextMsgReceived(type: Int, text: String) {
    }

    override fun onDataMsgReceived(data: ByteBuffer) {
    }

    override fun onConnectStatus(uId: Long, status: Int) {
    }

    override fun onParticipantVoice(uId: Long, volume: Double) {
    }

    override fun onError(function: String, ex: Exception) {
    }

}