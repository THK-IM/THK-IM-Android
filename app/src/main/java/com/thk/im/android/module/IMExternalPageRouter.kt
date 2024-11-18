package com.thk.im.android.module

import android.content.Context
import android.content.Intent
import com.thk.im.android.core.IMCoreManager
import com.thk.im.android.core.SessionType
import com.thk.im.android.core.base.BaseSubscriber
import com.thk.im.android.core.base.RxTransform
import com.thk.im.android.core.db.entity.Group
import com.thk.im.android.core.db.entity.Message
import com.thk.im.android.core.db.entity.Session
import com.thk.im.android.core.db.entity.User
import com.thk.im.android.live.CallType
import com.thk.im.android.live.RoomMode
import com.thk.im.android.live.api.vo.MediaParams
import com.thk.im.android.live.room.RTCRoom
import com.thk.im.android.live.room.RTCRoomManager
import com.thk.im.android.ui.call.LiveCallActivity
import com.thk.im.android.ui.chat.MessageActivity
import com.thk.im.android.ui.contact.ContactUserActivity
import com.thk.im.android.ui.protocol.IMPageRouter
import io.reactivex.disposables.CompositeDisposable

class IMExternalPageRouter : IMPageRouter {

    private val compositeDisposable = CompositeDisposable()

    override fun openSession(ctx: Context, session: Session) {
        val intent = Intent(ctx, MessageActivity::class.java)
        intent.putExtra("session", session)
        ctx.startActivity(intent)
    }

    override fun openUserPage(ctx: Context, user: User, session: Session) {
        ContactUserActivity.startContactUserActivity(ctx, user)
    }

    override fun openGroupPage(ctx: Context, group: Group, session: Session) {
    }


    override fun openLiveCall(ctx: Context, session: Session) {
        if (session.type == SessionType.Single.value) {
            val ids = longArrayOf(IMCoreManager.uId, session.entityId)
            val subscriber = object : BaseSubscriber<RTCRoom>() {
                override fun onNext(t: RTCRoom) {
                    RTCRoomManager.shared().addRoom(t)
                    LiveCallActivity.startCallActivity(ctx, t.id, CallType.RequestCalling, ids)
                }
            }
            val params = MediaParams(
                2048 * 8 * 1024,
                48 * 8 * 1024,
                540,
                960,
                20,
            )
            RTCRoomManager.shared().createRoom(RoomMode.Video, params)
                .compose(RxTransform.flowableToMain())
                .subscribe(subscriber)
            compositeDisposable.add(subscriber)
        }
    }

    override fun openMsgReadStatusPage(ctx: Context, session: Session, message: Message) {
    }

}