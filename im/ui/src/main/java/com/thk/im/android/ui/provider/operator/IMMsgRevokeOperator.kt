package com.thk.im.android.ui.provider.operator

import com.thk.im.android.core.db.entity.Message
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

    override fun onClick(sender: IMMsgSender, message: Message) {
    }
}