package com.thk.im.android

import android.content.Context
import android.content.Intent
import com.thk.im.android.core.db.entity.Session
import com.thk.im.android.core.db.entity.User
import com.thk.im.android.ui.chat.MessageActivity
import com.thk.im.android.ui.protocol.IMSessionOperator
import com.thk.im.android.ui.user.UserActivity

class ExternalSessionOperator: IMSessionOperator {
    override fun openSession(ctx: Context, session: Session) {
        val intent = Intent(ctx, MessageActivity::class.java)
        intent.putExtra("session", session)
        ctx.startActivity(intent)
    }

    override fun openUserPage(ctx: Context, user: User) {
        UserActivity.startUserActivity(ctx, user)
    }

}