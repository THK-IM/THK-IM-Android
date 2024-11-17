package com.thk.im.android.ui.call.popup

import android.content.Context
import android.graphics.Color
import androidx.lifecycle.Observer
import com.google.android.material.shape.ShapeAppearanceModel
import com.lxj.xpopup.core.PositionPopupView
import com.thk.im.android.R
import com.thk.im.android.core.IMCoreManager
import com.thk.im.android.core.base.BaseSubscriber
import com.thk.im.android.core.base.IMImageLoader
import com.thk.im.android.core.base.RxTransform
import com.thk.im.android.core.base.extension.setShape
import com.thk.im.android.core.base.utils.AppUtils
import com.thk.im.android.core.db.entity.User
import com.thk.im.android.core.event.XEventBus
import com.thk.im.android.databinding.PopupBeRequestingBinding
import com.thk.im.android.live.BeingRequestedSignal
import com.thk.im.android.live.CallType
import com.thk.im.android.live.CancelBeingRequestedSignal
import com.thk.im.android.live.LiveSignal
import com.thk.im.android.live.LiveSignalType
import com.thk.im.android.live.Role
import com.thk.im.android.live.RoomMode
import com.thk.im.android.live.liveSignalEvent
import com.thk.im.android.live.room.RTCRoom
import com.thk.im.android.live.room.RTCRoomManager
import com.thk.im.android.ui.call.LiveCallActivity
import io.reactivex.disposables.CompositeDisposable

class BeRequestedCallingPopup(context: Context) : PositionPopupView(context) {

    lateinit var signal: BeingRequestedSignal
    private lateinit var binding: PopupBeRequestingBinding
    private val disposable = CompositeDisposable()
    private val observer = Observer<LiveSignal> { s ->
        s.signalForType(
            LiveSignalType.BeingRequested.value,
            BeingRequestedSignal::class.java
        )?.let {
            if (it.roomId == signal.roomId) {
                beCalling(it)
            }
        }
        s.signalForType(
            LiveSignalType.CancelBeingRequested.value,
            CancelBeingRequestedSignal::class.java
        )?.let {
            if (it.roomId == signal.roomId) {
                dismiss()
            }
        }
    }

    override fun getImplLayoutId(): Int {
        return R.layout.popup_be_requesting
    }

    override fun onCreate() {
        super.onCreate()
        binding = PopupBeRequestingBinding.bind(popupImplView)
        XEventBus.observe(liveSignalEvent, observer)
        binding.lyContainer.setShape(
            Color.parseColor("#A0000000"), floatArrayOf(12f, 12f, 12f, 12f), false
        )

        val shapeMode = ShapeAppearanceModel.builder()
            .setAllCornerSizes(AppUtils.dp2px(10f).toFloat())
            .build()
        binding.ivAvatar.shapeAppearanceModel = shapeMode

        if (signal.mode == RoomMode.Video.value) {
            binding.tvMsg.text = "邀请你视频通话"
        } else if (signal.mode == RoomMode.Audio.value) {
            binding.tvMsg.text = "邀请你语音通话"
        }

        binding.ivAccept.setOnClickListener {
            val subscriber = object : BaseSubscriber<RTCRoom>() {
                override fun onNext(t: RTCRoom) {
                    dismiss()
                    RTCRoomManager.shared().addRoom(t)
                    LiveCallActivity.startCallActivity(
                        context, t.id, CallType.BeCalling, signal.members.toLongArray()
                    )
                }
            }
            RTCRoomManager.shared().joinRoom(signal.roomId, Role.Broadcaster.value)
                .compose(RxTransform.flowableToMain())
                .subscribe(subscriber)
            disposable.add(subscriber)
        }

        binding.ivReject.setOnClickListener {
            val subscriber = object : BaseSubscriber<Void>() {
                override fun onNext(t: Void?) {
                }

                override fun onComplete() {
                    super.onComplete()
                    dismiss()
                }
            }
            RTCRoomManager.shared().refuseToJoinRoom(signal.roomId, "")
                .compose(RxTransform.flowableToMain())
                .subscribe(subscriber)
            disposable.add(subscriber)
        }
        notifyCalling()
        requestUser(signal.requestId)
    }

    private fun requestUser(id: Long) {
        val subscriber = object : BaseSubscriber<User>() {
            override fun onNext(t: User) {
                updateUser(t)
            }
        }
        IMCoreManager.userModule.queryUser(id)
            .compose(RxTransform.flowableToMain())
            .subscribe(subscriber)
        disposable.add(subscriber)
    }

    private fun updateUser(u: User) {
        IMImageLoader.displayImageUrl(binding.ivAvatar, u.avatar ?: "")
        binding.tvNickname.text = u.nickname
    }

    private fun beCalling(signal: BeingRequestedSignal) {
        this.signal = signal
    }

    private fun notifyCalling() {
        if (signal.createTime + signal.timeoutTime > IMCoreManager.severTime) {
            AppUtils.instance().notifyNewMessage()
            rootView.postDelayed({
                notifyCalling()
            }, 1500)
        } else {
            dismiss()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        XEventBus.unObserve(liveSignalEvent, observer)
        disposable.clear()
    }

    override fun getPopupWidth(): Int {
        return AppUtils.instance().screenWidth
    }

}