package com.thk.im.android.ui.protocol.internal

import android.view.View
import com.thk.im.android.db.entity.Message

interface IMMsgVHOperator {
    fun onMsgCellClick(message: Message, position: Int, view: View)
    fun onMsgCellLongClick(message: Message, position: Int, view: View)
    fun onMsgResendClick(message: Message)
    fun isSelectMode(): Boolean
    fun isItemSelected(message: Message): Boolean
    fun onSelected(message: Message, selected: Boolean)
    fun readMessage(message: Message)
}