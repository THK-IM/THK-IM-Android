package com.thk.im.android.ui.protocol

import android.content.Context
import com.thk.im.android.core.db.entity.Session

interface IMSessionOperator {

    fun openSession(ctx: Context, session: Session)
}