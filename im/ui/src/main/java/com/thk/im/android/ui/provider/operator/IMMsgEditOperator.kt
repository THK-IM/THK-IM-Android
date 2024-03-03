package com.thk.im.android.ui.provider.operator

import com.thk.im.android.core.IMCoreManager
import com.thk.im.android.core.MsgType
import com.thk.im.android.core.db.entity.Message
import com.thk.im.android.ui.R
import com.thk.im.android.ui.protocol.IMMessageOperator
import com.thk.im.android.ui.protocol.internal.IMMsgSender

class IMMsgEditOperator : IMMessageOperator() {
    override fun id(): String {
        return "Edit"
    }

    override fun title(): String {
        return "编辑"
    }

    override fun resId(): Int {
        return R.drawable.ic_msg_opr_edit
    }

    override fun onClick(sender: IMMsgSender, message: Message) {
        if (message.type != MsgType.Text.value) {
            return
        }
        sender.reeditMessage(message)
    }

    override fun supportMessage(message: Message): Boolean {
        return (message.type == MsgType.Text.value && message.fUid == IMCoreManager.uId)
    }
}