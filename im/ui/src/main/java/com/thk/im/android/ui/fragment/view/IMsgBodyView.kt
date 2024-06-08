package com.thk.im.android.ui.fragment.view

import android.view.ViewGroup
import com.thk.im.android.core.db.entity.Message
import com.thk.im.android.core.db.entity.Session
import com.thk.im.android.ui.protocol.internal.IMMsgVHOperator

interface IMsgBodyView {

    fun setMessage(
        positionType: Int,
        message: Message,
        session: Session?,
        delegate: IMMsgVHOperator?,
        isReply: Boolean = false
    )

    fun contentView(): ViewGroup

    fun onViewDetached() {}

    fun onViewDestroyed() {}


}