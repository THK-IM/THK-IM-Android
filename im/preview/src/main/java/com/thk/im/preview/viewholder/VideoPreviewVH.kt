package com.thk.im.preview.viewholder

import android.view.View
import androidx.lifecycle.LifecycleOwner
import com.google.gson.Gson
import com.thk.im.android.preview.R
import com.thk.im.android.ui.manager.IMVideoMsgBody
import com.thk.im.android.ui.manager.IMVideoMsgData
import com.thk.im.preview.view.VideoPlayerView


class VideoPreviewVH(liftOwner: LifecycleOwner, itemView: View) :
    PreviewVH(liftOwner, itemView) {
    private val pvVideo = itemView.findViewById<VideoPlayerView>(R.id.pv_video)

    override fun startPreview() {
        super.startPreview()
        message?.let {
            var played = false
            if (it.data != null) {
                val data = Gson().fromJson(it.data, IMVideoMsgData::class.java)
                if (data?.path != null) {
                    played = true
                    pvVideo.startPlay(data.path!!)
                }
            }
            if (!played) {
                if (it.content != null) {
                    val body = Gson().fromJson(it.content, IMVideoMsgBody::class.java)
                    if (body?.url != null) {
                        pvVideo.startPlay(body.url!!)
                    }
                }
            }
        }
    }

    override fun stopPreview() {
        super.stopPreview()
        pvVideo.pause()
    }

    override fun hide() {
        super.hide()
        pvVideo.visibility = View.INVISIBLE
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