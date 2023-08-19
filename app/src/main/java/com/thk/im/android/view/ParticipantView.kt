package com.thk.im.android.view

import android.content.Context
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.TextView
import com.thk.android.im.live.room.BaseParticipant
import com.thk.im.android.R
import org.webrtc.SurfaceViewRenderer

class ParticipantView(ctx: Context) : FrameLayout(ctx) {

    private val uIdTextView: TextView
    private val videoViewRenderer: SurfaceViewRenderer
    private val muteVideoButton: ImageButton
    private val muteAudioButton: ImageButton
    private var participant: BaseParticipant? = null

    init {
        LayoutInflater.from(ctx).inflate(R.layout.view_participant, this, true)
        uIdTextView = findViewById(R.id.tv_uid)
        videoViewRenderer = findViewById(R.id.rtc_renderer_video)
        muteVideoButton = findViewById(R.id.btn_video_muted)
        muteAudioButton = findViewById(R.id.btn_audio_muted)

        muteVideoButton.setOnClickListener {
            participant?.let {
                val muted = it.getVideoMuted()
                it.setVideoMuted(!muted)
            }
            setSelected()
        }
        muteAudioButton.setOnClickListener {
            participant?.let {
                val muted = it.getAudioMuted()
                it.setAudioMuted(!muted)
            }
            setSelected()
        }
    }

    private fun setSelected() {
        participant?.let {
            muteVideoButton.isSelected = !it.getVideoMuted()
            muteAudioButton.isSelected = !it.getAudioMuted()
        }
    }

    fun setParticipant(p: BaseParticipant) {
        if (participant != null) {
            participant!!.detachViewRender()
        }
        participant = p
        participant!!.attachViewRender(this.videoViewRenderer)
        participant!!.initPeerConn()
        setSelected()
    }

    fun getParticipant(): BaseParticipant? {
        return participant
    }

    fun destroy() {
        if (participant != null) {
            participant!!.detachViewRender()
        }
        participant = null
    }
}