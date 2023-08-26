package com.thk.im.android.ui.provider.internal

import android.view.View
import androidx.lifecycle.LifecycleOwner
import com.thk.im.android.db.MsgType
import com.thk.im.android.ui.provider.MsgItemViewProvider
import com.thk.im.android.ui.viewholder.msg.BaseMsgVH
import com.thk.im.android.ui.viewholder.msg.TextMsgVH

class TextMsgIVProvider : MsgItemViewProvider() {

    override fun messageType(): Int {
        return MsgType.TEXT.value
    }

    override fun createViewHolder(
        lifecycleOwner: LifecycleOwner,
        itemView: View,
        viewType: Int
    ): BaseMsgVH {
        return TextMsgVH(lifecycleOwner, itemView, viewType)
    }
}