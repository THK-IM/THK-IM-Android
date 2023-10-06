package com.thk.im.android.media.preview.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.thk.im.android.media.R
import com.thk.im.android.media.preview.viewholder.ImageMediaVH
import com.thk.im.android.media.preview.viewholder.MediaVH
import com.thk.im.android.media.preview.viewholder.VideoMediaVH
import com.thk.im.android.ui.manager.ImageMediaItem
import com.thk.im.android.ui.manager.MediaItem

class MediaPreviewAdapter(private val lifecycleOwner: LifecycleOwner, items: List<MediaItem>) :
    RecyclerView.Adapter<MediaVH>() {


    private val medias = mutableListOf<MediaItem>()

    init {
        medias.addAll(items)
    }

    override fun getItemViewType(position: Int): Int {
        val media = medias[position]
        return if (media is ImageMediaItem) {
            1
        } else {
            2
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MediaVH {
        return if (viewType == 1) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.itemview_media_image, parent, false)
            ImageMediaVH(lifecycleOwner, view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.itemview_media_video, parent, false)
            VideoMediaVH(lifecycleOwner, view)
        }
    }

    override fun getItemCount(): Int {
        return medias.size
    }

    override fun onBindViewHolder(holder: MediaVH, position: Int) {
        holder.bindMedia(medias[position])
    }

    override fun onViewRecycled(holder: MediaVH) {
        super.onViewRecycled(holder)
        holder.onViewDetached()
    }

    override fun onViewAttachedToWindow(holder: MediaVH) {
        super.onViewAttachedToWindow(holder)
        holder.onViewAttached()
    }

    override fun onViewDetachedFromWindow(holder: MediaVH) {
        super.onViewDetachedFromWindow(holder)
        holder.onViewDetached()
    }

    override fun onFailedToRecycleView(holder: MediaVH): Boolean {
        holder.onViewDetached()
        return super.onFailedToRecycleView(holder)
    }
}