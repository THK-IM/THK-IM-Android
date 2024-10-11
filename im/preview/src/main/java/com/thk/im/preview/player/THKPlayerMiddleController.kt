package com.thk.im.preview.player

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.RelativeLayout
import com.thk.im.android.preview.R
import com.thk.im.android.preview.databinding.LayoutPlayerControllerMiddleBinding

class THKPlayerMiddleController : RelativeLayout {

    private val binding: LayoutPlayerControllerMiddleBinding

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    init {
        val view = LayoutInflater.from(context).inflate(
            R.layout.layout_player_controller_middle, this, true
        )
        binding = LayoutPlayerControllerMiddleBinding.bind(view)
    }

    fun showLoading(loading: Boolean) {
        binding.pbProgress.visibility = if (loading) View.VISIBLE else View.GONE
    }
}