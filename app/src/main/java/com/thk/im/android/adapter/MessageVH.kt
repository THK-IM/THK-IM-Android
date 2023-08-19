package com.thk.im.android.adapter

import android.view.View
import android.view.View.OnAttachStateChangeListener
import android.widget.TextView
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.thk.im.android.R

class MessageVH(private val lifecycleOwner: LifecycleOwner, itemView: View) :
    ViewHolder(itemView),
    DefaultLifecycleObserver {

    private val contentView: TextView

    init {
        lifecycleOwner.lifecycle.addObserver(this)
        itemView.addOnAttachStateChangeListener(object : OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(p0: View) {
                onResume(lifecycleOwner)
            }

            override fun onViewDetachedFromWindow(p0: View) {
                onDestroy(lifecycleOwner)
            }

        })
        contentView = itemView.findViewById(R.id.tv_content)
    }


    fun bind(msg: String) {
        contentView.text = msg
    }

    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        lifecycleOwner.lifecycle.removeObserver(this)
    }

}