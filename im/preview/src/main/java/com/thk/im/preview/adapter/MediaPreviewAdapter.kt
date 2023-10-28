package com.thk.im.preview.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.thk.im.android.preview.R
import com.thk.im.android.ui.manager.ImageMediaItem
import com.thk.im.android.ui.manager.MediaItem
import com.thk.im.preview.viewholder.ImageMediaVH
import com.thk.im.preview.viewholder.MediaVH
import com.thk.im.preview.viewholder.VideoMediaVH

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
        holder.onViewRecycled()
    }

    override fun onViewAttachedToWindow(holder: MediaVH) {
        super.onViewAttachedToWindow(holder)
        holder.onViewAttached()
    }

    override fun onViewDetachedFromWindow(holder: MediaVH) {
        super.onViewDetachedFromWindow(holder)
        holder.onViewDetached()
    }

    fun onPageSelected(position: Int, recyclerView: RecyclerView) {
        for (i in 0 until medias.size) {
            val viewHolder = recyclerView.findViewHolderForLayoutPosition(i)
            viewHolder?.let {
                if (position == i) {
                    (viewHolder as MediaVH).startPreview()
                } else {
                    (viewHolder as MediaVH).stopPreview()
                }
            }
        }
    }

    fun hideChildren(currentItem: Int, recyclerView: RecyclerView) {
        for (i in 0 until medias.size) {
            val viewHolder = recyclerView.findViewHolderForLayoutPosition(i)
            viewHolder?.let {
                if (currentItem != i) {
                    (viewHolder as MediaVH).hide()
                }
            }
        }
    }

    fun showChildren(currentItem: Int, recyclerView: RecyclerView) {
        for (i in 0 until medias.size) {
            val viewHolder = recyclerView.findViewHolderForLayoutPosition(i)
            viewHolder?.let {
                if (currentItem != i) {
                    (viewHolder as MediaVH).show()
                }
            }
        }
    }
}