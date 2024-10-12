package com.thk.im.preview.viewholder

import android.view.View
import androidx.lifecycle.LifecycleOwner
import com.google.gson.Gson
import com.thk.im.android.core.IMCoreManager
import com.thk.im.android.core.IMLoadProgress
import com.thk.im.android.core.IMMsgResourceType
import com.thk.im.android.core.base.IMImageLoader
import com.thk.im.android.core.db.entity.Message
import com.thk.im.android.core.event.XEventBus
import com.thk.im.android.core.fileloader.FileLoadState
import com.thk.im.android.preview.R
import com.thk.im.android.ui.manager.IMImageMsgBody
import com.thk.im.android.ui.manager.IMImageMsgData
import com.thk.im.preview.ExitPreviewEvent
import com.thk.im.preview.player.THKVideoPlayerView
import com.thk.im.preview.view.CircleProgressBar
import com.thk.im.preview.view.ZoomableImageView

class ImagePreviewVH(liftOwner: LifecycleOwner, itemView: View) :
    PreviewVH(liftOwner, itemView) {

    private val ivImage = itemView.findViewById<ZoomableImageView>(R.id.iv_image)
    private val progressView = itemView.findViewById<CircleProgressBar>(R.id.progress_dl)

    init {
        ivImage.setOnClickListener {
            XEventBus.post(ExitPreviewEvent, "")
        }
    }

    override fun onIMLoadProgress(progress: IMLoadProgress) {
        message?.let {
            val body = Gson().fromJson(it.content, IMImageMsgBody::class.java)
            if (body.url == progress.url) {
                if (progress.state == FileLoadState.Success.value) {
                    progressView.visibility = View.GONE
                    IMImageLoader.displayImageByPath(ivImage, progress.path)
                    val data = Gson().fromJson(it.data, IMImageMsgData::class.java) ?: IMImageMsgData()
                    data.height = body.height
                    data.width = body.width
                    data.path = progress.path
                    it.data = Gson().toJson(data)
                } else if (progress.state == FileLoadState.Ing.value
                    || (progress.state == FileLoadState.Init.value)
                ) {
                    progressView.setProgress(progress.progress)
                    progressView.visibility = View.VISIBLE
                } else {
                    progressView.visibility = View.GONE
                }

            }
        }
    }

    override fun bindMessage(message: Message) {
        super.bindMessage(message)
        this.message?.let {
            if (it.data != null) {
                val data = Gson().fromJson(it.data, IMImageMsgData::class.java)
                if (data.path != null) {
                    IMImageLoader.displayImageByPath(ivImage, data.path!!)
                } else {
                    if (data.thumbnailPath != null) {
                        IMImageLoader.displayImageByPath(ivImage, data.thumbnailPath!!)
                    }
                    downloadOriginImage()
                }
            } else {
                downloadOriginImage()
            }
        }
    }

    private fun downloadOriginImage() {
        message?.let {
            val processor = IMCoreManager.messageModule.getMsgProcessor(it.type)
            processor.downloadMsgContent(it, IMMsgResourceType.Source.value)
        }
    }

    override fun startPreview(playerView: THKVideoPlayerView) {
        playerView.stopPlay()
    }
}