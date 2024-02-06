package com.thk.im.android.ui.call.layout

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.thk.im.android.R
import com.thk.im.android.databinding.LayoutRequestCallBinding
import com.thk.im.android.ui.call.LiveCallProtocol

class RequestCallLayout : ConstraintLayout {

    private var binding: LayoutRequestCallBinding
    private lateinit var liveCallProtocol: LiveCallProtocol

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int)
            : super(context, attrs, defStyleAttr)

    init {
        val view = LayoutInflater.from(context).inflate(R.layout.layout_request_call, this, true)
        binding = LayoutRequestCallBinding.bind(view)
    }

    fun initCall(protocol: LiveCallProtocol) {
        this.liveCallProtocol = protocol
        binding.ivHangup.setOnClickListener {
            liveCallProtocol.hangup()
        }

        binding.ivOpenCloseCamera.isSelected = liveCallProtocol.isCurrentCameraOpened()
        binding.ivOpenCloseCamera.setOnClickListener {
            if (it.isSelected) {
                liveCallProtocol.openLocalCamera()
            } else {
                liveCallProtocol.closeLocalCamera()
            }
            it.isSelected = !it.isSelected
        }

        binding.ivSwitchCamera.setOnClickListener {
            liveCallProtocol.switchLocalCamera()
        }
    }
}