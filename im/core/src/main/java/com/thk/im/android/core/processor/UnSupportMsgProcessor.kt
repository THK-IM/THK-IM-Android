package com.thk.im.android.core.processor

import com.thk.im.android.db.MsgType

class UnSupportMsgProcessor : BaseMsgProcessor() {

    override fun messageType(): Int {
        return MsgType.UnSupport.value
    }

}