package com.thk.im.android.ui.protocol

import androidx.emoji2.widget.EmojiEditText
import com.thk.im.android.db.entity.Message
import com.thk.im.android.db.entity.Session

interface IMMsgSender {

    fun getSession(): Session

    fun resendMessage(msg: Message)

    /// 发送消息
    fun sendMessage(type: Int, body: Any)

    /// 输入框添加内容
    fun addInputContent(text: String)

    fun getEditText(): EmojiEditText

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

    /// 显示消息多选视图
    fun showMsgMultiChooseLayout()

    /// 关闭消息多选视图
    fun dismissMsgMultiChooseLayout()

}