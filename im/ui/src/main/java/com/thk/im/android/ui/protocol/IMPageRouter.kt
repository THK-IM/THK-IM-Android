package com.thk.im.android.ui.protocol

import android.content.Context
import com.thk.im.android.core.db.entity.Session
import com.thk.im.android.core.db.entity.User

interface IMPageRouter {

    fun openSession(ctx: Context, session: Session)

    fun openContactUserPage(ctx: Context, user: User)

    fun openLiveCall(ctx: Context, session: Session)
}