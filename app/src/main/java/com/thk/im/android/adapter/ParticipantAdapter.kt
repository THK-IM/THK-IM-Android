package com.thk.im.android.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.thk.android.im.live.participant.BaseParticipant
import com.thk.android.im.live.utils.LLog
import com.thk.im.android.R

class ParticipantAdapter(private val lifecycleOwner: LifecycleOwner): RecyclerView.Adapter<ParticipantVH>() {

    private val participants = mutableListOf<BaseParticipant>()
    fun setData(participants: List<BaseParticipant>) {
        Log.v("WebRtc", "setData: ${participants.size}")
        val oldSize = this.participants.size
        if (oldSize > 0) {
            this.participants.clear()
            notifyItemRangeRemoved(0, oldSize)
        }
        this.participants.addAll(participants)
        notifyItemRangeInserted(0, this.participants.size)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ParticipantVH {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.itemview_participant, parent, false)
        return ParticipantVH(lifecycleOwner, itemView)
    }

    override fun getItemCount(): Int {
        Log.v("WebRtc", "getItemCount: ${participants.size}")
        return this.participants.size
    }

    override fun onBindViewHolder(holder: ParticipantVH, position: Int) {
        holder.bind(participants[position])
    }

    fun addData(p: BaseParticipant) {
        LLog.d("addData ${this.participants.contains(p)}")
        if (!this.participants.contains(p)) {
            val oldSize = this.participants.size
            this.participants.add(p)
            notifyItemInserted(oldSize)
        }
    }

    fun remove(p: BaseParticipant) {
        val position = this.participants.indexOf(p)
        if (position >= 0) {
            this.participants.removeAt(position)
            notifyItemRemoved(position)
        }
    }
}