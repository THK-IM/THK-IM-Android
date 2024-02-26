package com.thk.im.android.ui.msg.view

import android.view.ViewGroup
import com.thk.im.android.core.db.entity.Message
import com.thk.im.android.core.db.entity.Session
import com.thk.im.android.ui.protocol.internal.IMMsgVHOperator

interface IMsgView {

    fun setMessage(
        message: Message,
        session: Session?,
        delegate: IMMsgVHOperator?,
        isReply: Boolean = false
    )

    fun contentView(): ViewGroup

    fun onViewDetached() {}

    fun onViewDestroyed() {}


}