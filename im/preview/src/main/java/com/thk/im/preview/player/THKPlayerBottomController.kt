package com.thk.im.preview.player

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.RelativeLayout
import android.widget.SeekBar
import com.thk.im.android.core.base.utils.DateUtils
import com.thk.im.android.core.base.utils.ShapeUtils
import com.thk.im.android.preview.R
import com.thk.im.android.preview.databinding.LayoutPlayerControllerBottomBinding
import java.util.Locale


class THKPlayerBottomController : RelativeLayout {

    private val binding: LayoutPlayerControllerBottomBinding

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    init {
        val view = LayoutInflater.from(context).inflate(
            R.layout.layout_player_controller_bottom, this, true
        )
        binding = LayoutPlayerControllerBottomBinding.bind(view)

        binding.lyContainer.background = ShapeUtils.createGradientRectangleDrawable(
            Color.parseColor("#00000000"),
            Color.parseColor("#80000000"),
            GradientDrawable.Orientation.TOP_BOTTOM
        )

        binding.tvCurrentTime.text = String.format(Locale.CHINESE, "00:00")
        binding.tvTotalTime.text = String.format(Locale.CHINESE, "00:00")
    }

    fun updateTime(total: Long, buffered: Long, current: Long) {
        binding.tvTotalTime.text = DateUtils.secondToDuration((total).toInt())
        binding.tvCurrentTime.text = DateUtils.secondToDuration((current).toInt())
        if (total > 0) {
            binding.seekbar.progress = (current * 100 / total).toInt()
            binding.seekbar.secondaryProgress = (buffered * 100 / total).toInt()
        }
    }

    fun hideSeekBar(hide: Boolean) {
        binding.seekbar.visibility = if (hide) View.GONE else View.VISIBLE
    }

    fun hideTotalTime(hide: Boolean) {
        binding.tvTotalTime.visibility = if (hide) View.GONE else View.VISIBLE
        binding.tvTimeM.visibility = if (hide) View.GONE else View.VISIBLE
    }

    fun hidePlayButton(hide: Boolean) {
        binding.ivPlayPause.visibility = if (hide) View.GONE else View.VISIBLE
    }

    fun setSeekbarDragListener(listener: SeekBar.OnSeekBarChangeListener) {
        binding.seekbar.setOnSeekBarChangeListener(listener)
    }

    fun setMuted(muted: Boolean) {
        binding.ivMute.isSelected = muted
    }

    fun setItemClickListener(l: OnClickListener) {
        binding.ivMute.setOnClickListener(l)
        binding.ivPlayPause.setOnClickListener(l)
    }
}