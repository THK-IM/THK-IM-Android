package com.thk.im.android.media.preview.viewholder

import android.view.View
import androidx.lifecycle.LifecycleOwner
import com.thk.im.android.ui.fragment.viewholder.BaseVH
import com.thk.im.android.ui.manager.MediaItem

open class MediaVH(liftOwner: LifecycleOwner, itemView: View) :
    BaseVH(liftOwner, itemView) {

    open fun bindMedia(mediaItem: MediaItem) {
    }

}