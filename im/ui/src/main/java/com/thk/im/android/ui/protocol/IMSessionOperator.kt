package com.thk.im.android.ui.protocol

import android.content.Context
import com.thk.im.android.core.db.entity.Session
import com.thk.im.android.core.db.entity.User

interface IMSessionOperator {

    fun openSession(ctx: Context, session: Session)

    fun openUserPage(ctx: Context, user: User)
}