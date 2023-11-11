package com.thk.im.android.ui.provider.internal.operator

import com.thk.im.android.db.entity.Message
import com.thk.im.android.ui.R
import com.thk.im.android.ui.protocol.IMMessageOperator
import com.thk.im.android.ui.protocol.internal.IMMsgSender

class IMMsgForwardOperator : IMMessageOperator() {
    override fun id(): String {
        return "Forward"
    }

    override fun title(): String {
        return "转发"
    }

    override fun resId(): Int {
        return R.drawable.icon_msg_operate_forward
    }

    override fun operator(sender: IMMsgSender, message: Message) {
    }
}