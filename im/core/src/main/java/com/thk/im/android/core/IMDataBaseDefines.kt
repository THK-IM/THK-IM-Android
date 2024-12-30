package com.thk.im.android.core

import androidx.annotation.Keep

/**
 * 会话类型
 */
@Keep
enum class SessionType(val value: Int) {
    Single(1),
    Group(2),
    SuperGroup(3),
    MsgRecord(4)
}

/**
 * session 禁言
 */
@Keep
enum class SessionMuted(val value: Int) {
    Normal(0),
    All(1),
    MySelf(2),
}

/**
 * session  消息提示
 */
@Keep
enum class SessionPrompt(val value: Int) {
    Normal(0),
    Reject(1),
    Silent(2),
}

/**
 * session 角色
 */
@Keep
enum class SessionRole(val value: Int) {
    Member(1),
    Admin(2),
    SuperAdmin(3),
    Owner(4),
}

/**
 * 消息发送状态
 */
@Keep
enum class MsgSendStatus(val value: Int) {
    Init(0),                // 初始
    Uploading(1),             // 上传中
    Sending(2),             // 发送中
    SendFailed(3),          // 发送失败
    Success(4),         // 发送或接收成功
}

/**
 * 消息操作状态
 */
@Keep
enum class MsgOperateStatus(var value: Int) {
    Init(0),
    Ack(1),        // 用户已接收
    ClientRead(2), // 用户已读
    ServerRead(4), // 用户已告知服务端已读
    Update(8)     // 用户更新消息（重新编辑等操作）
}

/**
 * 消息类型
 */
@Keep
enum class MsgType(var value: Int) {
    Reedit(-3),     // 重编辑消息
    Read(-2),       // 读消息消息
    Received(-1),   // 收消息消息
    UnSupport(0),   // 未知消息
    Text(1),        // 文本消息
    Emoji(2),       // 表情图片消息
    Audio(3),       // 语音消息
    Image(4),       // 图片消息
    RichText(5),    // 富文本消息
    Video(6),       // 视频消息
    Record(7),      // 消息记录
    Revoke(100),    // 撤回消息
    TimeLine(9999), // 时间线消息
}

/**
 * 性别
 */
@Keep
enum class SexType(var value: Int) {
    Unknown(0),
    Man(1),
    Women(2),
    Machine(3),
    Ai(4)
}
