package com.thk.im.android.ui.protocol.internal

import android.content.Context
import android.view.View
import androidx.emoji2.widget.EmojiEditText
import com.thk.im.android.core.db.entity.Message
import com.thk.im.android.core.db.entity.Session
import com.thk.im.android.core.db.entity.SessionMember
import com.thk.im.android.core.db.entity.User
import io.reactivex.Flowable

interface IMMsgSender {


    /// 提示有新消息
    fun showNewMsgTipsView(isHidden: Boolean)

    /// 获取Context
    fun context(): Context

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

    /// 显示toast
    fun showToast(text: String)

    /// 显示错误
    fun showError(throwable: Throwable)

    /// show message
    fun showMessage(text: String, success: Boolean)

    /// 发送消息到session forwardType 0单条转发, 1合并转发
    fun forwardMessageToSession(messages: List<Message>, forwardType: Int)

    /// 转发选定的消息 forwardType 0单条转发, 1合并转发
    fun forwardSelectedMessages(forwardType: Int)

    ///  打开at会话成员控制器
    fun openAtPopupView()

    /// At用户
    fun addAtUser(user: User, sessionMember: SessionMember?)

    /// 回复消息
    fun replyMessage(msg: Message)

    /// 关闭回复消息
    fun closeReplyMessage()

    /// 重编辑消息
    fun reeditMessage(message: Message)

    /// 同步获取用户信息
    fun syncGetSessionMemberInfo(userId: Long): Pair<User, SessionMember?>?

    /// 同步获取用户id
    fun syncGetSessionMemberUserIdByNickname(nick: String): Long?

    /// 设置用户信息
    fun saveSessionMemberInfo(info: Pair<User, SessionMember?>)

    /// 异步获取用户信息
    fun asyncGetSessionMemberInfo(userId: Long): Flowable<Pair<User, SessionMember?>>

}