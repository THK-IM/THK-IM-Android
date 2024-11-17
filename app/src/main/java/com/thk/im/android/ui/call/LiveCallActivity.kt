package com.thk.im.android.ui.call

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import com.thk.im.android.core.IMCoreManager
import com.thk.im.android.core.base.BaseSubscriber
import com.thk.im.android.core.base.RxTransform
import com.thk.im.android.core.db.entity.User
import com.thk.im.android.core.event.XEventBus
import com.thk.im.android.databinding.ActvitiyLiveCallBinding
import com.thk.im.android.live.AcceptRequestSignal
import com.thk.im.android.live.CallType
import com.thk.im.android.live.EndCallSignal
import com.thk.im.android.live.HangupSignal
import com.thk.im.android.live.KickMemberSignal
import com.thk.im.android.live.LiveSignal
import com.thk.im.android.live.LiveSignalType
import com.thk.im.android.live.RejectRequestSignal
import com.thk.im.android.live.liveSignalEvent
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
    private lateinit var rtcRoom: RTCRoom

    private fun callType(): Int {
        return intent.getIntExtra("callType", 1)
    }

    private fun roomId(): String {
        return intent.getStringExtra("roomId") ?: ""
    }

    private fun members(): Array<Long> {
        return intent.getLongArrayExtra("members")?.toTypedArray() ?: emptyArray()
    }

    private fun acceptMembers(): Array<Long> {
        return intent.getLongArrayExtra("accept_members")?.toTypedArray() ?: emptyArray()
    }

    private fun rejectMembers(): Array<Long> {
        return intent.getLongArrayExtra("reject_members")?.toTypedArray() ?: emptyArray()
    }

    private fun needCallMembers(): Set<Long> {
        val members = members()
        val acceptMembers = acceptMembers()
        val rejectMembers = rejectMembers()
        val needCallMembers = mutableSetOf<Long>()
        for (m in members) {
            if (acceptMembers.contains(m)) {
                continue
            }
            if (rejectMembers.contains(m)) {
                continue
            }
            if (m == RTCRoomManager.shared().myUId) {
                continue
            }
            needCallMembers.add(m)
        }
        return needCallMembers
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActvitiyLiveCallBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        val room = RTCRoomManager.shared().getRoomById(roomId())
        if (room == null) {
            finish()
            return
        }
        rtcRoom = room
        initView()
        initUserInfo()
        checkPermission()
        XEventBus.observe(this, liveSignalEvent, Observer<LiveSignal> { signal ->
            signal.signalForType(
                LiveSignalType.AcceptRequest.value,
                AcceptRequestSignal::class.java
            )?.let {
                onRemoteAcceptedCallingBySignal(it.roomId, it.uId)
            }

            signal.signalForType(
                LiveSignalType.RejectRequest.value,
                RejectRequestSignal::class.java
            )?.let {
                onRemoteRejectedCallingBySignal(it.roomId, it.uId, it.msg)
            }

            signal.signalForType(
                LiveSignalType.Hangup.value,
                HangupSignal::class.java
            )?.let {
                onRemoteHangupCallingBySignal(it.roomId, it.uId, it.msg)
            }

            signal.signalForType(
                LiveSignalType.KickMember.value,
                KickMemberSignal::class.java
            )?.let {
                onMemberKickedOffBySignal(it.roomId, it.kickIds, it.msg)
            }

            signal.signalForType(
                LiveSignalType.EndCall.value,
                EndCallSignal::class.java
            )?.let {
                onCallEndedBySignal(it.roomId)
            }
        })
    }

    private fun initView() {
        binding.llRequestCall.initCall(this)
        binding.llCalling.initCall(this)

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

//        val subscriber = object : BaseSubscriber<Long>() {
//            override fun onNext(t: Long) {
//                timerTick(t)
//            }
//        }
//        Flowable.interval(0, 3, TimeUnit.SECONDS).take(Long.MAX_VALUE)
//            .compose(RxTransform.flowableToMain())
//            .subscribe(subscriber)
//        addDispose(subscriber)
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
        if (callType() == CallType.RequestCalling.value) {
            showRequestCallView()
        } else {
            showCallingView()
        }
    }

    private fun showRequestCallView() {
        binding.llRequestCall.visibility = View.VISIBLE
        binding.llCalling.visibility = View.GONE
        startRequestCalling()
    }

    private fun showCallingView() {
        binding.llRequestCall.visibility = View.GONE
        binding.llCalling.visibility = View.VISIBLE
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
            binding.participantLocal.startPeerConnection()
        }
    }

    override fun finish() {
        super.finish()
        RTCRoomManager.shared().destroyLocalRoom(roomId())
    }

    override fun room(): RTCRoom {
        return rtcRoom
    }

    override fun startRequestCalling() {
        val needCallMembers = needCallMembers()
        if (needCallMembers.isNotEmpty()) {
            val subscriber = object : BaseSubscriber<Void>() {
                override fun onNext(t: Void?) {
                }
            }
            RTCRoomManager.shared().callRoomMembers(
                roomId(), "ssss", 3, needCallMembers
            ).subscribe(subscriber)
            addDispose(subscriber)
            binding.llCalling.postDelayed({
                startRequestCalling()
            }, 3000)
        }
    }

    override fun cancelRequestCalling() {
        val subscriber = object : BaseSubscriber<Void>() {
            override fun onNext(t: Void?) {
                finish()
            }
        }
        RTCRoomManager.shared().cancelCallRoomMembers(rtcRoom.id, "", needCallMembers().toSet())
            .compose(RxTransform.flowableToMain())
            .subscribe(subscriber)
        addDispose(subscriber)
    }

    override fun hangupCalling() {
        val subscriber = object : BaseSubscriber<Void>() {
            override fun onNext(t: Void?) {
                finish()
            }
        }
        RTCRoomManager.shared().leaveRoom(roomId(), true)
            .compose(RxTransform.flowableToMain())
            .subscribe(subscriber)
        addDispose(subscriber)
    }

    override fun onRemoteAcceptedCallingBySignal(roomId: String, uId: Long) {
        if (roomId == roomId()) {
            var members = acceptMembers()
            members = members.plus(uId)
            intent.putExtra("accept_members", members)
        }
    }

    override fun onRemoteRejectedCallingBySignal(roomId: String, uId: Long, msg: String) {
        if (roomId == roomId()) {
            var members = rejectMembers()
            members = members.plus(uId)
            intent.putExtra("reject_members", members)
        }
    }

    override fun onRemoteHangupCallingBySignal(roomId: String, uId: Long, msg: String) {
        if (roomId == roomId()) {
            val subscriber = object : BaseSubscriber<Void>() {
                override fun onNext(t: Void?) {
                    finish()
                }
            }
            RTCRoomManager.shared().leaveRoom(roomId, true)
                .compose(RxTransform.flowableToMain())
                .subscribe(subscriber)
            addDispose(subscriber)
        }
    }

    override fun onMemberKickedOffBySignal(roomId: String, uIds: Set<Long>, msg: String) {
    }

    override fun onCallEndedBySignal(roomId: String) {
        finish()
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