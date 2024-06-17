package com.thk.im.android.ui.provider.msg.proccessor

import com.thk.im.android.core.IMCoreManager
import com.thk.im.android.core.MsgType
import com.thk.im.android.core.db.entity.Message
import com.thk.im.android.core.processor.IMBaseMsgProcessor
import com.thk.im.android.ui.R

class IMUnSupportMsgProcessor : IMBaseMsgProcessor() {

    override fun messageType(): Int {
        return MsgType.UnSupport.value
    }

    override fun msgDesc(msg: Message): String {
        return IMCoreManager.app.getString(R.string.im_un_support_msg)
    }

}