package com.thk.im.android.core.processor

import com.thk.im.android.db.MsgType


class TextMsgProcessor : BaseMsgProcessor() {

    override fun messageType(): Int {
        return MsgType.TEXT.value
    }
}