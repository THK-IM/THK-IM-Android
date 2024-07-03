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

class IMMsgReplyOperator: IMMessageOperator() {
    override fun id(): String {
        return "Reply"
    }

    override fun title(): String {
        return IMCoreManager.app.getString(R.string.reply)
    }

    override fun resId(): Int {
        return R.drawable.ic_msg_opr_reply
    }

    override fun onClick(sender: IMMsgSender, message: Message) {
        sender.replyMessage(message)
    }

    override fun supportMessage(message: Message, session: Session): Boolean {
        if (message.type == MsgType.Revoke.value) {
            return false
        }
        if (message.fUid == 0L) {
            return false
        }
        return IMUIManager.uiResourceProvider?.supportFunction(session, IMChatFunction.Image.value)
            ?: true
    }
}