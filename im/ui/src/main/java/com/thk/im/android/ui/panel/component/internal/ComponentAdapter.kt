package com.thk.im.android.ui.panel.component.internal

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

/**
 * 组件适配器
 */
class ComponentAdapter(private val uiComponentManager: UIComponentManager) :
    RecyclerView.Adapter<BaseComponentViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseComponentViewHolder {
        return uiComponentManager.provideViewHolder(parent)
    }

    override fun getItemCount(): Int {
        return uiComponentManager.size
    }

    override fun onBindViewHolder(holder: BaseComponentViewHolder, position: Int) {
        uiComponentManager.getComponent(position)?.let {
            holder.setData(it)
        }
    }
}