package com.thk.im.android.ui.provider.msg

import android.view.View
import androidx.lifecycle.LifecycleOwner
import com.thk.im.android.core.db.MsgType
import com.thk.im.android.ui.protocol.IMBaseMessageIVProvider
import com.thk.im.android.ui.fragment.viewholder.BaseMsgVH
import com.thk.im.android.ui.provider.msg.viewholder.IMUnSupportMsgVH

class IMUnSupportMsgIVProvider : IMBaseMessageIVProvider() {

    override fun messageType(): Int {
        return MsgType.UnSupport.value
    }

    override fun createViewHolder(
        lifecycleOwner: LifecycleOwner,
        itemView: View,
        viewType: Int
    ): BaseMsgVH {
        return IMUnSupportMsgVH(lifecycleOwner, itemView, viewType)
    }
}