package com.thk.im.android.ui.provider.operator

import com.thk.im.android.core.db.entity.Message
import com.thk.im.android.ui.R
import com.thk.im.android.ui.protocol.IMMessageOperator
import com.thk.im.android.ui.protocol.internal.IMMsgSender

class IMMsgMultiSelectOperator: IMMessageOperator() {
    override fun id(): String {
        return "MultiSelect"
    }

    override fun title(): String {
        return "多选"
    }

    override fun resId(): Int {
        return R.drawable.ic_msg_opr_mulit_select
    }

    override fun onClick(sender: IMMsgSender, message: Message) {
        sender.setSelectMode(true, message)
    }

    override fun supportMessage(message: Message): Boolean {
        return true
    }
}