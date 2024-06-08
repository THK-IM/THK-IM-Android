package com.thk.im.android.ui.provider.msg

import android.content.Context
import android.view.View
import androidx.lifecycle.LifecycleOwner
import com.thk.im.android.core.MsgType
import com.thk.im.android.ui.fragment.view.IMsgBodyView
import com.thk.im.android.ui.fragment.viewholder.msg.IMBaseMsgVH
import com.thk.im.android.ui.protocol.IMBaseMessageIVProvider
import com.thk.im.android.ui.provider.msg.view.IMImageMsgView
import com.thk.im.android.ui.provider.msg.viewholder.IMImageMsgVH

class IMImageMsgIVProvider : IMBaseMessageIVProvider() {

    override fun messageType(): Int {
        return MsgType.Image.value
    }

    override fun hasBubble(): Boolean {
        return false
    }

    override fun canSelect(): Boolean {
        return true
    }

    override fun replyMsgView(context: Context): IMsgBodyView {
        return IMImageMsgView(context)
    }

    override fun createViewHolder(
        lifecycleOwner: LifecycleOwner,
        itemView: View,
        viewType: Int
    ): IMBaseMsgVH {
        return IMImageMsgVH(lifecycleOwner, itemView, viewType)
    }
}