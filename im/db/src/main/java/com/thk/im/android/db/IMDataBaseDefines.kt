package com.thk.im.android.db

/**
 * 会话类型
 */
enum class SessionType(val value: Int) {
    Single(1),
    Group(2),
    SuperGroup(3)
}

/**
 * session 禁言
 */
enum class SessionMuted(val value: Int) {
    Normal(0),
    All(1),
    MySelf(2),
}

/**
 * session  消息提示
 */
enum class SessionPrompt(val value: Int) {
    Normal(0),
    Reject(1),
    Silent(2),
}

/**
 * 消息发送状态
 */
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
enum class MsgOperateStatus(var value: Int) {
    Init(0),
    Ack(1),        // 用户已接收
    ClientRead(2), // 用户已读
    ServerRead(4), // 用户已告知服务端已读
    Revoke(8),     // 用户撤回
    Update(16)     // 用户更新消息（重新编辑等操作）
}

/**
 * 消息类型
 */
enum class MsgType(var value: Int) {
    UnSupport(0),  // 未知
    TEXT(1),     // 文本
    EMOJI(2),    // 表情图片
    Audio(3),    // 语音
    IMAGE(4),    // 图片
    RICH(5),     // 富文本
    VIDEO(6),    // 视频
    FILE(7),     // 文件
    LOCATION(8), // 定位
    CALL(9),     // 通话
}

/**
 * 性别
 */
enum class SexType(var value: Int) {
    Unknown(0),
    Man(1),
    Women(2),
    Machine(3),
    Ai(4)
}
