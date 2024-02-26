package com.thk.im.android.ui.provider.msg.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import com.thk.im.android.core.db.entity.Message
import com.thk.im.android.core.db.entity.Session
import com.thk.im.android.ui.R
import com.thk.im.android.ui.databinding.ViewMsgTextBinding
import com.thk.im.android.ui.msg.view.IMsgView
import com.thk.im.android.ui.protocol.internal.IMMsgVHOperator

class IMUnSupportMsgView : LinearLayout, IMsgView {

    private var binding: ViewMsgTextBinding

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    init {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.view_msg_unsupport, this, true)
        binding = ViewMsgTextBinding.bind(view)
    }

    override fun contentView(): ViewGroup {
        return this
    }

    override fun setMessage(
        message: Message,
        session: Session?,
        delegate: IMMsgVHOperator?,
        isReply: Boolean
    ) {
        binding.tvMsgContent.text = "不支持的消息类型，请尝试升级客户端"
        when (message.fUid) {
            0L -> {
                binding.tvMsgContent.setTextColor(
                    ContextCompat.getColor(
                        context,
                        R.color.hint_font_main
                    )
                )
            }

            else -> {
                binding.tvMsgContent.setTextColor(
                    ContextCompat.getColor(
                        context,
                        R.color.font_main
                    )
                )
            }
        }
    }
}