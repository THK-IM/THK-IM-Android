package com.thk.im.android.core.processor

import com.thk.im.android.db.MsgType
import com.thk.im.android.db.entity.Message

class RevokeMessageProcessor : BaseMsgProcessor() {
    override fun messageType(): Int {
        return MsgType.Revoke.value
    }

    override fun send(msg: Message, resend: Boolean) {

    }

    override fun received(msg: Message) {

    }
}