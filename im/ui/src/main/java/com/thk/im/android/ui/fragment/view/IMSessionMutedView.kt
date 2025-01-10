package com.thk.im.android.ui.fragment.view

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.RelativeLayout
import com.thk.im.android.core.base.extension.setShape
import com.thk.im.android.ui.R
import com.thk.im.android.ui.databinding.ViewSessionMutedBinding

class IMSessionMutedView : RelativeLayout {

    private var binding: ViewSessionMutedBinding

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int)
            : super(context, attrs, defStyleAttr)

    init {
        val view =
            LayoutInflater.from(context).inflate(R.layout.view_session_muted, this, true)
        binding = ViewSessionMutedBinding.bind(view)
    }

}