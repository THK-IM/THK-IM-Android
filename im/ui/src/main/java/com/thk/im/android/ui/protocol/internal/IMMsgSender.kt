package com.thk.im.android.ui.protocol.internal

import android.view.View
import androidx.emoji2.widget.EmojiEditText
import com.thk.im.android.core.db.entity.Message
import com.thk.im.android.core.db.entity.Session

interface IMMsgSender {

    /// 获取session
    fun getSession(): Session

    /// 重发消息
    fun resendMessage(msg: Message)

    /// 发送消息
    fun sendMessage(
        type: Int,
        body: Any?,
        data: Any?,
        atUser: String? = null,
        referMsgId: Long? = null
    )

    /// 输入框添加内容
    fun addInputContent(text: String)

    /// 获取输入框内容
    fun getEditText(): EmojiEditText

    /// 删除输入框内容
    fun deleteContent(count: Int)

    /// 选择照片
    fun choosePhoto()

    /// 相机拍照
    fun openCamera()

    /// 移动到最新消息
    fun moveToLatestMessage()

    /// 打开底部面本:position: 1表情 2更多
    fun showBottomPanel(position: Int)

    /// 关闭底部面板
    fun closeBottomPanel(): Boolean

    /// 顶起常驻视图（消息列表+底部输入框）
    fun moveUpAlwaysShowView(isKeyboardShow: Boolean, bottomHeight: Int, duration: Long)

    /// 打开键盘
    fun openKeyboard(): Boolean

    /// 键盘是否显示
    fun isKeyboardShowing(): Boolean

    /// 关闭键盘
    fun closeKeyboard(): Boolean

    /// 打开/关闭多选消息视图
    fun setSelectMode(selected: Boolean, message: Message?)

    /// 删除多选视图选中的消息
    fun deleteSelectedMessages()

    /// 已读消息
    fun readMessage(message: Message)

    /// 弹出消息操作面板弹窗
    fun popupMessageOperatorPanel(view: View, message: Message)

    /// show loading
    fun showLoading(text: String)

    /// dismiss Loading
    fun dismissLoading()

    /// show message
    fun showMessage(text: String, success: Boolean)

    /// 发送消息到session forwardType 0单条转发, 1合并转发
    fun forwardMessageToSession(messages: List<Message>, forwardType: Int)

    /// 转发选定的消息 forwardType 0单条转发, 1合并转发
    fun forwardSelectedMessages(forwardType: Int)

}