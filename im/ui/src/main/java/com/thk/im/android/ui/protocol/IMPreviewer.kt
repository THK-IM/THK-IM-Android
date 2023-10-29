package com.thk.im.android.ui.protocol

import android.app.Activity
import android.view.View
import com.thk.im.android.db.entity.Message

interface IMPreviewer {

    /// 预览消息
    fun previewMediaMessage(activity: Activity, items: ArrayList<Message>, view: View, defaultId: Long)
}