package com.thk.im.android.adapter

import android.view.View
import android.view.View.OnAttachStateChangeListener
import android.widget.TextView
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.thk.android.im.live.participant.BaseParticipant
import com.thk.im.android.R
import org.webrtc.SurfaceViewRenderer

class ParticipantVH(private val lifecycleOwner: LifecycleOwner, itemView: View) :
    ViewHolder(itemView),
    DefaultLifecycleObserver {

    private val uIdTextView: TextView
    private val videoViewRenderer: SurfaceViewRenderer

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
        uIdTextView = itemView.findViewById(R.id.tv_uid)
        videoViewRenderer = itemView.findViewById(R.id.rtc_renderer_video)
    }

    private var participant: BaseParticipant? = null

    fun bind(participant: BaseParticipant) {
        this.participant = participant
        uIdTextView.text = this.participant!!.uid
        this.participant?.attachViewRender(videoViewRenderer)
        this.participant?.initPeerConn()
    }

    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)
//        this.participant?.attachViewRender(videoViewRenderer)
    }

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
//        this.participant?.detachViewRender()
    }

    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
//        this.participant?.detachViewRender()
        lifecycleOwner.lifecycle.removeObserver(this)
    }

}