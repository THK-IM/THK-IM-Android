package com.thk.im.android.ui.fragment.popup

import android.content.Context
import com.google.android.flexbox.FlexboxLayout
import com.lxj.xpopup.core.AttachPopupView
import com.thk.im.android.db.entity.Message
import com.thk.im.android.ui.R
import com.thk.im.android.ui.protocol.IMMessageOperator
import com.thk.im.android.ui.protocol.internal.IMMsgSender

class MessageOperatorPopup(context: Context) : AttachPopupView(context) {

    lateinit var message: Message
    lateinit var sender: IMMsgSender
    lateinit var operators: List<IMMessageOperator>

    override fun onCreate() {
        super.onCreate()
        val operatorLayout = findViewById<FlexboxLayout>(R.id.layout_operators)
        for (operator in operators) {
            val operatorView = MessageOperatorItemView(context)
            operatorLayout.addView(operatorView)
            operatorView.setOperator(operator, sender, message) {
                dismiss()
            }
        }
    }

    override fun getImplLayoutId(): Int {
        return R.layout.popup_message_operator
    }
}