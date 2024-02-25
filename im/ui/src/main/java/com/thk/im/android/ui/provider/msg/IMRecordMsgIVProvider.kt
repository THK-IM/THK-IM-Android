package com.thk.im.android.ui.provider.msg

import android.view.View
import androidx.lifecycle.LifecycleOwner
import com.thk.im.android.core.MsgType
import com.thk.im.android.ui.msg.viewholder.BaseMsgVH
import com.thk.im.android.ui.protocol.IMBaseMessageIVProvider
import com.thk.im.android.ui.provider.msg.viewholder.IMRecordMsgVH

class IMRecordMsgIVProvider : IMBaseMessageIVProvider() {
    override fun messageType(): Int {
        return MsgType.RECORD.value
    }

    override fun hasBubble(): Boolean {
        return true
    }

    override fun canSelect(): Boolean {
        return true
    }

    override fun createViewHolder(
        lifecycleOwner: LifecycleOwner,
        itemView: View,
        viewType: Int
    ): BaseMsgVH {
        return IMRecordMsgVH(lifecycleOwner, itemView, viewType)
    }
}