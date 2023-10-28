package com.thk.im.preview.viewholder

import android.view.View
import androidx.lifecycle.LifecycleOwner
import com.google.gson.Gson
import com.thk.im.android.base.IMImageLoader
import com.thk.im.android.db.entity.Message
import com.thk.im.android.preview.R
import com.thk.im.android.ui.manager.IMImageMsgBody
import com.thk.im.android.ui.manager.IMImageMsgData
import com.thk.im.preview.view.ZoomableImageView

class ImagePreviewVH(liftOwner: LifecycleOwner, itemView: View) :
    PreviewVH(liftOwner, itemView) {

    private val iVMedia = itemView.findViewById<ZoomableImageView>(R.id.iv_media)

    override fun bindMessage(message: Message) {
        super.bindMessage(message)
        if (message.data != null) {
            val data = Gson().fromJson(message.data, IMImageMsgData::class.java)
            if (data.path != null) {
                IMImageLoader.displayImageByPath(iVMedia, data.path!!)
            } else {
                if (data.thumbnailPath != null) {
                    IMImageLoader.displayImageByPath(iVMedia, data.thumbnailPath!!)
                } else {
                    startDownload()
                }
            }
        }
    }

    private fun startDownload() {
        message?.let {
            if (it.content != null) {
                val content = Gson().fromJson(it.content, IMImageMsgBody::class.java)
                if (content != null) {
                    // TODO
                }
            }
        }
    }

    override fun startPreview() {
        super.startPreview()
    }

    override fun stopPreview() {
        super.stopPreview()
        iVMedia.reset()
    }
}