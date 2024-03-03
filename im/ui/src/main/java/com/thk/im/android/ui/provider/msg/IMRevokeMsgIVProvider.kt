package com.thk.im.android.ui.provider.msg

import android.content.Context
import android.view.View
import androidx.lifecycle.LifecycleOwner
import com.thk.im.android.core.MsgType
import com.thk.im.android.core.db.entity.Message
import com.thk.im.android.ui.fragment.view.IMsgView
import com.thk.im.android.ui.fragment.viewholder.msg.IMBaseMsgVH
import com.thk.im.android.ui.manager.IMMsgPosType
import com.thk.im.android.ui.protocol.IMBaseMessageIVProvider
import com.thk.im.android.ui.provider.msg.view.IMRevokeMsgView
import com.thk.im.android.ui.provider.msg.viewholder.IMRevokeMsgVHIM

class IMRevokeMsgIVProvider : IMBaseMessageIVProvider() {

    override fun messageType(): Int {
        return MsgType.Revoke.value
    }

    override fun hasBubble(): Boolean {
        return true
    }

    override fun canSelect(): Boolean {
        return true
    }

    override fun viewType(entity: Message): Int {
        // 撤回消息固定在中间显示
        return 3 * messageType() + IMMsgPosType.Mid.value
    }

    override fun replyMsgView(context: Context): IMsgView {
        return IMRevokeMsgView(context)
    }

    override fun createViewHolder(
        lifecycleOwner: LifecycleOwner,
        itemView: View,
        viewType: Int
    ): IMBaseMsgVH {
        return IMRevokeMsgVHIM(lifecycleOwner, itemView, viewType)
    }
}