package com.thk.im.android.ui.panel.component.internal

import androidx.annotation.DrawableRes
import com.google.gson.Gson
import com.luck.picture.lib.entity.LocalMedia
import com.thk.im.android.base.LLog
import com.thk.im.android.core.IMCoreManager
import com.thk.im.android.core.IMImageMsgData
import com.thk.im.android.db.MsgType

abstract class BaseMediaComponent(name: String, @DrawableRes id: Int) :
    BaseUIComponent(name, id) {
    fun onMediaResult(result: ArrayList<LocalMedia>) {
        try {
            for (media in result) {
                LLog.v(
                    "onMediaResult", "onResult: " + Thread.currentThread().name +
                            ", " + media.mimeType + ", " + media.isOriginal + ", "
                            + media.realPath + ", " + media.compressPath + ", " + media.isCompressed + ", " + media.size
                )
                if (media.mimeType.startsWith("video", true)) {
                    // TODO
//                    val msgIVProvider =
//                        IMItemViewManager.getMsgIVProviderByMsgType(MsgType.VIDEO.value)
//                    val content =
//                        msgIVProvider.assembleMsgContent(componentManager.mSid, media.realPath)
//                    content?.let {
//                        val processor =
//                            IMCoreManager.getMessageModule()
//                                .getMsgProcessor(MsgType.VIDEO.value)
//                        processor.sendMessage(it, componentManager.mSid)
//                    }
                } else {
                    val processor =
                        IMCoreManager.getMessageModule().getMsgProcessor(MsgType.IMAGE.value)
                    var path = media.compressPath
                    if (path == null || media.isOriginal) {
                        path = media.realPath
                    }
                    val imageMsgData = IMImageMsgData()
                    imageMsgData.path = path
                    processor.sendMessage(imageMsgData, componentManager.mSid)
                }

            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}