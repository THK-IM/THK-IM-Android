package com.thk.im.android.ui.protocol

import com.thk.im.android.core.db.entity.Message
import com.thk.im.android.ui.protocol.internal.IMMsgSender

abstract class IMMessageOperator {
    abstract fun id(): String
    abstract fun title(): String
    abstract fun resId(): Int
    abstract fun onClick(sender: IMMsgSender, message: Message)
    abstract fun supportMessage(message: Message): Boolean
}