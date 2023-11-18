package com.thk.im.android.ui.provider.operator

import com.thk.im.android.core.IMCoreManager
import com.thk.im.android.core.IMSendMsgCallback
import com.thk.im.android.core.db.MsgType
import com.thk.im.android.core.db.entity.Message
import com.thk.im.android.ui.R
import com.thk.im.android.ui.protocol.IMMessageOperator
import com.thk.im.android.ui.protocol.internal.IMMsgSender

class IMMsgRevokeOperator : IMMessageOperator() {
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
        val callback = object : IMSendMsgCallback {
            override fun onStart() {
                sender.showLoading("正在撤回")
            }

            override fun onResult(e: Exception?) {
                if (e != null) {
                    sender.showMessage("撤回失败", false)
                } else {
                    sender.showMessage("撤回成功", false)
                }
            }


        }
        IMCoreManager.getMessageModule()
            .getMsgProcessor(MsgType.Revoke.value)
            .send(message, false, callback)
    }
}