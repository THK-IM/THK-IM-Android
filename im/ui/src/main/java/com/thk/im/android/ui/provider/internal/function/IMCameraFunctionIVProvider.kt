package com.thk.im.android.ui.provider.internal.function

import com.thk.im.android.ui.R
import com.thk.im.android.ui.protocol.IMMsgSender
import com.thk.im.android.ui.provider.IMBaseFunctionIVProvider

class IMCameraFunctionIVProvider: IMBaseFunctionIVProvider() {
    override fun position(): Int {
        return 0
    }

    override fun iconResId(): Int {
        return R.drawable.chat_camera
    }

    override fun title(): String {
        return "拍照"
    }

    override fun click(sender: IMMsgSender) {
        sender.openCamera()
    }
}