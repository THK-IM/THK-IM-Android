package com.thk.im.android.ui.provider.msg.proccessor

import com.thk.im.android.core.IMSendMsgCallback
import com.thk.im.android.core.db.MsgType
import com.thk.im.android.core.db.entity.Message
import com.thk.im.android.core.processor.IMBaseMsgProcessor

class IMReeditMessageProcessor: IMBaseMsgProcessor() {
    override fun messageType(): Int {
        return MsgType.Reedit.value
    }

    override fun send(msg: Message, resend: Boolean, callback: IMSendMsgCallback?) {

    }

    override fun received(msg: Message) {

    }
}