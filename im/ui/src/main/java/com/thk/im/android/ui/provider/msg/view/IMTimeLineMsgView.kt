package com.thk.im.android.ui.provider.msg.view

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import com.thk.im.android.core.IMCoreManager
import com.thk.im.android.core.base.utils.DateUtils
import com.thk.im.android.core.db.entity.Message
import com.thk.im.android.core.db.entity.Session
import com.thk.im.android.ui.R
import com.thk.im.android.ui.databinding.ViewMsgTimelineBinding
import com.thk.im.android.ui.fragment.view.IMsgBodyView
import com.thk.im.android.ui.protocol.internal.IMMsgVHOperator

class IMTimeLineMsgView : LinearLayout, IMsgBodyView {

    private var binding: ViewMsgTimelineBinding

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    init {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.view_msg_timeline, this, true)
        binding = ViewMsgTimelineBinding.bind(view)
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
        if (isReply) {
            binding.tvMsgContent.textSize = 12.0f
            binding.tvMsgContent.setTextColor(Color.parseColor("#ff999999"))
        } else {
            binding.tvMsgContent.textSize = 14.0f
            binding.tvMsgContent.setTextColor(Color.WHITE)
        }
        binding.tvMsgContent.text =
            DateUtils.timeToMsgTime(message.cTime, IMCoreManager.severTime)
    }
}