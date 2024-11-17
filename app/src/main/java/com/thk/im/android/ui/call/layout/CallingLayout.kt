package com.thk.im.android.ui.call.layout

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.thk.im.android.R
import com.thk.im.android.databinding.LayoutCallingBinding
import com.thk.im.android.ui.call.LiveCallProtocol

class CallingLayout : ConstraintLayout {

    private lateinit var liveCallProtocol: LiveCallProtocol

    private var binding: LayoutCallingBinding

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int)
            : super(context, attrs, defStyleAttr)

    init {
        val view = LayoutInflater.from(context).inflate(R.layout.layout_calling, this, true)
        binding = LayoutCallingBinding.bind(view)
    }

    fun initCall(protocol: LiveCallProtocol) {
        liveCallProtocol = protocol
        binding.ivHangup.setOnClickListener {
            liveCallProtocol.hangupCalling()
        }

        binding.ivAudioMute.isSelected = protocol.room()?.isLocalAudioMuted() ?: true
        binding.ivAudioMute.setOnClickListener {
            liveCallProtocol.room()?.muteLocalAudio(!binding.ivAudioMute.isSelected)
            binding.ivAudioMute.isSelected = liveCallProtocol.room()?.isLocalAudioMuted() ?: true
        }

        binding.ivSwitchSpeaker.isSelected = liveCallProtocol.room()?.isSpeakerMuted() ?: true
        binding.ivSwitchSpeaker.setOnClickListener {
            liveCallProtocol.room()?.muteSpeaker(!binding.ivSwitchSpeaker.isSelected)
            binding.ivSwitchSpeaker.isSelected = liveCallProtocol.room()?.isSpeakerMuted() ?: true
        }

        binding.ivOpenCloseCamera.isSelected = liveCallProtocol.room()?.isLocalVideoMuted() ?: true
        binding.ivOpenCloseCamera.setOnClickListener {
            liveCallProtocol.room()?.muteLocalVideo(!binding.ivOpenCloseCamera.isSelected)
            binding.ivOpenCloseCamera.isSelected =
                liveCallProtocol.room()?.isLocalVideoMuted() ?: true
        }

        binding.ivSwitchCamera.setOnClickListener {
            liveCallProtocol.room()?.switchLocalCamera()
        }
    }
}