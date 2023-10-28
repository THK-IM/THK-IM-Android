package com.thk.im.android.ui.protocol

import android.app.Activity
import android.view.View
import com.thk.im.android.db.entity.Message
import com.thk.im.android.ui.manager.MediaItem

interface IMPreviewer {

    /// 预览消息
    fun previewMessage(activity: Activity, items: ArrayList<Message>, view: View, position: Int)
}