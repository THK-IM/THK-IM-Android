package com.thk.im.android.ui.component

import android.content.Context
import android.graphics.Color
import android.text.TextUtils
import android.widget.LinearLayout
import androidx.emoji2.widget.EmojiTextView
import androidx.lifecycle.Observer
import com.lxj.xpopup.core.PositionPopupView
import com.thk.im.android.R
import com.thk.im.android.core.base.BaseSubscriber
import com.thk.im.android.core.base.RxTransform
import com.thk.im.android.core.base.extension.setShape
import com.thk.im.android.core.base.utils.AppUtils
import com.thk.im.android.core.event.XEventBus
import com.thk.im.android.live.BeingRequestedSignal
import com.thk.im.android.live.CallType
import com.thk.im.android.live.CancelBeingRequestedSignal
import com.thk.im.android.live.LiveSignal
import com.thk.im.android.live.LiveSignalType
import com.thk.im.android.live.Role
import com.thk.im.android.live.liveSignalEvent
import com.thk.im.android.live.room.RTCRoom
import com.thk.im.android.live.room.RTCRoomManager
import com.thk.im.android.ui.call.LiveCallActivity
import io.reactivex.disposables.CompositeDisposable

class BeRequestedCallPopup(context: Context) : PositionPopupView(context) {

    lateinit var signal: BeingRequestedSignal
    private lateinit var rootView: LinearLayout
    private lateinit var msgView: EmojiTextView
    private lateinit var acceptView: EmojiTextView
    private lateinit var rejectView: EmojiTextView
    private val disposable = CompositeDisposable()
    private val observer = Observer<LiveSignal> { s ->
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
        XEventBus.observe(liveSignalEvent, observer)
        rootView = findViewById(R.id.ly_root)
        msgView = findViewById(R.id.tv_msg)
        acceptView = findViewById(R.id.tv_accept)
        rejectView = findViewById(R.id.tv_reject)
        rootView.setShape(
            Color.parseColor("#FFFFFF"), floatArrayOf(12f, 12f, 12f, 12f), false
        )
        if (TextUtils.isEmpty(signal.msg)) {
            msgView.text = "xx邀请你进行电话"
        } else {
            msgView.text = signal.msg
        }
        acceptView.setShape(
            Color.parseColor("#DDDDDD"), floatArrayOf(12f, 12f, 12f, 12f), false
        )

        rejectView.setShape(
            Color.parseColor("#DDDDDD"), floatArrayOf(12f, 12f, 12f, 12f), false
        )

        acceptView.setOnClickListener {
            val subscriber = object : BaseSubscriber<RTCRoom>() {
                override fun onNext(t: RTCRoom) {
                    dismiss()
                    RTCRoomManager.shared().addRoom(t)
                    LiveCallActivity.startCallActivity(
                        context, t.id, CallType.BeCalling, signal.members.toTypedArray()
                    )
                }
            }
            RTCRoomManager.shared().joinRoom(signal.roomId, Role.Broadcaster.value)
                .compose(RxTransform.flowableToMain())
                .subscribe(subscriber)
            disposable.add(subscriber)
        }

        rejectView.setOnClickListener {
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