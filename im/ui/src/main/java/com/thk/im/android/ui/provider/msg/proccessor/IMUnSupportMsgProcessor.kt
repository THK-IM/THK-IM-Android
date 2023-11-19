package com.thk.im.android.ui.provider.msg.proccessor

import com.thk.im.android.core.processor.IMBaseMsgProcessor
import com.thk.im.android.core.db.MsgType

class IMUnSupportMsgProcessor : IMBaseMsgProcessor() {

    override fun messageType(): Int {
        return MsgType.UnSupport.value
    }

}