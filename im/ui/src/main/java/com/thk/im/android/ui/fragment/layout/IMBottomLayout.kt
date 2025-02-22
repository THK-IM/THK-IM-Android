package com.thk.im.android.ui.fragment.layout

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.thk.im.android.core.base.extension.dp2px
import com.thk.im.android.core.db.entity.Session
import com.thk.im.android.ui.R
import com.thk.im.android.ui.databinding.LayoutMessageBottomBinding
import com.thk.im.android.ui.fragment.adapter.IMEmojiFragmentAdapter
import com.thk.im.android.ui.fragment.adapter.IMEmojiTitleAdapter
import com.thk.im.android.ui.fragment.adapter.IMFunctionAdapter
import com.thk.im.android.ui.manager.IMUIManager
import com.thk.im.android.ui.protocol.internal.IMMsgPreviewer
import com.thk.im.android.ui.protocol.internal.IMMsgSender
import com.thk.im.android.ui.utils.ScreenUtils

class IMBottomLayout : ConstraintLayout {

    private var contentHeight = 0
    private val binding: LayoutMessageBottomBinding

    private lateinit var lifecycleOwner: LifecycleOwner
    private lateinit var session: Session
    private var msgPreviewer: IMMsgPreviewer? = null
    private var msgSender: IMMsgSender? = null

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int)
            : super(context, attrs, defStyleAttr)

    init {
        val view = LayoutInflater.from(context).inflate(R.layout.layout_message_bottom, this, true)
        binding = LayoutMessageBottomBinding.bind(view)
        val bgLayoutColor =
            IMUIManager.uiResourceProvider?.panelBgColor() ?: Color.parseColor("#FFFFFF")
        val inputTextColor =
            IMUIManager.uiResourceProvider?.inputTextColor() ?: Color.parseColor("#333333")
        binding.lyEmojiTab.setBackgroundColor(bgLayoutColor)
        ContextCompat.getDrawable(context, R.drawable.ic_emoji_del)?.let {
            it.setTint(inputTextColor)
            binding.ivEmojiDel.setImageDrawable(it)
        }
    }

    fun init(
        lifecycleOwner: LifecycleOwner,
        session: Session,
        sender: IMMsgSender?,
        previewer: IMMsgPreviewer?
    ) {
        this.lifecycleOwner = lifecycleOwner
        this.session = session
        this.msgSender = sender
        this.msgPreviewer = previewer
        initView()
    }

    private fun initView() {
        binding.root.visibility = INVISIBLE
        val linearLayoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        binding.vpEmojiTitle.layoutManager = linearLayoutManager

        val adapter = IMEmojiTitleAdapter()
        adapter.menuSelectListener = object : IMEmojiTitleAdapter.MenuSelectListener {
            override fun onSelected(position: Int) {
                binding.vpEmojiContent.currentItem = position
            }
        }
        binding.vpEmojiTitle.adapter = adapter

        binding.ivEmojiDel.setOnClickListener {
            msgSender?.deleteContent(1)
        }

        binding.vpEmojiContent.adapter = IMEmojiFragmentAdapter(lifecycleOwner as Fragment)
        binding.vpEmojiContent.orientation = ViewPager2.ORIENTATION_HORIZONTAL
        binding.vpEmojiContent.registerOnPageChangeCallback(object : OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                adapter.setSelected(position)
            }
        })
        binding.vpEmojiContent.currentItem = 0

        val gridLayoutManager = GridLayoutManager(context, 4)
        binding.rcvFunctions.layoutManager = gridLayoutManager
        val functionsAdapter = IMFunctionAdapter(this.msgSender)
        binding.rcvFunctions.adapter = functionsAdapter
    }

    fun showBottomPanel(position: Int) {
        binding.vpEmojiContent.currentItem = position
        binding.root.visibility = VISIBLE
        contentHeight = if (position == 0) {
            binding.lyEmojiTab.visibility = VISIBLE
            binding.vpEmojiContent.visibility = VISIBLE
            binding.rcvFunctions.visibility = GONE
            if (ScreenUtils.isMultiWindowMode(context)) {
                180.dp2px()
            } else {
                300.dp2px()
            }
        } else {
            binding.lyEmojiTab.visibility = GONE
            binding.vpEmojiContent.visibility = GONE
            binding.rcvFunctions.visibility = VISIBLE
            if (ScreenUtils.isMultiWindowMode(context)) {
                120.dp2px()
            } else {
                200.dp2px()
            }
        }
        this.layoutParams.height = contentHeight
        msgSender?.moveUpAlwaysShowView(false, contentHeight, 150)
    }


    fun getContentHeight(): Int {
        return contentHeight
    }


    fun closeBottomPanel() {
        binding.root.visibility = INVISIBLE
        contentHeight = 0
        msgSender?.moveUpAlwaysShowView(false, contentHeight, 150)
    }

    fun onKeyboardChange(isKeyShowing: Boolean, height: Int, duration: Long) {
        if (isKeyShowing || height <= 0) {
            binding.root.visibility = INVISIBLE
            contentHeight = 0
        } else {
            binding.root.visibility = VISIBLE
        }
    }

}