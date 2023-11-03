package com.thk.im.android.ui.provider.internal.msg.viewholder

import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.thk.im.android.db.entity.Message
import com.thk.im.android.db.entity.Session
import com.thk.im.android.ui.R
import com.thk.im.android.ui.fragment.adapter.ViewHolderSelect
import com.thk.im.android.ui.fragment.viewholder.BaseMsgVH
import com.thk.im.android.ui.manager.IMMsgPosType
import com.thk.im.android.ui.protocol.internal.IMMsgVHOperator

class UnSupportMsgVH(liftOwner: LifecycleOwner, itemView: View, viewType: Int) :
    BaseMsgVH(liftOwner, itemView, viewType) {

    override fun getContentId(): Int {
        return R.layout.itemview_msg_text
    }

    override fun onViewBind(
        position: Int,
        messages: List<Message>,
        session: Session,
        msgVHOperator: IMMsgVHOperator,
        viewHolderSelect: ViewHolderSelect
    ) {
        super.onViewBind(position, messages, session, msgVHOperator, viewHolderSelect)
        val tvMsgContent: TextView = itemView.findViewById(R.id.tv_msg_content)
        tvMsgContent.text = "不支持的消息类型，请尝试升级客户端"
        when (getPositionType()) {
            IMMsgPosType.Left.value -> {
                tvMsgContent.setTextColor(
                    ContextCompat.getColor(
                        tvMsgContent.context,
                        R.color.font_main
                    )
                )
                tvMsgContent.setBackgroundResource(R.drawable.chat_bg_1)
            }

            IMMsgPosType.Right.value -> {
                tvMsgContent.setTextColor(
                    ContextCompat.getColor(
                        tvMsgContent.context,
                        R.color.font_bg_main
                    )
                )
                tvMsgContent.setBackgroundResource(R.drawable.chat_bg_3)
            }

            else -> {
                tvMsgContent.setTextColor(
                    ContextCompat.getColor(
                        tvMsgContent.context,
                        R.color.hint_font_main
                    )
                )
                tvMsgContent.setBackgroundResource(R.drawable.bg_tv_msg_tips)
            }
        }
    }
}