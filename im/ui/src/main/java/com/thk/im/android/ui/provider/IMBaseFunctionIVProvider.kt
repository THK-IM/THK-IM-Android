package com.thk.im.android.ui.provider

import com.thk.im.android.ui.protocol.IMMsgSender

abstract class IMBaseFunctionIVProvider {

    abstract fun position(): Int

    abstract fun iconResId(): Int

    abstract fun title(): String

    abstract fun click(sender: IMMsgSender)

}