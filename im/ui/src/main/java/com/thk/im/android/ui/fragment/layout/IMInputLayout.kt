package com.thk.im.android.ui.fragment.layout

import android.content.Context
import android.content.Context.INPUT_METHOD_SERVICE
import android.os.Bundle
import android.os.ResultReceiver
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import com.thk.im.android.db.MsgType
import com.thk.im.android.ui.R
import com.thk.im.android.ui.databinding.LayoutMessageInputBinding
import com.thk.im.android.ui.protocol.IMMsgPreviewer
import com.thk.im.android.ui.protocol.IMMsgSender

class IMInputLayout : ConstraintLayout {

    private var binding: LayoutMessageInputBinding

    private lateinit var msgSender: IMMsgSender
    private lateinit var msgPreviewer: IMMsgPreviewer
    private lateinit var fragment: Fragment

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int)
            : super(context, attrs, defStyleAttr)

    init {
        val view = LayoutInflater.from(context).inflate(R.layout.layout_message_input, this, true)
        binding = LayoutMessageInputBinding.bind(view)
        binding.etMessage.isFocusable = true
        binding.etMessage.isFocusableInTouchMode = true

        binding.tvSendMsg.setOnClickListener {
            binding.etMessage.text?.let {
                msgSender.sendMessage(MsgType.TEXT.value, it.toString())
            }
            binding.etMessage.text = null
        }

        binding.etMessage.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun afterTextChanged(p0: Editable?) {
                if ((p0?.length ?: 0) > 0) {
                    binding.tvSendMsg.visibility = View.VISIBLE
                    binding.ivAddMore.visibility = View.GONE
                } else {
                    binding.tvSendMsg.visibility = View.GONE
                    binding.ivAddMore.visibility = View.VISIBLE
                }
            }
        })

        binding.ivEmo.setOnClickListener {
            binding.ivVoice.isSelected = false
            binding.ivAddMore.isSelected = false
            binding.btRecordVoice.visibility = View.GONE
            binding.etMessage.visibility = View.VISIBLE
            binding.ivEmo.isSelected = !binding.ivEmo.isSelected
            if (binding.ivEmo.isSelected) {
                // 关闭键盘 显示表情
                if (msgSender.isKeyboardShowing()) {
                    closeKeyboard(object : ResultReceiver(handler) {
                        override fun onReceiveResult(resultCode: Int, resultData: Bundle?) {
                            super.onReceiveResult(resultCode, resultData)
                            msgSender.showBottomPanel(0)
                        }
                    })
                } else {
                    msgSender.showBottomPanel(0)
                }
            } else {
                // 关闭表情 显示键盘
                openKeyboard()
            }
        }

        binding.ivAddMore.setOnClickListener {
            binding.ivVoice.isSelected = false
            binding.ivEmo.isSelected = false
            binding.btRecordVoice.visibility = View.GONE
            binding.etMessage.visibility = View.VISIBLE
            binding.ivAddMore.isSelected = !binding.ivAddMore.isSelected
            if (binding.ivAddMore.isSelected) {
                // 关闭键盘 显示更多
                if (msgSender.isKeyboardShowing()) {
                    closeKeyboard(object : ResultReceiver(handler) {
                        override fun onReceiveResult(resultCode: Int, resultData: Bundle?) {
                            super.onReceiveResult(resultCode, resultData)
                            msgSender.showBottomPanel(1)
                        }
                    })
                } else {
                    msgSender.showBottomPanel(1)
                }
            } else {
                // 关闭更多 显示键盘
                msgSender.openKeyboard()
            }
        }
    }

    fun init(fragment: Fragment, sender: IMMsgSender, previewer: IMMsgPreviewer) {
        this.fragment = fragment
        this.msgSender = sender
        this.msgPreviewer = previewer
    }

    fun openKeyboard(): Boolean {
        val imm = context.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        binding.etMessage.requestFocus()
        imm.showSoftInput(binding.etMessage, 0)
        return true
    }

    private fun closeKeyboard(resultReceiver: ResultReceiver) {
        val imm = context.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(windowToken, 0, resultReceiver)
    }

    fun closeKeyboard(): Boolean {
        val imm = context.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        binding.etMessage.requestFocus()
        imm.hideSoftInputFromWindow(windowToken, 0)
        return true
    }

    fun getLayoutHeight(): Int {
        return height
    }

    fun onKeyboardChange(isKeyShowing: Boolean, height: Int, duration: Long) {
        if (isKeyShowing || height <=0 ) {
            binding.ivAddMore.isSelected = false
            binding.ivEmo.isSelected = false
        }
    }


}