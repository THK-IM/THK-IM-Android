package com.thk.im.android.ui.fragment.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.thk.im.android.ui.R
import com.thk.im.android.ui.fragment.viewholder.PanelMenuVH
import com.thk.im.android.ui.manager.IMUIManager

class PanelMenuAdapter : RecyclerView.Adapter<PanelMenuVH>() {

    private var selectedIndex = 0
    var menuSelectListener: MenuSelectListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PanelMenuVH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.itemview_panel_menu, parent, false)
        return PanelMenuVH(view)
    }

    override fun getItemCount(): Int {
        return IMUIManager.panelFragmentProviders.size
    }

    override fun onBindViewHolder(holder: PanelMenuVH, position: Int) {
        holder.setPanelProvider(
            selectedIndex == position,
            IMUIManager.panelFragmentProviders[position]
        )
        holder.itemView.setOnClickListener {
            menuSelectListener?.let { listener ->
                val intercept = IMUIManager.panelFragmentProviders[position]?.menuClicked()
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