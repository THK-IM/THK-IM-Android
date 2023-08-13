package com.thk.im.android.ui.panel.component

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import com.thk.im.android.ui.R
import com.thk.im.android.ui.panel.component.internal.BaseComponentViewHolder
import com.thk.im.android.ui.panel.component.internal.BaseUIComponent

/**
 * 组件ViewHolder
 */
class ComponentViewHolder(itemView: View) : BaseComponentViewHolder(itemView) {


    companion object {
        fun create(parent: ViewGroup): ComponentViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val view = layoutInflater.inflate(R.layout.item_component, parent, false)
            return ComponentViewHolder(view)
        }
    }

    override fun setData(component: BaseUIComponent) {
        super.setData(component)
        val imageView = itemView.findViewById<AppCompatImageView>(R.id.iv_avatar)
        val title = itemView.findViewById<AppCompatTextView>(R.id.tv_title)
        title.text = component.name
        imageView.setBackgroundResource(component.icon)
    }
}