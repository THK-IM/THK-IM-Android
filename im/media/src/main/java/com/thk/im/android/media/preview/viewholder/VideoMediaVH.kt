package com.thk.im.android.media.preview.viewholder

import android.view.View
import androidx.appcompat.widget.AppCompatImageView
import androidx.lifecycle.LifecycleOwner
import com.thk.im.android.base.IMImageLoader
import com.thk.im.android.media.R
import com.thk.im.android.media.preview.view.VideoPlayerView
import com.thk.im.android.ui.manager.ImageMediaItem
import com.thk.im.android.ui.manager.MediaItem
import com.thk.im.android.ui.manager.VideoMediaItem


class VideoMediaVH(liftOwner: LifecycleOwner, itemView: View) :
    MediaVH(liftOwner, itemView) {
    private val pvVideo = itemView.findViewById<VideoPlayerView>(R.id.pv_video)


    override fun bindMedia(mediaItem: MediaItem) {
        super.bindMedia(mediaItem)
        val videoMediaItem = mediaItem as VideoMediaItem
        if (videoMediaItem.sourcePath != null) {
            pvVideo.startPlay(videoMediaItem.sourcePath!!)
        } else if (videoMediaItem.sourceUrl != null) {
            pvVideo.startPlay(videoMediaItem.sourceUrl!!)
        } else {
            pvVideo.stopPlay()
        }
    }
}