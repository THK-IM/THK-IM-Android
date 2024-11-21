package com.thk.im.android.module.msg.call

import android.view.View
import androidx.lifecycle.LifecycleOwner
import com.thk.im.android.constant.DemoMsgType
import com.thk.im.android.ui.fragment.viewholder.msg.IMBaseMsgVH
import com.thk.im.android.ui.protocol.IMBaseMessageIVProvider

class IMCallMsgProvider : IMBaseMessageIVProvider() {

    override fun messageType(): Int {
        return DemoMsgType.Call.value
    }

    override fun createViewHolder(
        lifecycleOwner: LifecycleOwner,
        itemView: View,
        viewType: Int
    ): IMBaseMsgVH {
        return IMCallMsgVH(lifecycleOwner, itemView, viewType)
    }

    override fun hasBubble(): Boolean {
        return true
    }
}