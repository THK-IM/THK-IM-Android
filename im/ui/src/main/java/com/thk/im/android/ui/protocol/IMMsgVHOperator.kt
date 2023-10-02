package com.thk.im.android.ui.protocol

import android.view.View
import com.thk.im.android.db.entity.Message

interface IMMsgVHOperator {
    fun onMsgCellClick(message: Message, position: Int, view: View)
    fun onMsgCellLongClick(message: Message, position: Int, view: View)
    fun onMsgResendClick(message: Message)
}