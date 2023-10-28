package com.thk.im.android.ui.provider.internal.function

import com.thk.im.android.ui.R
import com.thk.im.android.ui.protocol.internal.IMMsgSender
import com.thk.im.android.ui.provider.IMBaseFunctionIVProvider

class IMAlbumFunctionIVProvider : IMBaseFunctionIVProvider() {
    override fun position(): Int {
        return 1
    }

    override fun iconResId(): Int {
        return R.drawable.chat_album
    }

    override fun title(): String {
        return "相册"
    }

    override fun click(sender: IMMsgSender) {
        sender.choosePhoto()
    }
}