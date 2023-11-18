package com.thk.im.android.ui.provider.msg

import android.view.View
import androidx.lifecycle.LifecycleOwner
import com.thk.im.android.core.db.MsgType
import com.thk.im.android.core.db.entity.Message
import com.thk.im.android.ui.fragment.viewholder.BaseMsgVH
import com.thk.im.android.ui.manager.IMMsgPosType
import com.thk.im.android.ui.protocol.IMBaseMessageIVProvider
import com.thk.im.android.ui.provider.msg.viewholder.IMRevokeMsgVH

class IMRevokeMsgIVProvider : IMBaseMessageIVProvider() {
    override fun messageType(): Int {
        return MsgType.Revoke.value
    }

    override fun viewType(entity: Message): Int {
        // 撤回消息固定在中间显示
        return 3 * messageType() + IMMsgPosType.Mid.value
    }

    override fun createViewHolder(
        lifecycleOwner: LifecycleOwner,
        itemView: View,
        viewType: Int
    ): BaseMsgVH {
        return IMRevokeMsgVH(lifecycleOwner, itemView, viewType)
    }
}