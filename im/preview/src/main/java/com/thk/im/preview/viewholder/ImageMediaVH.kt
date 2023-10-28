package com.thk.im.preview.viewholder

import android.view.View
import androidx.lifecycle.LifecycleOwner
import com.thk.im.android.base.IMImageLoader
import com.thk.im.android.preview.R
import com.thk.im.preview.view.ZoomableImageView
import com.thk.im.android.ui.manager.ImageMediaItem
import com.thk.im.android.ui.manager.MediaItem

class ImageMediaVH(liftOwner: LifecycleOwner, itemView: View) :
    MediaVH(liftOwner, itemView) {

    private val iVMedia = itemView.findViewById<ZoomableImageView>(R.id.iv_media)

    override fun bindMedia(mediaItem: MediaItem) {
        super.bindMedia(mediaItem)
        val imageItem = mediaItem as ImageMediaItem
        if (imageItem.sourcePath != null) {
            IMImageLoader.displayImageByPath(iVMedia, imageItem.sourcePath!!)
            return
        }
        if (imageItem.thumbnailPath != null) {
            IMImageLoader.displayImageByPath(iVMedia, imageItem.thumbnailPath!!)
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