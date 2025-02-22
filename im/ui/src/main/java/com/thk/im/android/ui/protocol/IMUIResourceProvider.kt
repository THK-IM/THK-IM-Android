package com.thk.im.android.ui.protocol

import android.graphics.drawable.Drawable
import com.thk.im.android.core.db.entity.Message
import com.thk.im.android.core.db.entity.Session
import com.thk.im.android.core.db.entity.User
import com.thk.im.android.ui.manager.IMMsgPosType

interface IMUIResourceProvider {

    /// 头像
    fun avatar(user: User): Int?

    /// 表情字符串数组
    fun unicodeEmojis(): List<String>?

    /// 消息包裹容器布局id
    fun msgContainer(posType: IMMsgPosType): Int?

    /// 消息选中图片
    fun messageSelectImageResource(): Int?

    /// 消息气泡图
    fun msgBubble(message: Message, session: Session?): Drawable?

    /// 主题色
    fun tintColor(): Int?

    /// 底部输入，表情/更多/弹出面板背景颜色
    fun panelBgColor(): Int?

    /// 输入区域背景颜色
    fun inputBgColor(): Int?

    /// 页面背景颜色+文本输入位置背景颜色
    fun layoutBgColor(): Int?

    /// 输入文字颜色
    fun inputTextColor(): Int?

    /// 界面提示文字颜色
    fun tipTextColor(): Int?

    /// 是否支持某个功能
    fun supportFunction(session: Session, functionFlag: Long): Boolean

    /// 是否可以At所有人
    fun canAtAll(session: Session): Boolean
}