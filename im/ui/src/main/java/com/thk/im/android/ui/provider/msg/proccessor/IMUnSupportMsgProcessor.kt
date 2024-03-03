package com.thk.im.android.ui.provider.msg.proccessor

import com.thk.im.android.core.MsgType
import com.thk.im.android.core.processor.IMBaseMsgProcessor

class IMUnSupportMsgProcessor : IMBaseMsgProcessor() {

    override fun messageType(): Int {
        return MsgType.UnSupport.value
    }

}