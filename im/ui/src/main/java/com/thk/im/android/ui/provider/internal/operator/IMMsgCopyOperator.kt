package com.thk.im.android.ui.provider.internal.operator

import com.thk.im.android.db.entity.Message
import com.thk.im.android.ui.R
import com.thk.im.android.ui.protocol.IMMessageOperator
import com.thk.im.android.ui.protocol.internal.IMMsgSender

class IMMsgCopyOperator: IMMessageOperator() {
    override fun id(): String {
        return "Copy"
    }

    override fun title(): String {
        return "复制"
    }

    override fun resId(): Int {
        return R.drawable.icon_love
    }

    override fun operator(sender: IMMsgSender, message: Message) {
        // TODO
    }
}