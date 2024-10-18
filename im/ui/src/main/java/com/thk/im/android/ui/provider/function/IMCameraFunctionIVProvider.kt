package com.thk.im.android.ui.provider.function

import com.thk.im.android.core.IMCoreManager
import com.thk.im.android.core.db.entity.Session
import com.thk.im.android.ui.R
import com.thk.im.android.ui.manager.IMChatFunction
import com.thk.im.android.ui.manager.IMUIManager
import com.thk.im.android.ui.protocol.IMBaseFunctionIVProvider
import com.thk.im.android.ui.protocol.internal.IMMsgSender

class IMCameraFunctionIVProvider : IMBaseFunctionIVProvider() {
    override fun position(): Int {
        return 0
    }

    override fun iconResId(): Int {
        return R.drawable.ic_msg_camera
    }

    override fun title(): String {
        return IMCoreManager.app.getString(R.string.camera)
    }

    override fun click(sender: IMMsgSender) {
        sender.openCamera()
    }

    override fun supportSession(session: Session): Boolean {
        return (IMUIManager.uiResourceProvider?.supportFunction(session, IMChatFunction.Image.value)
            ?: true) ||
                (IMUIManager.uiResourceProvider?.supportFunction(
                    session,
                    IMChatFunction.Video.value
                ) ?: true)
    }
}