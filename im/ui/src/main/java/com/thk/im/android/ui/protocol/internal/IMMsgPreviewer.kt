package com.thk.im.android.ui.protocol.internal

import android.view.View
import com.thk.im.android.db.entity.Message

interface IMMsgPreviewer {

    ///  预览消息
    fun previewMessage(msg: Message, position: Int, originView: View)

    /// 打开多选消息视图
    fun setSelectMode(selected: Boolean, firstSelectId: Long?)
}