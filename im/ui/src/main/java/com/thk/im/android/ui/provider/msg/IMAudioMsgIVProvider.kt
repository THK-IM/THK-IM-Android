package com.thk.im.android.ui.provider.msg

import android.content.Context
import android.view.View
import androidx.lifecycle.LifecycleOwner
import com.thk.im.android.core.MsgType
import com.thk.im.android.ui.fragment.view.IMsgBodyView
import com.thk.im.android.ui.fragment.viewholder.msg.IMBaseMsgVH
import com.thk.im.android.ui.manager.IMMsgPosType
import com.thk.im.android.ui.protocol.IMBaseMessageIVProvider
import com.thk.im.android.ui.provider.msg.view.IMAudioMsgView
import com.thk.im.android.ui.provider.msg.viewholder.IMAudioMsgVH

open class IMAudioMsgIVProvider : IMBaseMessageIVProvider() {

    override fun messageType(): Int {
        return MsgType.Audio.value
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
    ): IMBaseMsgVH {
        return IMAudioMsgVH(lifecycleOwner, itemView, viewType)
    }

    override fun msgBodyView(context: Context, position: IMMsgPosType): IMsgBodyView {
        val v = IMAudioMsgView(context)
        v.setPosition(position)
        return v
    }
}