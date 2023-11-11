package com.thk.im.android.ui.fragment.popup

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.thk.im.android.db.entity.Message
import com.thk.im.android.ui.R
import com.thk.im.android.ui.databinding.ViewMessageOperatorBinding
import com.thk.im.android.ui.protocol.IMMessageOperator
import com.thk.im.android.ui.protocol.internal.IMMsgSender

class MessageOperatorView : LinearLayout {

    private var binding: ViewMessageOperatorBinding

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int)
            : super(context, attrs, defStyleAttr)

    init {
        val view = LayoutInflater.from(context).inflate(R.layout.view_message_operator, this, true)
        binding = ViewMessageOperatorBinding.bind(view)
    }

    fun setOperator(operator: IMMessageOperator, sender: IMMsgSender, message: Message, clickListener: OnClickListener) {
        binding.ivMsgOpr.setImageResource(operator.resId())
        binding.tvMsgOpr.text = operator.title()
        binding.root.setOnClickListener {
            clickListener.onClick(binding.root)
            operator.operator(sender, message)
        }
    }

}