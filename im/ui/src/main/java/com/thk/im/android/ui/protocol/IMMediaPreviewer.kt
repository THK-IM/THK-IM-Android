package com.thk.im.android.ui.protocol

import android.app.Activity
import android.view.View
import com.thk.im.android.ui.manager.MediaItem

interface IMMediaPreviewer {

    /// 预览消息
    fun previewMessage(activity: Activity, items: ArrayList<MediaItem>, view: View, position: Int)
}