package com.thk.im.preview.viewholder

import android.view.View
import android.widget.RelativeLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.lifecycle.LifecycleOwner
import com.google.gson.Gson
import com.thk.im.android.base.IMImageLoader
import com.thk.im.android.base.LLog
import com.thk.im.android.db.entity.Message
import com.thk.im.android.preview.R
import com.thk.im.android.ui.manager.IMVideoMsgBody
import com.thk.im.android.ui.manager.IMVideoMsgData
import com.thk.im.preview.view.VideoPlayerView


class VideoPreviewVH(liftOwner: LifecycleOwner, itemView: View) :
    PreviewVH(liftOwner, itemView) {
    private val pvVideo = itemView.findViewById<VideoPlayerView>(R.id.pv_video)
    private val lyCover = itemView.findViewById<RelativeLayout>(R.id.ly_cover)
    private val ivCover = itemView.findViewById<AppCompatImageView>(R.id.iv_cover)
    private val ivPlay = itemView.findViewById<AppCompatImageView>(R.id.iv_play)

    override fun bindMessage(message: Message) {
        super.bindMessage(message)
        if (message.data != null) {
            val data = Gson().fromJson(message.data, IMVideoMsgData::class.java)
            if (data?.thumbnailPath != null) {
                IMImageLoader.displayImageByPath(ivCover, data.thumbnailPath!!)
            } else {
                downloadCover()
            }
        }
        pvVideo.visibility = View.GONE
        lyCover.visibility = View.VISIBLE
        ivPlay.setOnClickListener {
            startPreview()
            lyCover.visibility = View.GONE
        }
    }

    private fun downloadCover() {
        message?.let {
            if (it.content != null) {
                try {
                    val content = Gson().fromJson(it.content, IMVideoMsgBody::class.java)
                    if (content != null) {
                        // TODO
                    }
                } catch (e: Exception) {
                    LLog.e("${it.content} ${e.toString()}")
                }
            }
        }
    }

    override fun startPreview() {
        super.startPreview()
        pvVideo.visibility = View.VISIBLE
        message?.let {
            var played = false
            if (it.data != null) {
                val data = Gson().fromJson(it.data, IMVideoMsgData::class.java)
                if (data?.path != null) {
                    played = true
                    pvVideo.initPlay(data.path!!)
                    pvVideo.play()
                }
            }
            if (!played) {
                if (it.content != null) {
                    val body = Gson().fromJson(it.content, IMVideoMsgBody::class.java)
                    if (body?.url != null) {
                        pvVideo.initPlay(body.url!!)
                        pvVideo.play()
                    }
                }
            }
        }
    }

    override fun stopPreview() {
        super.stopPreview()
        pvVideo.releasePlay()
        lyCover.visibility = View.VISIBLE
        pvVideo.visibility = View.GONE
    }

    override fun hide() {
        super.hide()
        pvVideo.visibility = View.GONE
    }

    override fun show() {
        super.show()
        pvVideo.visibility = View.VISIBLE
    }

    override fun onViewRecycled() {
        super.onViewRecycled()
        pvVideo.releasePlay()
    }
}