package com.thk.im.android

import android.content.Context
import android.content.Intent
import com.thk.im.android.core.db.entity.Session
import com.thk.im.android.ui.protocol.IMSessionOperator

class ExternalSessionOperator: IMSessionOperator {
    override fun openSession(ctx: Context, session: Session) {
        val intent = Intent()
        intent.setClass(ctx, MessageActivity::class.java)
        intent.putExtra("session", session)
        ctx.startActivity(intent)
    }
}