package com.thk.im.android.ui.protocol

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import com.thk.im.android.core.IMCoreManager
import com.thk.im.android.core.db.entity.Message
import com.thk.im.android.ui.R
import com.thk.im.android.ui.fragment.view.IMsgBodyView
import com.thk.im.android.ui.fragment.viewholder.msg.IMBaseMsgVH
import com.thk.im.android.ui.manager.IMMsgPosType

abstract class IMBaseMessageIVProvider {

    /**
     * 消息类型, ex: 文本(1)
     */
    abstract fun messageType(): Int

    open fun hasBubble() : Boolean {
        return false
    }

    open fun canSelect(): Boolean {
        return false
    }

    open fun getSelfId(): Long {
        return IMCoreManager.uId
    }

    /**
     * 视图类型,一种消息类型可注册多种视图类型
     * ex: 文本消息类型(1),系统发的文本视图类型(3),他人发的文本视图类型(4),自己发的图片视图类型(5)
     * ex: 图片消息类型(2),系统发的图片视图类型(6),他人发的图片视图类型(7),自己发的图片视图类型(8)
     */
    open fun viewType(entity: Message): Int {
        val selfId = getSelfId()
        return when (entity.fUid) {
            0L -> {
                3 * messageType() + IMMsgPosType.Mid.value     // 中间显示 （一般为后台系统发送, 如 某某加入/退出了群聊）
            }

            selfId -> {
                3 * messageType() + IMMsgPosType.Right.value   // 右侧视图显示 (自己发送)
            }

            else -> {
                3 * messageType() + IMMsgPosType.Left.value    // 左侧视图显示（一般为他人发送）
            }
        }
    }

    /**
     * 返回消息视图实例
     */
    fun viewHolder(
        lifecycleOwner: LifecycleOwner,
        viewType: Int,
        parent: ViewGroup
    ): IMBaseMsgVH {
        val viewRes = getDefaultRes(viewType)
        val itemView = LayoutInflater.from(parent.context).inflate(viewRes, parent, false)
        return createViewHolder(lifecycleOwner, itemView, viewType)
    }

    abstract fun replyMsgView(context: Context): IMsgBodyView

    abstract fun createViewHolder(
        lifecycleOwner: LifecycleOwner,
        itemView: View,
        viewType: Int
    ): IMBaseMsgVH


    open fun getDefaultRes(viewType: Int): Int {
        val msgType = messageType()
        return when (viewType) {
            3 * msgType + 1 -> {
                R.layout.itemview_msg_left_container
            }

            3 * msgType + 2 -> {
                R.layout.itemview_msg_right_container
            }

            else -> {
                R.layout.itemview_msg_mid_container
            }
        }
    }


}