package com.thk.im.android.ui.fragment.panel

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.thk.im.android.ui.R
import com.thk.im.android.ui.manager.IMUIManager
import com.thk.im.android.ui.protocol.internal.IMMsgSender

class IMFunctionAdapter(private val msgSender: IMMsgSender?) : RecyclerView.Adapter<IMFunctionsVH>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IMFunctionsVH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.itemview_function, parent, false)
        return IMFunctionsVH(view)
    }

    override fun getItemCount(): Int {
        return IMUIManager.functionIVProviders.size
    }

    override fun onBindViewHolder(holder: IMFunctionsVH, position: Int) {
        holder.position = position
        holder.itemView.setOnClickListener {
            msgSender?.let { sender ->
                IMUIManager.functionIVProviders[position]?.let { provider ->
                    provider.click(sender)
                }
            }

        }
    }
}