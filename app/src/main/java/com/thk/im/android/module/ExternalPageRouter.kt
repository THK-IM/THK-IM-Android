package com.thk.im.android.module

import android.content.Context
import android.content.Intent
import com.thk.android.im.live.IMLiveManager
import com.thk.android.im.live.Mode
import com.thk.android.im.live.room.Room
import com.thk.im.android.core.SessionType
import com.thk.im.android.core.base.BaseSubscriber
import com.thk.im.android.core.base.RxTransform
import com.thk.im.android.core.db.entity.Session
import com.thk.im.android.core.db.entity.User
import com.thk.im.android.ui.call.LiveCallActivity
import com.thk.im.android.ui.chat.MessageActivity
import com.thk.im.android.ui.protocol.IMPageRouter
import com.thk.im.android.ui.contact.ContactUserActivity
import io.reactivex.disposables.CompositeDisposable

class ExternalPageRouter: IMPageRouter {

    private val compositeDisposable = CompositeDisposable()

    override fun openSession(ctx: Context, session: Session) {
        val intent = Intent(ctx, MessageActivity::class.java)
        intent.putExtra("session", session)
        ctx.startActivity(intent)
    }

    override fun openContactUserPage(ctx: Context, user: User) {
        ContactUserActivity.startContactUserActivity(ctx, user)
    }

    override fun openLiveCall(ctx: Context, session: Session) {
        if (session.type == SessionType.Single.value) {
            val ids = mutableSetOf(session.entityId, IMLiveManager.shared().selfId)
            val subscriber = object: BaseSubscriber<Room>() {
                override fun onNext(t: Room?) {
                    t?.let {
                        LiveCallActivity.startCallActivity(ctx)
                    }
                }
            }
            IMLiveManager.shared().createRoom(ids, Mode.Video)
                .compose(RxTransform.flowableToMain())
                .subscribe(subscriber)
            compositeDisposable.add(subscriber)
        }
    }

}