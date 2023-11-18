package com.thk.im.android.ui.protocol.internal

import android.view.View
import com.thk.im.android.core.db.entity.Message

interface IMMsgPreviewer {

    ///  预览消息
    fun previewMessage(msg: Message, position: Int, originView: View)

}