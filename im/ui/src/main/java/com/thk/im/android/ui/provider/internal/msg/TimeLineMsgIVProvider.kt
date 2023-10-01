package com.thk.im.android.ui.provider.internal.msg

import android.view.View
import androidx.lifecycle.LifecycleOwner
import com.thk.im.android.ui.provider.IMBaseMessageIVProvider
import com.thk.im.android.ui.fragment.viewholder.BaseMsgVH
import com.thk.im.android.ui.provider.internal.msg.viewholder.TimeLineMsgVH

class TimeLineMsgIVProvider : IMBaseMessageIVProvider() {

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