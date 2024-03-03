package com.thk.im.android.ui.fragment.view

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.core.view.children
import com.thk.im.android.core.base.extension.setShape
import com.thk.im.android.core.db.entity.Message
import com.thk.im.android.core.db.entity.Session
import com.thk.im.android.core.db.entity.User
import com.thk.im.android.ui.R
import com.thk.im.android.ui.databinding.ViewReplyMsgContainerBinding
import com.thk.im.android.ui.manager.IMUIManager
import com.thk.im.android.ui.protocol.internal.IMMsgVHOperator

class IMReplyMsgContainerView : LinearLayout {

    private val binding: ViewReplyMsgContainerBinding

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int)
            : super(context, attrs, defStyleAttr)

    init {
        val view =
            LayoutInflater.from(context).inflate(R.layout.view_reply_msg_container, this, true)
        binding = ViewReplyMsgContainerBinding.bind(view)
        binding.tvReplyMsgUserNick.setTextColor(Color.parseColor("#ff08AAFF"))
        binding.vReplyMsgLine.setShape(
            Color.parseColor("#ff08AAFF"), floatArrayOf(2f, 2f, 2f, 2f), false
        )
    }

    fun setMessage(user: User, message: Message, session: Session, delegate: IMMsgVHOperator?) {
        binding.tvReplyMsgUserNick.text = user.nickname
        binding.flReplyContent.children.forEach {
            binding.flReplyContent.removeView(it)
        }
        val view = IMUIManager.getMsgIVProviderByMsgType(message.type).replyMsgView(context)
        view.setMessage(message, session, delegate, true)
        binding.flReplyContent.addView(view.contentView())
    }
}