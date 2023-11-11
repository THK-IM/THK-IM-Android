package com.thk.im.android.ui.provider.internal.msg

import android.view.View
import androidx.lifecycle.LifecycleOwner
import com.thk.im.android.db.MsgType
import com.thk.im.android.ui.provider.IMBaseMessageIVProvider
import com.thk.im.android.ui.fragment.viewholder.BaseMsgVH
import com.thk.im.android.ui.provider.internal.msg.viewholder.IMAudioMsgVH

class IMAudioMsgIVProvider : IMBaseMessageIVProvider() {
    override fun messageType(): Int {
        return MsgType.Audio.value
    }


    override fun createViewHolder(
        lifecycleOwner: LifecycleOwner,
        itemView: View,
        viewType: Int
    ): BaseMsgVH {
        return IMAudioMsgVH(lifecycleOwner, itemView, viewType)
    }
}