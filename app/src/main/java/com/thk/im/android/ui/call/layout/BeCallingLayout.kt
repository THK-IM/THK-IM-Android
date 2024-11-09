package com.thk.im.android.ui.call.layout

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.thk.im.android.R
import com.thk.im.android.databinding.LayoutBeCallingBinding
import com.thk.im.android.ui.call.LiveCallProtocol

class BeCallingLayout : ConstraintLayout {

    private var binding: LayoutBeCallingBinding
    private lateinit var liveCallProtocol: LiveCallProtocol

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int)
            : super(context, attrs, defStyleAttr)

    init {
        val view = LayoutInflater.from(context).inflate(R.layout.layout_be_calling, this, true)
        binding = LayoutBeCallingBinding.bind(view)
    }

    fun initCall(protocol: LiveCallProtocol) {
        liveCallProtocol = protocol
        binding.ivHangup.setOnClickListener {
            liveCallProtocol.rejectCalling()
        }

        binding.ivAccept.setOnClickListener {
            liveCallProtocol.acceptCalling()
        }

        binding.ivOpenCloseCamera.isSelected = liveCallProtocol.room().isLocalVideoMuted()
        binding.ivOpenCloseCamera.setOnClickListener {
            liveCallProtocol.room().muteLocalVideo(!binding.ivOpenCloseCamera.isSelected)
            binding.ivOpenCloseCamera.isSelected = liveCallProtocol.room().isLocalVideoMuted()
        }

        binding.ivSwitchCamera.setOnClickListener {
            liveCallProtocol.room().switchLocalCamera()
        }
    }
}