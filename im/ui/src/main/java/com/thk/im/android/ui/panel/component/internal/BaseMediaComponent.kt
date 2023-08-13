package com.thk.im.android.ui.panel.component.internal

import androidx.annotation.DrawableRes
import com.google.gson.Gson
import com.luck.picture.lib.entity.LocalMedia
import com.thk.im.android.core.IMManager
import com.thk.im.android.core.processor.body.ImageBody
import com.thk.im.android.core.utils.LLog
import com.thk.im.android.db.entity.MsgType
import com.thk.im.android.ui.manager.IMItemViewManager

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
                    val msgIVProvider =
                        IMItemViewManager.getMsgIVProviderByMsgType(MsgType.VIDEO.value)
                    val content =
                        msgIVProvider.assembleMsgContent(componentManager.mSid, media.realPath)
                    content?.let {
                        val processor =
                            IMManager.getMessageModule()
                                .getMessageProcessor(MsgType.VIDEO.value)
                        processor.sendMessage(it, componentManager.mSid)
                    }
                } else {
                    val processor =
                        IMManager.getMessageModule().getMessageProcessor(MsgType.IMAGE.value)
                    var path = media.compressPath
                    if (path == null || media.isOriginal) {
                        path = media.realPath
                    }
                    val body =
                        ImageBody(null, path, media.width, media.height)
                    processor.sendMessage(Gson().toJson(body), componentManager.mSid)
                }

            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}