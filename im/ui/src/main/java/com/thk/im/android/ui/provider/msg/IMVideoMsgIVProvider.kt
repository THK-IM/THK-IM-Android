package com.thk.im.android.ui.provider.msg

import android.view.View
import androidx.lifecycle.LifecycleOwner
import com.thk.im.android.db.MsgType
import com.thk.im.android.ui.protocol.IMBaseMessageIVProvider
import com.thk.im.android.ui.fragment.viewholder.BaseMsgVH
import com.thk.im.android.ui.provider.msg.viewholder.IMVideoMsgVH

class IMVideoMsgIVProvider : IMBaseMessageIVProvider() {


    override fun messageType(): Int {
        return MsgType.VIDEO.value
    }

    override fun createViewHolder(
        lifecycleOwner: LifecycleOwner,
        itemView: View,
        viewType: Int
    ): BaseMsgVH {
        return IMVideoMsgVH(lifecycleOwner, itemView, viewType)
    }
}