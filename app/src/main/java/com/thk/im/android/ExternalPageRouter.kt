package com.thk.im.android

import android.content.Context
import android.content.Intent
import com.thk.im.android.core.db.entity.Session
import com.thk.im.android.core.db.entity.User
import com.thk.im.android.ui.chat.MessageActivity
import com.thk.im.android.ui.protocol.IMPageRouter
import com.thk.im.android.ui.contact.ContactUserActivity

class ExternalPageRouter: IMPageRouter {
    override fun openSession(ctx: Context, session: Session) {
        val intent = Intent(ctx, MessageActivity::class.java)
        intent.putExtra("session", session)
        ctx.startActivity(intent)
    }

    override fun openContactUserPage(ctx: Context, user: User) {
        ContactUserActivity.startContactUserActivity(ctx, user)
    }

}