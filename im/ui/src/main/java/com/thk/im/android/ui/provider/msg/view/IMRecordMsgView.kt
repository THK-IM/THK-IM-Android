package com.thk.im.android.ui.provider.msg.view

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import com.google.gson.Gson
import com.thk.im.android.core.db.entity.Message
import com.thk.im.android.core.db.entity.Session
import com.thk.im.android.ui.R
import com.thk.im.android.ui.databinding.ViewMsgRecordBinding
import com.thk.im.android.ui.fragment.view.IMsgBodyView
import com.thk.im.android.ui.manager.IMRecordMsgBody
import com.thk.im.android.ui.protocol.internal.IMMsgVHOperator

class IMRecordMsgView : LinearLayout, IMsgBodyView {

    private var binding: ViewMsgRecordBinding

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    init {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.view_msg_record, this, true)
        binding = ViewMsgRecordBinding.bind(view)
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
        val recordBody = Gson().fromJson(message.content, IMRecordMsgBody::class.java)
        binding.tvRecordTitle.text = recordBody.title
        binding.tvRecordContent.text = recordBody.content

        if (isReply) {
            binding.tvRecordTitle.textSize = 12.0f
            binding.tvRecordTitle.setTextColor(Color.parseColor("#ff999999"))
            binding.tvRecordContent.textSize = 12.0f
            binding.tvRecordContent.setTextColor(Color.parseColor("#ff999999"))
        }
    }
}