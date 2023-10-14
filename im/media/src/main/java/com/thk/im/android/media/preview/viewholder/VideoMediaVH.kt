package com.thk.im.android.media.preview.viewholder

import android.view.View
import androidx.lifecycle.LifecycleOwner
import com.thk.im.android.media.R
import com.thk.im.android.media.preview.view.VideoPlayerView
import com.thk.im.android.ui.manager.VideoMediaItem


class VideoMediaVH(liftOwner: LifecycleOwner, itemView: View) :
    MediaVH(liftOwner, itemView) {
    private val pvVideo = itemView.findViewById<VideoPlayerView>(R.id.pv_video)

    override fun startPreview() {
        super.startPreview()
        val videoMediaItem = mediaItem as VideoMediaItem
        if (videoMediaItem.sourcePath != null) {
            pvVideo.startPlay(videoMediaItem.sourcePath!!)
        } else if (videoMediaItem.sourceUrl != null) {
            pvVideo.startPlay(videoMediaItem.sourceUrl!!)
        } else {
            pvVideo.releasePlay()
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