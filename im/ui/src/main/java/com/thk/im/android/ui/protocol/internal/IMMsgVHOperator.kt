package com.thk.im.android.ui.protocol.internal

import android.view.View
import com.thk.im.android.core.db.entity.Message
import com.thk.im.android.core.db.entity.SessionMember
import com.thk.im.android.core.db.entity.User
import io.reactivex.Flowable

interface IMMsgVHOperator {

    /// 点击消息回复内容
    fun onMsgReferContentClick(message: Message, view: View)

    /// 点击消息内容
    fun onMsgCellClick(message: Message, position: Int, view: View)

    /// 点击消息发送人
    fun onMsgCellLongClick(message: Message, position: Int, view: View)

    /// 长按消息
    fun onMsgSenderLongClick(message: Message, pos: Int, it: View)

    /// 长按消息发送人
    fun onMsgSenderClick(message: Message, position: Int, view: View)

    /// 点击消息已读状态
    fun onMsgReadStatusClick(message: Message)

    /// 点击消息重发
    fun onMsgResendClick(message: Message)

    /// 是否为选择模式
    fun isSelectMode(): Boolean

    /// 选中
    fun onSelected(message: Message, selected: Boolean)

    /// 是否为被选中
    fun isItemSelected(message: Message): Boolean

    /// 获取发送者
    fun msgSender(): IMMsgSender?

}