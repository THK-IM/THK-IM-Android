package com.thk.im.android.ui.protocol

import com.thk.im.android.core.db.entity.Session
import com.thk.im.android.ui.protocol.internal.IMMsgSender

abstract class IMBaseFunctionIVProvider {

    abstract fun position(): Int

    abstract fun iconResId(): Int

    abstract fun title(): String

    abstract fun click(sender: IMMsgSender)

    abstract fun supportSession(session: Session): Boolean

}