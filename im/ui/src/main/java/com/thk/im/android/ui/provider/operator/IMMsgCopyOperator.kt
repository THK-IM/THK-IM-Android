package com.thk.im.android.ui.provider.operator

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import com.thk.im.android.core.IMCoreManager
import com.thk.im.android.core.MsgType
import com.thk.im.android.core.db.entity.Message
import com.thk.im.android.core.db.entity.Session
import com.thk.im.android.ui.R
import com.thk.im.android.ui.protocol.IMMessageOperator
import com.thk.im.android.ui.protocol.internal.IMMsgSender

class IMMsgCopyOperator: IMMessageOperator() {
    override fun id(): String {
        return "Copy"
    }

    override fun title(): String {
        return IMCoreManager.app.getString(R.string.copy)
    }

    override fun resId(): Int {
        return R.drawable.ic_msg_opr_copy
    }

    override fun onClick(sender: IMMsgSender, message: Message) {
        if (message.type == MsgType.Text.value) {
            val clipboard = IMCoreManager.app
                .getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip: ClipData = ClipData.newPlainText("text", message.content)
            clipboard.setPrimaryClip(clip)
        }
    }

    override fun supportMessage(message: Message, session: Session): Boolean {
        return (message.type == MsgType.Text.value)
    }
}