package com.thk.im.android.ui.call.layout

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.thk.im.android.R
import com.thk.im.android.core.base.IMImageLoader
import com.thk.im.android.core.db.entity.User
import com.thk.im.android.databinding.LayoutCallingInfoBinding
import com.thk.im.android.ui.call.LiveCallProtocol

class CallingInfoLayout : ConstraintLayout {

    private var binding: LayoutCallingInfoBinding

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int)
            : super(context, attrs, defStyleAttr)

    init {
        val view = LayoutInflater.from(context).inflate(R.layout.layout_calling_info, this, true)
        binding = LayoutCallingInfoBinding.bind(view)
    }

    fun setUserInfo(user: User) {
        user.avatar?.let {
            IMImageLoader.displayImageUrl(binding.ivOther, it)
        }
        binding.tvOther.text = user.nickname
    }
}