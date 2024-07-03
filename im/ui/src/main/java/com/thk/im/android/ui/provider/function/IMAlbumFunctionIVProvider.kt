package com.thk.im.android.ui.provider.function

import com.thk.im.android.core.IMCoreManager
import com.thk.im.android.core.db.entity.Session
import com.thk.im.android.ui.R
import com.thk.im.android.ui.manager.IMChatFunction
import com.thk.im.android.ui.manager.IMUIManager
import com.thk.im.android.ui.protocol.IMBaseFunctionIVProvider
import com.thk.im.android.ui.protocol.internal.IMMsgSender

class IMAlbumFunctionIVProvider : IMBaseFunctionIVProvider() {
    override fun position(): Int {
        return 1
    }

    override fun iconResId(): Int {
        return R.drawable.ic_msg_media
    }

    override fun title(): String {
        return IMCoreManager.app.getString(R.string.album)
    }

    override fun click(sender: IMMsgSender) {
        sender.choosePhoto()
    }

    override fun supportSession(session: Session): Boolean {
        return (IMUIManager.uiResourceProvider?.supportFunction(session, IMChatFunction.Image.value) ?: true) ||
                (IMUIManager.uiResourceProvider?.supportFunction(session, IMChatFunction.Video.value) ?: true)
    }
}