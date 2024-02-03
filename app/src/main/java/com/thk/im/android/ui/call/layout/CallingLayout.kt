package com.thk.im.android.ui.call.layout

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.thk.im.android.R
import com.thk.im.android.core.base.IMImageLoader
import com.thk.im.android.core.db.entity.User
import com.thk.im.android.databinding.LayoutBeCallingBinding
import com.thk.im.android.ui.call.LiveCallProtocol

class CallingLayout : ConstraintLayout {

    private var binding: LayoutBeCallingBinding

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int)
            : super(context, attrs, defStyleAttr)

    init {
        val view = LayoutInflater.from(context).inflate(R.layout.layout_be_calling, this, true)
        binding = LayoutBeCallingBinding.bind(view)
    }

    fun initUI(user: User, liveCallProtocol: LiveCallProtocol) {
        user.avatar?.let {
            IMImageLoader.displayImageUrl(binding.ivOther, it)
        }
        binding.tvOther.text = user.nickname
    }
}