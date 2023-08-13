package com.thk.im.android.db.entity

enum class MsgStatus(val value: Int) {
    Init(0),                // 初始
    Uploading(1),           // 附件上传中
    Sending(2),             // 发送中
    SendFailed(3),          // 发送失败
    SorRSuccess(4),         // 发送或接收成功
    AlreadyRead(5),         // 已读
    Deleted(9),             // 删除
}