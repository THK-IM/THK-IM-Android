package com.thk.im.android.ui.provider.function

import com.thk.im.android.core.db.entity.Session
import com.thk.im.android.ui.R
import com.thk.im.android.ui.manager.IMChatFunction
import com.thk.im.android.ui.protocol.IMBaseFunctionIVProvider
import com.thk.im.android.ui.protocol.internal.IMMsgSender

class IMCameraFunctionIVProvider: IMBaseFunctionIVProvider() {
    override fun position(): Int {
        return 0
    }

    override fun iconResId(): Int {
        return R.drawable.ic_msg_camera
    }

    override fun title(): String {
        return "拍照"
    }

    override fun click(sender: IMMsgSender) {
        sender.openCamera()
    }

    override fun supportSession(session: Session): Boolean {
        return (session.functionFlag.and(IMChatFunction.Image.value) != 0L) ||
                (session.functionFlag.and(IMChatFunction.Video.value) != 0L)
    }
}