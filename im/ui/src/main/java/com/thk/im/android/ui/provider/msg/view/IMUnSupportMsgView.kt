package com.thk.im.android.ui.provider.msg.view

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import com.thk.im.android.core.db.entity.Message
import com.thk.im.android.core.db.entity.Session
import com.thk.im.android.ui.R
import com.thk.im.android.ui.databinding.ViewMsgTextBinding
import com.thk.im.android.ui.fragment.view.IMsgBodyView
import com.thk.im.android.ui.protocol.internal.IMMsgVHOperator

class IMUnSupportMsgView : LinearLayout, IMsgBodyView {

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
        positionType: Int,
        message: Message,
        session: Session?,
        delegate: IMMsgVHOperator?,
        isReply: Boolean
    ) {
        binding.tvMsgContent.text = context.getString(R.string.not_support_msg_update_client)
        when (message.fUid) {
            0L -> {
                if (isReply) {
                    binding.tvMsgContent.textSize = 12.0f
                    binding.tvMsgContent.setTextColor(Color.parseColor("#0A0E10"))
                } else {
                    binding.tvMsgContent.textSize = 12.0f
                    binding.tvMsgContent.setTextColor(Color.parseColor("#FFFFFF"))
                }
            }

            else -> {
                if (isReply) {
                    binding.tvMsgContent.textSize = 12.0f
                    binding.tvMsgContent.setTextColor(Color.parseColor("#FFFFFF"))
                } else {
                    binding.tvMsgContent.textSize = 16.0f
                    binding.tvMsgContent.setTextColor(Color.parseColor("#0A0E10"))
                }
            }
        }
    }
}