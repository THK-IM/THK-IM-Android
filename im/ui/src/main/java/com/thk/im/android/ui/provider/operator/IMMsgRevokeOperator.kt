package com.thk.im.android.ui.provider.operator

import com.thk.im.android.core.IMCoreManager
import com.thk.im.android.core.IMSendMsgCallback
import com.thk.im.android.core.MsgType
import com.thk.im.android.core.db.entity.Message
import com.thk.im.android.core.db.entity.Session
import com.thk.im.android.ui.R
import com.thk.im.android.ui.protocol.IMMessageOperator
import com.thk.im.android.ui.protocol.internal.IMMsgSender

class IMMsgRevokeOperator : IMMessageOperator() {
    override fun id(): String {
        return "Revoke"
    }

    override fun title(): String {
        return IMCoreManager.app.getString(R.string.revoke)
    }

    override fun resId(): Int {
        return R.drawable.ic_msg_opr_revoke
    }

    override fun onClick(sender: IMMsgSender, message: Message) {
        val callback = object : IMSendMsgCallback {
            override fun onResult(message: Message, e: Exception?) {
                if (e != null) {
                    sender.showMessage(IMCoreManager.app.getString(R.string.revoke_failed), false)
                } else {
                    sender.showMessage(IMCoreManager.app.getString(R.string.revoke_success), false)
                }
            }
        }
        IMCoreManager.messageModule
            .getMsgProcessor(MsgType.Revoke.value)
            .send(message, false, callback)
    }

    override fun supportMessage(message: Message, session: Session): Boolean {
        if (message.type == MsgType.Revoke.value) {
            return false
        }
        if (message.fUid != IMCoreManager.uId) {
            return false
        }
        // 超过120s不允许撤回
        if (kotlin.math.abs(message.cTime - IMCoreManager.severTime) > 1000 * 120) {
            return false
        }
        return true
    }
}