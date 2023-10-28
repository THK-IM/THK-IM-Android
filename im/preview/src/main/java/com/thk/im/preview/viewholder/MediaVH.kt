package com.thk.im.preview.viewholder

import android.view.View
import androidx.lifecycle.LifecycleOwner
import com.thk.im.android.ui.fragment.viewholder.BaseVH
import com.thk.im.android.ui.manager.MediaItem

open class MediaVH(liftOwner: LifecycleOwner, itemView: View) :
    BaseVH(liftOwner, itemView) {

    protected var mediaItem: MediaItem? = null
    open fun bindMedia(mediaItem: MediaItem) {
        this.mediaItem = mediaItem
    }

    open fun startPreview() {

    }

    open fun stopPreview() {

    }

    open fun hide() {
    }

    open fun show() {

    }

}