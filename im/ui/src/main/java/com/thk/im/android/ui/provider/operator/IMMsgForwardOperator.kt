package com.thk.im.android.ui.provider.operator

import com.thk.im.android.core.IMCoreManager
import com.thk.im.android.core.MsgType
import com.thk.im.android.core.db.entity.Message
import com.thk.im.android.core.db.entity.Session
import com.thk.im.android.ui.R
import com.thk.im.android.ui.manager.IMChatFunction
import com.thk.im.android.ui.manager.IMUIManager
import com.thk.im.android.ui.protocol.IMMessageOperator
import com.thk.im.android.ui.protocol.internal.IMMsgSender

class IMMsgForwardOperator : IMMessageOperator() {
    override fun id(): String {
        return "Forward"
    }

    override fun title(): String {
        return IMCoreManager.app.getString(R.string.forward)
    }

    override fun resId(): Int {
        return R.drawable.ic_msg_opr_forward
    }

    override fun onClick(sender: IMMsgSender, message: Message) {
        sender.forwardMessageToSession(arrayListOf(message), 0)
    }

    override fun supportMessage(message: Message, session: Session): Boolean {
        if (message.type == MsgType.Revoke.value) {
            return false
        }
        return IMUIManager.uiResourceProvider?.supportFunction(
            session,
            IMChatFunction.Forward.value
        ) ?: true
    }
}