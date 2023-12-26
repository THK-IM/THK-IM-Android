package com.thk.im.android.ui.component

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.RelativeLayout
import androidx.core.content.ContextCompat
import com.thk.im.android.R
import com.thk.im.android.databinding.LayoutNavigationItemBinding

class NavigationItemLayout : RelativeLayout {

    private var binding: LayoutNavigationItemBinding

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int)
            : super(context, attrs, defStyleAttr)

    init {
        val view = LayoutInflater.from(context).inflate(R.layout.layout_navigation_item, this, true)
        binding = LayoutNavigationItemBinding.bind(view)
    }

    fun setIconTitle(iconRes: Int, title: String) {
        binding.ivNavigationIcon.setImageDrawable(ContextCompat.getDrawable(context, iconRes))
        binding.tvNavigationTitle.text = title
    }

}