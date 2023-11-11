package com.thk.im.android.ui.provider.internal.operator

import com.thk.im.android.db.entity.Message
import com.thk.im.android.ui.R
import com.thk.im.android.ui.protocol.IMMessageOperator
import com.thk.im.android.ui.protocol.internal.IMMsgSender

class IMMsgRevokeOperator: IMMessageOperator() {
    override fun id(): String {
        return "Revoke"
    }

    override fun title(): String {
        return "撤回"
    }

    override fun resId(): Int {
        return R.drawable.ic_keyboard
    }

    override fun operator(sender: IMMsgSender, message: Message) {
    }
}