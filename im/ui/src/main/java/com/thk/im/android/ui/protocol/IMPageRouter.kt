package com.thk.im.android.ui.protocol

import android.content.Context
import com.thk.im.android.core.db.entity.Group
import com.thk.im.android.core.db.entity.Message
import com.thk.im.android.core.db.entity.Session
import com.thk.im.android.core.db.entity.User

interface IMPageRouter {

    fun openSession(ctx: Context, session: Session)

    fun openUserPage(ctx: Context, user: User, session: Session)

    fun openGroupPage(ctx: Context, group: Group, session: Session)

    fun openLiveCall(ctx: Context, session: Session)

    fun openMsgReadStatusPage(ctx: Context, session: Session, message: Message)
}