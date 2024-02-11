package com.thk.im.android.module

import android.app.Application
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.thk.android.im.live.IMLiveManager
import com.thk.android.im.live.Role
import com.thk.android.im.live.room.Room
import com.thk.im.android.IMApplication
import com.thk.im.android.core.base.BaseSubscriber
import com.thk.im.android.core.base.LLog
import com.thk.im.android.core.base.RxTransform
import com.thk.im.android.core.module.internal.DefaultCustomModule
import com.thk.im.android.ui.call.LiveCallActivity
import io.reactivex.disposables.CompositeDisposable

enum class LiveSignalType(val value: Int) {
    InviteLiveCall(1),
    HangupLiveCall(2),
    EndLiveCall(3)
}

data class LiveSignal(
    @SerializedName("room_id")
    var roomId: String,
    @SerializedName("members")
    var members: Set<Long>,
    @SerializedName("owner_id")
    var ownerId: Long,
    @SerializedName("mode")
    var mode: Int,
    @SerializedName("create_time")
    var createTime: Long,
    @SerializedName("msg_type")
    var msgType: Int,
    @SerializedName("operator_id")
    var operatorId: Long
)


class IMCustomModule(val app: Application) : DefaultCustomModule() {

    private val compositeDisposable = CompositeDisposable()
    private val liveCallSignalType = 400
    override fun reset() {
        compositeDisposable.clear()
    }

    override fun onSignalReceived(type: Int, body: String) {
        LLog.d("onSignalReceived, $type  LiveSignal: $body")
        if (type == liveCallSignalType) {
            val signal = Gson().fromJson(body, LiveSignal::class.java)
            when (signal.msgType) {
                LiveSignalType.InviteLiveCall.value -> {
                    onNewLiveCall(signal)
                }

                LiveSignalType.HangupLiveCall.value -> {
                    hangupLiveCall(signal)
                }

                LiveSignalType.EndLiveCall.value -> {
                    endLiveCall(signal)
                }
            }
        }
    }

    private fun onNewLiveCall(signal: LiveSignal) {
        val room = IMLiveManager.shared().getRoom()
        if (room == null) {
            val subscriber = object : BaseSubscriber<Room>() {
                override fun onNext(t: Room?) {
                    t?.let {
                        IMLiveManager.shared().getRoom()?.members?.addAll(signal.members)
                        startLiveCall()
                    }
                }
            }
            IMLiveManager.shared().joinRoom(signal.roomId, Role.Broadcaster)
                .compose(RxTransform.flowableToMain())
                .subscribe(subscriber)
            compositeDisposable.add(subscriber)
        } else {
            if (signal.roomId != room.id) {
                // TODO 占线
            }
        }
    }

    private fun startLiveCall() {
        val activity = (app as IMApplication).currentActivity()
        if (activity != null) {
            LiveCallActivity.startCallActivity(activity)
        } else {

        }
    }

    private fun hangupLiveCall(signal: LiveSignal) {
        IMLiveManager.shared().getRoom()?.let {
            if (it.id == signal.roomId) {
                it.onMemberHangup(signal.operatorId)
            }
        }
    }

    private fun endLiveCall(signal: LiveSignal) {
        IMLiveManager.shared().getRoom()?.let {
            if (it.id == signal.roomId) {
                it.onEndCall()
            }
        }
    }
}