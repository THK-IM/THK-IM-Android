package com.thk.im.android.ui.call

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import com.google.gson.Gson
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import com.thk.im.android.constant.DemoMsgType
import com.thk.im.android.core.IMCoreManager
import com.thk.im.android.core.base.BaseSubscriber
import com.thk.im.android.core.base.LLog
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
import com.thk.im.android.live.engine.LiveRTCEngine
import com.thk.im.android.live.room.BaseParticipant
import com.thk.im.android.live.room.RTCRoom
import com.thk.im.android.live.room.RTCRoomCallBack
import com.thk.im.android.live.room.RTCRoomManager
import com.thk.im.android.live.room.RemoteParticipant
import com.thk.im.android.module.msg.call.IMCallMsg
import com.thk.im.android.ui.base.BaseActivity
import java.nio.ByteBuffer

class LiveCallActivity : BaseActivity(), RTCRoomCallBack, LiveCallProtocol {

    companion object {

        fun startCallActivity(
            ctx: Context,
            sessionId: Long?,
            roomId: String,
            callType: CallType,
            members: LongArray
        ) {
            val intent = Intent(ctx, LiveCallActivity::class.java)
            intent.putExtra("callType", callType.value)
            intent.putExtra("sessionId", sessionId)
            intent.putExtra("roomId", roomId)
            intent.putExtra("members", members)
            ctx.startActivity(intent)
        }
    }

    private var callMsg: IMCallMsg? = null
    private lateinit var binding: ActvitiyLiveCallBinding
    private val callAction = Runnable {
        startRequestCalling()
    }

    private fun callType(): Int {
        return intent.getIntExtra("callType", 1)
    }

    private fun roomId(): String {
        return intent.getStringExtra("roomId") ?: ""
    }

    private fun members(): MutableSet<Long> {
        return intent.getLongArrayExtra("members")?.toMutableSet() ?: mutableSetOf()
    }

    private fun acceptMembers(): MutableSet<Long> {
        return intent.getLongArrayExtra("accept_members")?.toMutableSet() ?: mutableSetOf()
    }

    private fun rejectMembers(): MutableSet<Long> {
        return intent.getLongArrayExtra("reject_members")?.toMutableSet() ?: mutableSetOf()
    }

    private fun hangupMembers(): MutableSet<Long> {
        return intent.getLongArrayExtra("hangup_members")?.toMutableSet() ?: mutableSetOf()
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
        RTCRoomManager.shared().getRoomById(roomId())?.let {
            it.callback = this
            if (it.ownerId == IMCoreManager.uId) {
                callMsg = IMCallMsg(it.id, it.ownerId, it.mode, it.createTime, 0, 0, 0)
            }
        }
        initView()
        initUserInfo()
        checkPermission()
        XEventBus.observe(this, LiveSignal.EVENT, Observer<LiveSignal> { signal ->
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
    }

    private fun initUserInfo() {
        RTCRoomManager.shared().getRoomById(roomId())?.getAllParticipants()?.forEach {
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
        RTCRoomManager.shared().getRoomById(roomId())?.getAllParticipants()?.forEach { p ->
            initParticipant(p)
        }
    }

    private fun showRequestCallView() {
        binding.llRequestCall.visibility = View.VISIBLE
        binding.llCalling.visibility = View.GONE
        startRequestCalling()
//        try {
//            val afd = assets.openFd("dukou.mp3")
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//                LiveRTCEngine.shared().mediaPlayer?.setMediaItem(afd)
//                LiveRTCEngine.shared().mediaPlayer?.play()
//            }
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
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
        binding.llCalling.removeCallbacks(callAction)
        callMsg?.let {
            if (it.accepted == 2) {
                it.duration = IMCoreManager.severTime - it.acceptTime
            }
            sendCallMsgToUId(it)
            callMsg = null
        }
        RTCRoomManager.shared().destroyLocalRoom(roomId())
    }

    private fun sendCallMsgToUId(callMsg: IMCallMsg) {
        val sessionId = intent.getLongExtra("sessionId", 0)
        if (sessionId == 0L) return
        val msg = Gson().toJson(callMsg)
        IMCoreManager.messageModule.sendMessage(sessionId, DemoMsgType.Call.value, msg, null)
    }

    override fun room(): RTCRoom? {
        return RTCRoomManager.shared().getRoomById(roomId())
    }

    override fun startRequestCalling() {
        val needCallMembers = needCallMembers()
        if (needCallMembers.isNotEmpty()) {
            val subscriber = object : BaseSubscriber<Void>() {
                override fun onNext(t: Void?) {
                }
            }
            RTCRoomManager.shared().callRoomMembers(
                roomId(), "", (2 + LiveSignal.TIMEOUT_SECOND).toLong(), needCallMembers
            ).compose(RxTransform.flowableToMain())
                .subscribe(subscriber)
            addDispose(subscriber)
            binding.llCalling.postDelayed(callAction, (LiveSignal.TIMEOUT_SECOND) * 1000L)
        }
    }

    override fun cancelRequestCalling() {
        val subscriber = object : BaseSubscriber<Void>() {
            override fun onNext(t: Void?) {
            }

            override fun onComplete() {
                super.onComplete()
                finish()
            }
        }
        RTCRoomManager.shared().cancelCallRoomMembers(roomId(), "", needCallMembers().toSet())
            .compose(RxTransform.flowableToMain())
            .subscribe(subscriber)
        addDispose(subscriber)
    }

    override fun hangupCalling() {
        val subscriber = object : BaseSubscriber<Void>() {
            override fun onNext(t: Void?) {
            }

            override fun onComplete() {
                super.onComplete()
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
            val acceptMembers = acceptMembers()
            acceptMembers.add(uId)
            intent.putExtra("accept_members", acceptMembers.toLongArray())

            val rejectMembers = rejectMembers()
            rejectMembers.remove(uId)
            intent.putExtra("reject_members", rejectMembers.toLongArray())

            showCallingView()

            callMsg?.let {
                it.accepted = 2
                it.acceptTime = IMCoreManager.severTime
            }
        }
    }

    override fun onRemoteRejectedCallingBySignal(roomId: String, uId: Long, msg: String) {
        if (roomId == roomId()) {
            val acceptMembers = acceptMembers()
            acceptMembers.remove(uId)
            intent.putExtra("accept_members", acceptMembers.toLongArray())

            val rejectMembers = rejectMembers()
            rejectMembers.add(uId)
            intent.putExtra("reject_members", rejectMembers.toLongArray())
            callMsg?.let {
                it.accepted = 1
                it.acceptTime = IMCoreManager.severTime
            }
            if (needCallMembers().isEmpty() && acceptMembers().isEmpty()) {
                finish()
            }
        }
    }

    override fun onRemoteHangupCallingBySignal(roomId: String, uId: Long, msg: String) {
        if (roomId == roomId()) {
            val hangupMembers = hangupMembers()
            hangupMembers.add(uId)
            intent.putExtra("hangup_members", hangupMembers.toLongArray())
            finish()
        }
    }

    override fun onMemberKickedOffBySignal(roomId: String, uIds: Set<Long>, msg: String) {
    }

    override fun onCallEndedBySignal(roomId: String) {
        finish()
    }

    override fun onParticipantJoin(p: BaseParticipant) {
        initParticipant(p)
        showCallingView()
    }

    override fun onParticipantLeave(p: BaseParticipant) {
//        finish()
    }

    override fun onTextMsgReceived(type: Int, text: String) {
    }

    override fun onDataMsgReceived(data: ByteBuffer) {
    }

    override fun onConnectStatus(uId: Long, status: Int) {
    }

    override fun onParticipantVoice(uId: Long, volume: Double) {
        LLog.d(
            "RTCRoomCallBack",
            "uid: $uId, isMyself: ${uId == RTCRoomManager.shared().myUId} volume: $volume"
        )
    }

    override fun onError(function: String, ex: Exception) {
    }

    override fun onDestroy() {
        super.onDestroy()
        LiveRTCEngine.shared().mediaPlayer?.release()
    }

}