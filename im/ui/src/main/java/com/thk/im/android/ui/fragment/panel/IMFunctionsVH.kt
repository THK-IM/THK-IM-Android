package com.thk.im.android.ui.fragment.panel

import android.view.View
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import com.thk.im.android.ui.R
import com.thk.im.android.ui.manager.IMUIManager

class IMFunctionsVH(itemView: View) : RecyclerView.ViewHolder(itemView) {

    private val iconView = itemView.findViewById<AppCompatImageView>(R.id.iv_function)
    private val titleView = itemView.findViewById<AppCompatTextView>(R.id.tv_function)

    private var position = 0

    fun setPosition(position: Int) {
        this.position = position
        val provider = IMUIManager.functionIVProviders[position]
        provider?.let {
            this.position = position
            iconView.setImageResource(it.iconResId())
            titleView.text = it.title()
        }

    }
}