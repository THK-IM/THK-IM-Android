package com.thk.im.android.ui.provider.msg.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import com.google.gson.Gson
import com.thk.im.android.core.db.entity.Message
import com.thk.im.android.core.db.entity.Session
import com.thk.im.android.ui.R
import com.thk.im.android.ui.databinding.ViewMsgRecordBinding
import com.thk.im.android.ui.manager.IMRecordMsgBody
import com.thk.im.android.ui.msg.view.IMsgView
import com.thk.im.android.ui.protocol.internal.IMMsgVHOperator

class IMRecordMsgView : LinearLayout, IMsgView {

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
        message: Message,
        session: Session?,
        delegate: IMMsgVHOperator?,
        isReply: Boolean
    ) {
        val recordBody = Gson().fromJson(message.content, IMRecordMsgBody::class.java)
        binding.tvRecordTitle.text = recordBody.title
        binding.tvRecordContent.text = recordBody.content
    }
}