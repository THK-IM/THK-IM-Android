package com.thk.im.android.ui.provider.msg.proccessor

import com.thk.im.android.core.MsgType
import com.thk.im.android.core.db.entity.Message
import com.thk.im.android.core.processor.IMBaseMsgProcessor

class IMUnSupportMsgProcessor : IMBaseMsgProcessor() {

    override fun messageType(): Int {
        return MsgType.UnSupport.value
    }

    override fun msgDesc(msg: Message): String {
        return "[未知消息]"
    }

}