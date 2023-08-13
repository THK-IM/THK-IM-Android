package com.thk.im.android.ui.viewholder.msg

import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.thk.im.android.db.entity.Message
import com.thk.im.android.db.entity.Session
import com.thk.im.android.ui.R

class UnSupportMsgVH(liftOwner: LifecycleOwner, itemView: View, viewType: Int) :
    BaseMsgVH(liftOwner, itemView, viewType) {

    override fun getContentId(): Int {
        return R.layout.itemview_msg_text
    }

    override fun onViewBind(msg: Message, ses: Session) {
        super.onViewBind(msg, ses)
        val tvMsgContent: TextView = contentContainer.findViewById(R.id.tv_msg_content)
        tvMsgContent.text = "不支持的消息类型，请尝试升级客户端"
        when (getType()) {
            MsgPosType.Left.value -> {
                tvMsgContent.setTextColor(
                    ContextCompat.getColor(
                        tvMsgContent.context,
                        R.color.main_text_color
                    )
                )
                tvMsgContent.setBackgroundResource(R.drawable.chat_bg_1)
            }

            MsgPosType.Right.value -> {
                tvMsgContent.setTextColor(
                    ContextCompat.getColor(
                        tvMsgContent.context,
                        R.color.white
                    )
                )
                tvMsgContent.setBackgroundResource(R.drawable.chat_bg_3)
            }

            else -> {
                tvMsgContent.setTextColor(
                    ContextCompat.getColor(
                        tvMsgContent.context,
                        R.color.main_text_color
                    )
                )
                tvMsgContent.setBackgroundResource(R.drawable.bg_tv_msg_tips)
            }
        }
    }
}