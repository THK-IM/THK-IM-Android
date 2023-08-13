package com.thk.im.android.core.processor

import com.thk.im.android.core.bean.MessageBean
import com.thk.im.android.db.entity.Message
import com.thk.im.android.db.entity.MsgType


class TextMsgProcessor : BaseMsgProcessor() {

    override fun messageType(): Int {
        return MsgType.TEXT.value
    }

    override fun msgBean2Entity(bean: MessageBean): Message {
        return super.msgBean2Entity(bean)
    }

    override fun entity2MsgBean(msg: Message): MessageBean {
        msg.sid
        return super.entity2MsgBean(msg)
    }
}