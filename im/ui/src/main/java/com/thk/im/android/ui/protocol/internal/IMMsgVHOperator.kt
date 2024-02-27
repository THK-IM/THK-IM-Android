package com.thk.im.android.ui.protocol.internal

import android.view.View
import com.thk.im.android.core.db.entity.Message

interface IMMsgVHOperator {
    fun onMsgReferContentClick(message: Message, view: View)
    fun onMsgCellClick(message: Message, position: Int, view: View)
    fun onMsgCellLongClick(message: Message, position: Int, view: View)
    fun onMsgSenderClick(message: Message, position: Int, view: View)
    fun onMsgSenderLongClick(message: Message, pos: Int, it: View)
    fun onMsgResendClick(message: Message)
    fun isSelectMode(): Boolean
    fun isItemSelected(message: Message): Boolean
    fun onSelected(message: Message, selected: Boolean)
    fun readMessage(message: Message)
    fun setEditText(text: String)
}