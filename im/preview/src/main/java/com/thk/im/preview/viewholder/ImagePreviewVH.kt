package com.thk.im.preview.viewholder

import android.view.View
import androidx.lifecycle.LifecycleOwner
import com.google.gson.Gson
import com.thk.im.android.base.IMImageLoader
import com.thk.im.android.core.IMCoreManager
import com.thk.im.android.core.IMMsgResourceType
import com.thk.im.android.db.entity.Message
import com.thk.im.android.preview.R
import com.thk.im.android.ui.manager.IMImageMsgData
import com.thk.im.preview.view.ZoomableImageView

class ImagePreviewVH(liftOwner: LifecycleOwner, itemView: View) :
    PreviewVH(liftOwner, itemView) {

    private val iVMedia = itemView.findViewById<ZoomableImageView>(R.id.iv_media)

    override fun bindMessage(message: Message) {
        super.bindMessage(message)
        startPreview()
    }

    override fun startPreview() {
        super.startPreview()
        message?.let {
            if (it.data != null) {
                val data = Gson().fromJson(it.data, IMImageMsgData::class.java)
                if (data.path != null) {
                    IMImageLoader.displayImageByPath(iVMedia, data.path!!)
                } else {
                    if (data.thumbnailPath != null) {
                        IMImageLoader.displayImageByPath(iVMedia, data.thumbnailPath!!)
                    }
                    downloadOriginImage()
                }
            }
        }
    }

    private fun downloadOriginImage() {
        message?.let {
            val processor = IMCoreManager.getMessageModule().getMsgProcessor(it.type)
            processor.downloadMsgContent(it, IMMsgResourceType.Source.value)
        }
    }

    override fun stopPreview() {
        super.stopPreview()
        iVMedia.reset()
    }

    override fun onViewRecycled() {
        super.onViewRecycled()
        stopPreview()
    }
}