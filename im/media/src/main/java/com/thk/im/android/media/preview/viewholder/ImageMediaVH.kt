package com.thk.im.android.media.preview.viewholder

import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.lifecycle.LifecycleOwner
import com.thk.im.android.base.IMImageLoader
import com.thk.im.android.media.R
import com.thk.im.android.ui.manager.ImageMediaItem
import com.thk.im.android.ui.manager.MediaItem

class ImageMediaVH(liftOwner: LifecycleOwner, itemView: View) :
    MediaVH(liftOwner, itemView) {

    private val testView = itemView.findViewById<TextView>(R.id.tv_page)
    private val iVMedia = itemView.findViewById<AppCompatImageView>(R.id.iv_media)


    override fun bindMedia(mediaItem: MediaItem) {
        super.bindMedia(mediaItem)
        val imageItem = mediaItem as ImageMediaItem
        testView.text = "${imageItem.width}"

        if (imageItem.thumbnailPath != null) {
            IMImageLoader.displayImageByPath(iVMedia, imageItem.thumbnailPath!!)
        }
    }
}