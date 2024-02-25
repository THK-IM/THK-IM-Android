package com.thk.im.android.ui.provider.msg

import android.view.View
import androidx.lifecycle.LifecycleOwner
import com.thk.im.android.ui.fragment.viewholder.BaseMsgVH
import com.thk.im.android.ui.protocol.IMBaseMessageIVProvider
import com.thk.im.android.ui.provider.msg.viewholder.IMTimeLineMsgVH

class IMTimeLineMsgIVProvider : IMBaseMessageIVProvider() {

    companion object {
        const val timeLineMsgType = 9999
    }

    override fun messageType(): Int {
        return timeLineMsgType
    }

    override fun hasBubble(): Boolean {
        return true
    }

    override fun canSelect(): Boolean {
        return false
    }

    override fun createViewHolder(
        lifecycleOwner: LifecycleOwner,
        itemView: View,
        viewType: Int
    ): BaseMsgVH {
        return IMTimeLineMsgVH(lifecycleOwner, itemView, messageType())
    }
}