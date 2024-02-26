package com.thk.im.android.ui.provider.msg

import android.content.Context
import android.view.View
import androidx.lifecycle.LifecycleOwner
import com.thk.im.android.core.MsgType
import com.thk.im.android.ui.msg.view.IMsgView
import com.thk.im.android.ui.msg.viewholder.BaseMsgVH
import com.thk.im.android.ui.protocol.IMBaseMessageIVProvider
import com.thk.im.android.ui.provider.msg.view.IMUnSupportMsgView
import com.thk.im.android.ui.provider.msg.viewholder.IMUnSupportMsgVH

class IMUnSupportMsgIVProvider : IMBaseMessageIVProvider() {

    override fun messageType(): Int {
        return MsgType.UnSupport.value
    }

    override fun hasBubble(): Boolean {
        return true
    }

    override fun canSelect(): Boolean {
        return true
    }

    override fun replyMsgView(context: Context): IMsgView {
        return IMUnSupportMsgView(context)
    }

    override fun createViewHolder(
        lifecycleOwner: LifecycleOwner,
        itemView: View,
        viewType: Int
    ): BaseMsgVH {
        return IMUnSupportMsgVH(lifecycleOwner, itemView, viewType)
    }
}