package com.thk.im.android.ui.provider.msg.proccessor

import com.thk.im.android.core.processor.IMBaseMsgProcessor
import com.thk.im.android.core.db.MsgType


class IMTextMsgProcessor : IMBaseMsgProcessor() {

    override fun messageType(): Int {
        return MsgType.TEXT.value
    }
}