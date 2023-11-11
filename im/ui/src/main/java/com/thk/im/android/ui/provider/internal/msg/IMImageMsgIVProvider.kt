package com.thk.im.android.ui.provider.internal.msg

import android.view.View
import androidx.lifecycle.LifecycleOwner
import com.thk.im.android.db.MsgType
import com.thk.im.android.ui.provider.IMBaseMessageIVProvider
import com.thk.im.android.ui.fragment.viewholder.BaseMsgVH
import com.thk.im.android.ui.provider.internal.msg.viewholder.IMImageMsgVH

class IMImageMsgIVProvider : IMBaseMessageIVProvider() {
    override fun messageType(): Int {
        return MsgType.IMAGE.value
    }

    override fun createViewHolder(
        lifecycleOwner: LifecycleOwner,
        itemView: View,
        viewType: Int
    ): BaseMsgVH {
        return IMImageMsgVH(lifecycleOwner, itemView, viewType)
    }
}