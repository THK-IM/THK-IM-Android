package com.thk.im.android.ui.panel.component.internal

import android.view.View
import androidx.recyclerview.widget.RecyclerView

abstract class BaseComponentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
    View.OnClickListener {

    private var component: BaseUIComponent? = null


    open fun setData(component: BaseUIComponent) {
        this.component = component
        component.attachView = itemView
        itemView.setOnClickListener(this)
    }

    override fun onClick(p0: View?) {
        component?.onComponentClick(p0)
    }
}