package com.thk.im.android.ui.protocol

import android.app.Activity
import android.view.View
import com.thk.im.android.core.db.entity.Message
import com.thk.im.android.core.db.entity.Session

interface IMPreviewer {

    /// 预览消息
    fun previewMediaMessage(activity: Activity, items: ArrayList<Message>, view: View, defaultId: Long)

    /// 预览消息记录
    fun previewRecordMessage(activity: Activity, originSession: Session, message: Message)
}