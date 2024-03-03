package com.thk.im.android.ui.fragment.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.thk.im.android.core.db.entity.Message
import com.thk.im.android.ui.R
import com.thk.im.android.ui.databinding.ItemViewMessageOperatorBinding
import com.thk.im.android.ui.protocol.IMMessageOperator
import com.thk.im.android.ui.protocol.internal.IMMsgSender

class IMMessageOperatorItemView : LinearLayout {

    private var binding: ItemViewMessageOperatorBinding

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int)
            : super(context, attrs, defStyleAttr)

    init {
        val view = LayoutInflater.from(context).inflate(R.layout.item_view_message_operator, this, true)
        binding = ItemViewMessageOperatorBinding.bind(view)
    }

    fun setOperator(operator: IMMessageOperator, sender: IMMsgSender, message: Message, clickListener: OnClickListener) {
        binding.ivMsgOpr.setImageResource(operator.resId())
        binding.tvMsgOpr.text = operator.title()
        binding.root.setOnClickListener {
            clickListener.onClick(binding.root)
            operator.onClick(sender, message)
        }
    }

}