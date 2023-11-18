package com.thk.im.android.core.processor

import com.thk.im.android.core.db.MsgType
import com.thk.im.android.core.db.entity.Message

class RevokeMessageProcessor : BaseMsgProcessor() {
    override fun messageType(): Int {
        return MsgType.Revoke.value
    }

    override fun send(msg: Message, resend: Boolean) {

    }

    override fun received(msg: Message) {

    }
}