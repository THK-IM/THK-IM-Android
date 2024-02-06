package com.thk.im.android.ui.call.layout

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.thk.im.android.R
import com.thk.im.android.databinding.LayoutCallingBinding
import com.thk.im.android.ui.call.LiveCallProtocol

class CallingLayout : ConstraintLayout {

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

    }
}