package com.thk.im.android.ui.protocol

import com.thk.im.android.db.entity.Message
import com.thk.im.android.ui.protocol.internal.IMMsgSender

abstract class IMMessageOperator {
    abstract fun id(): String
    abstract fun title(): String
    abstract fun resId(): Int
    abstract fun operator(sender: IMMsgSender, message: Message)
}