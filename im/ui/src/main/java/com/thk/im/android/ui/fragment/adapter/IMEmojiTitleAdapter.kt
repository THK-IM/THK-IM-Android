package com.thk.im.android.ui.fragment.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.thk.im.android.ui.R
import com.thk.im.android.ui.fragment.viewholder.emoji.IMEmojiTitleVH
import com.thk.im.android.ui.manager.IMUIManager

class IMEmojiTitleAdapter : RecyclerView.Adapter<IMEmojiTitleVH>() {

    private var selectedIndex = 0
    var menuSelectListener: MenuSelectListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IMEmojiTitleVH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.itemview_panel_menu, parent, false)
        return IMEmojiTitleVH(view)
    }

    override fun getItemCount(): Int {
        return IMUIManager.emojiFragmentProviders.size
    }

    override fun onBindViewHolder(holder: IMEmojiTitleVH, position: Int) {
        holder.setPanelProvider(
            selectedIndex == position,
            IMUIManager.emojiFragmentProviders[position]
        )
        holder.itemView.setOnClickListener {
            menuSelectListener?.let { listener ->
                val intercept = IMUIManager.emojiFragmentProviders[position]?.menuClicked()
                if (intercept == null || !intercept) {
                    listener.onSelected(position)
                }
            }
        }
    }

    fun setSelected(index: Int) {
        if (selectedIndex != index) {
            notifyItemChanged(selectedIndex)
            selectedIndex = index
            notifyItemChanged(selectedIndex)
        }
    }

    interface MenuSelectListener {
        fun onSelected(position: Int)
    }

}