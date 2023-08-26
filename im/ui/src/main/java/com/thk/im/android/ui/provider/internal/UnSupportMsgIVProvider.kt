package com.thk.im.android.ui.provider.internal

import android.view.View
import androidx.lifecycle.LifecycleOwner
import com.thk.im.android.db.MsgType
import com.thk.im.android.ui.provider.MsgItemViewProvider
import com.thk.im.android.ui.viewholder.msg.BaseMsgVH
import com.thk.im.android.ui.viewholder.msg.UnSupportMsgVH

class UnSupportMsgIVProvider : MsgItemViewProvider() {

    override fun messageType(): Int {
        return MsgType.UnSupport.value
    }

    override fun createViewHolder(
        lifecycleOwner: LifecycleOwner,
        itemView: View,
        viewType: Int
    ): BaseMsgVH {
        return UnSupportMsgVH(lifecycleOwner, itemView, viewType)
    }
}