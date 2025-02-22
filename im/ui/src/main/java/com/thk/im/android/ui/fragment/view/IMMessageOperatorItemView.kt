package com.thk.im.android.ui.fragment.view

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import com.thk.im.android.core.db.entity.Message
import com.thk.im.android.ui.R
import com.thk.im.android.ui.databinding.ItemViewMessageOperatorBinding
import com.thk.im.android.ui.manager.IMUIManager
import com.thk.im.android.ui.protocol.IMMessageOperator
import com.thk.im.android.ui.protocol.internal.IMMsgSender

class IMMessageOperatorItemView : LinearLayout {

    private var binding: ItemViewMessageOperatorBinding

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int)
            : super(context, attrs, defStyleAttr)

    init {
        val view =
            LayoutInflater.from(context).inflate(R.layout.item_view_message_operator, this, true)
        binding = ItemViewMessageOperatorBinding.bind(view)

        val inputTextColor =
            IMUIManager.uiResourceProvider?.inputTextColor() ?: Color.parseColor("#333333")
        binding.tvMsgOpr.setTextColor(inputTextColor)
    }

    fun setOperator(
        operator: IMMessageOperator,
        sender: IMMsgSender,
        message: Message,
        clickListener: OnClickListener,
    ) {
        val inputTextColor =
            IMUIManager.uiResourceProvider?.inputTextColor() ?: Color.parseColor("#333333")
        val drawable = ContextCompat.getDrawable(context, operator.resId())
        drawable?.setTint(inputTextColor)
        binding.ivMsgOpr.setImageDrawable(drawable)
        binding.tvMsgOpr.text = operator.title()
        binding.root.setOnClickListener {
            clickListener.onClick(binding.root)
            operator.onClick(sender, message)
        }
    }

}