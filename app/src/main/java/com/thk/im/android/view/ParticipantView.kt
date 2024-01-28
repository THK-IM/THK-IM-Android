package com.thk.im.android.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import androidx.constraintlayout.widget.ConstraintLayout
import com.thk.android.im.live.room.BaseParticipant
import com.thk.im.android.R
import com.thk.im.android.databinding.ViewParticipantBinding


class ParticipantView : ConstraintLayout {
    private var participant: BaseParticipant? = null
    private val binding: ViewParticipantBinding
    private var touchByMove = false
    private var lastPositionX = 0f
    private var lastPositionY = 0f

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int)
            : super(context, attrs, defStyleAttr)

    init {
        val view = LayoutInflater.from(context).inflate(R.layout.view_participant, this, true)
        binding = ViewParticipantBinding.bind(view)
        binding.btnVideoMuted.setOnClickListener {
            participant?.let {
                val muted = it.getVideoMuted()
                it.setVideoMuted(!muted)
            }
            setSelected()
        }
        binding.btnAudioMuted.setOnClickListener {
            participant?.let {
                val muted = it.getAudioMuted()
                it.setAudioMuted(!muted)
            }
            setSelected()
        }
    }


    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (moveByTouch() && event != null) {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    lastPositionX = event.x
                    lastPositionY = event.y
                }

                MotionEvent.ACTION_MOVE -> {
                    translationX = x + (event.x - lastPositionX)
                    translationY = y + (event.y - lastPositionY)
                }

                MotionEvent.ACTION_UP -> {}
                MotionEvent.ACTION_CANCEL -> {}
            }
        }
        return true
    }

    fun setMoveByTouch(b: Boolean) {
        touchByMove = b
    }

    private fun moveByTouch(): Boolean {
        return touchByMove
    }

    private fun setSelected() {
        participant?.let {
            binding.btnVideoMuted.isSelected = !it.getVideoMuted()
            binding.btnAudioMuted.isSelected = !it.getAudioMuted()
        }
    }

    fun setParticipant(p: BaseParticipant) {
        if (participant != null) {
            participant!!.detachViewRender()
        }
        participant = p
        participant!!.attachViewRender(binding.rtcRendererVideo)
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