package com.thk.im.android.ui.fragment.layout

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
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
import com.thk.im.android.ui.fragment.adapter.PanelFragmentAdapter
import com.thk.im.android.ui.fragment.adapter.PanelMenuAdapter
import com.thk.im.android.ui.fragment.panel.IMFunctionAdapter
import com.thk.im.android.ui.protocol.internal.IMMsgPreviewer
import com.thk.im.android.ui.protocol.internal.IMMsgSender

class IMBottomLayout : ConstraintLayout {

    private var contentHeight = 0
    private var binding: LayoutMessageBottomBinding

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
        binding.vpMenu.layoutManager = linearLayoutManager

        val adapter = PanelMenuAdapter()
        adapter.menuSelectListener = object : PanelMenuAdapter.MenuSelectListener {
            override fun onSelected(position: Int) {
                binding.vpContent.currentItem = position
            }
        }
        binding.vpMenu.adapter = adapter

        binding.vpContent.adapter = PanelFragmentAdapter(lifecycleOwner as Fragment)
        binding.vpContent.orientation = ViewPager2.ORIENTATION_HORIZONTAL
        binding.vpContent.offscreenPageLimit = 5
        binding.vpContent.registerOnPageChangeCallback(object : OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                adapter.setSelected(position)
            }
        })
        binding.vpContent.currentItem = 0


        val gridLayoutManager = GridLayoutManager(context, 4)
        binding.rcvFunctions.layoutManager = gridLayoutManager
        val functionsAdapter = IMFunctionAdapter(this.msgSender)
        binding.rcvFunctions.adapter = functionsAdapter
    }

    fun showBottomPanel(position: Int) {
        binding.vpContent.currentItem = position
        binding.root.visibility = VISIBLE
        contentHeight = if (position == 0) {
            binding.vpMenu.visibility = VISIBLE
            binding.vpContent.visibility = VISIBLE
            binding.rcvFunctions.visibility = GONE
            300.dp2px()
        } else {
            binding.vpMenu.visibility = GONE
            binding.vpContent.visibility = GONE
            binding.rcvFunctions.visibility = VISIBLE
            200.dp2px()
        }
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