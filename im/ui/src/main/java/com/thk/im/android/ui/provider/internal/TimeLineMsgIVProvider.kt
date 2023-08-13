package com.thk.im.android.ui.provider.internal

import android.view.View
import androidx.lifecycle.LifecycleOwner
import com.thk.im.android.ui.R
import com.thk.im.android.ui.provider.MsgItemViewProvider
import com.thk.im.android.ui.viewholder.msg.BaseMsgVH
import com.thk.im.android.ui.viewholder.msg.TimeLineMsgVH

class TimeLineMsgIVProvider : MsgItemViewProvider() {

    override fun messageType(): Int {
        return 9999
    }

    override fun createViewHolder(
        lifecycleOwner: LifecycleOwner,
        itemView: View,
        viewType: Int
    ): BaseMsgVH {
        return TimeLineMsgVH(lifecycleOwner, itemView, messageType())
    }
}