package com.thk.im.android.ui.provider.internal

import android.view.View
import androidx.lifecycle.LifecycleOwner
import com.google.gson.Gson
import com.thk.im.android.core.IMCoreManager
import com.thk.im.android.core.processor.body.VideoBody
import com.thk.im.android.db.MsgType
import com.thk.im.android.ui.provider.MsgItemViewProvider
import com.thk.im.android.ui.viewholder.msg.BaseMsgVH
import com.thk.im.android.ui.viewholder.msg.VideoMsgVH

class VideoMsgIVProvider : MsgItemViewProvider() {

    override fun assembleMsgContent(sid: Long, params: String): String? {
        val storageModule = IMCoreManager.getStorageModule()
        val pathPair = storageModule.getPathsFromFullPath(params) ?: return null
        val videoParams = com.thk.im.android.base.MediaUtils.getVideoParams(params) ?: return null
        val thumbnailPath = storageModule.allocLocalFilePath(
            sid,
            pathPair.second + ".cover",
            "video"
        )
        try {
            storageModule.saveImageInto(thumbnailPath, videoParams.first)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        val aspect = com.thk.im.android.base.MediaUtils.getBitmapAspect(thumbnailPath)
        val width = aspect.first
        val height = aspect.second
        val ratio = 100 * width / height
        val body = VideoBody(
            null, params, thumbnailPath, videoParams.second,
            ratio, width, height
        )
        return Gson().toJson(body)
    }

    override fun messageType(): Int {
        return MsgType.VIDEO.value
    }

    override fun createViewHolder(
        lifecycleOwner: LifecycleOwner,
        itemView: View,
        viewType: Int
    ): BaseMsgVH {
        return VideoMsgVH(lifecycleOwner, itemView, viewType)
    }
}