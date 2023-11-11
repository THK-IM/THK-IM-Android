package com.thk.im.android.ui.provider.internal.msg.proccessor

import com.thk.im.android.core.processor.BaseMsgProcessor
import com.thk.im.android.db.MsgType

class IMUnSupportMsgProcessor : BaseMsgProcessor() {

    override fun messageType(): Int {
        return MsgType.UnSupport.value
    }

}