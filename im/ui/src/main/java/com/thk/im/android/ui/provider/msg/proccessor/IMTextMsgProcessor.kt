package com.thk.im.android.ui.provider.msg.proccessor

import com.thk.im.android.core.processor.BaseMsgProcessor
import com.thk.im.android.core.db.MsgType


class IMTextMsgProcessor : BaseMsgProcessor() {

    override fun messageType(): Int {
        return MsgType.TEXT.value
    }
}