package com.thk.im.android.ui.fragment.layout

import android.annotation.SuppressLint
import android.content.Context
import android.content.Context.INPUT_METHOD_SERVICE
import android.os.Bundle
import android.os.ResultReceiver
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.emoji2.widget.EmojiEditText
import androidx.fragment.app.Fragment
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import com.thk.im.android.base.LLog
import com.thk.im.android.base.ToastUtils
import com.thk.im.android.core.IMCoreManager
import com.thk.im.android.core.IMFileFormat
import com.thk.im.android.db.MsgType
import com.thk.im.android.ui.R
import com.thk.im.android.ui.databinding.LayoutMessageInputBinding
import com.thk.im.android.ui.fragment.adapter.MessageAdapter
import com.thk.im.android.ui.manager.IMAudioMsgData
import com.thk.im.android.ui.manager.IMUIManager
import com.thk.im.android.ui.protocol.AudioCallback
import com.thk.im.android.ui.protocol.AudioStatus
import com.thk.im.android.ui.protocol.internal.IMMsgPreviewer
import com.thk.im.android.ui.protocol.internal.IMMsgSender
import java.io.File
import kotlin.math.abs

@SuppressLint("ClickableViewAccessibility")
class IMInputLayout : ConstraintLayout {

    private var binding: LayoutMessageInputBinding

    private lateinit var msgSender: IMMsgSender
    private lateinit var msgPreviewer: IMMsgPreviewer
    private lateinit var fragment: Fragment

    private var audioEventY = 0f
    private var audioCancel = false

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    init {
        val view = LayoutInflater.from(context).inflate(R.layout.layout_message_input, this, true)
        binding = LayoutMessageInputBinding.bind(view)
        binding.etMessage.isFocusable = true
        binding.etMessage.isFocusableInTouchMode = true
        binding.etMessage.requestFocus()
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

        binding.ivAudioRecord.setOnClickListener {
            binding.ivAudioRecord.isSelected = !binding.ivAudioRecord.isSelected
            if (binding.ivAudioRecord.isSelected) {
                binding.btRecordVoice.visibility = View.VISIBLE
                binding.etMessage.visibility = View.INVISIBLE
            } else {
                binding.btRecordVoice.visibility = View.INVISIBLE
                binding.etMessage.visibility = View.VISIBLE
                binding.etMessage.requestFocus()
            }
        }

        binding.ivEmo.setOnClickListener {
            binding.ivAudioRecord.isSelected = false
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
            binding.ivAudioRecord.isSelected = false
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

        binding.btRecordVoice.setOnTouchListener { _, p1 ->
            when (p1.action) {
                MotionEvent.ACTION_DOWN -> {
                    audioEventY = p1.y
                    audioCancel = false
                    checkAudioPermission()
                }

                MotionEvent.ACTION_MOVE -> {
                    val cancel = abs(p1.y - audioEventY) > 180
                    if (audioCancel != cancel) {
                        audioCancel = cancel
                        if (audioCancel) {
                            binding.btRecordVoice.text = "松开 取消"
                        } else {
                            binding.btRecordVoice.text = "松开 发送"
                        }
                    }
                }

                MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> {
                    stopAudioRecording()
                }
            }
            true
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
        if (isKeyShowing || height <= 0) {
            binding.ivAddMore.isSelected = false
            binding.ivEmo.isSelected = false
        }
    }

    fun addInputContent(text: String) {
        val index: Int = binding.etMessage.selectionStart
        if (binding.etMessage.text != null) {
            binding.etMessage.text!!.insert(index, text)
        } else {
            val newText = binding.etMessage.text.toString() + text
            binding.etMessage.setText(newText.toCharArray(), 0, newText.toCharArray().size)
        }
    }

    fun getEditText(): EmojiEditText {
        return binding.etMessage
    }

    fun deleteContent(count: Int) {
        val index: Int = binding.etMessage.selectionStart
        if (!binding.etMessage.text.isNullOrEmpty()) {
            if (binding.etMessage.selectionEnd != index) {
                binding.etMessage.text.delete(index, binding.etMessage.selectionEnd)
                return
            }
            val chars = binding.etMessage.text.toString().toCharArray()
            for (i in 0 until count) {
                if (index >= 2) {
                    if (Character.isSurrogatePair(chars[index - 2], chars[index - 1])) {
                        binding.etMessage.text.delete(index - 2, index)
                    } else {
                        binding.etMessage.text.delete(index - 1, index)
                    }
                } else {
                    binding.etMessage.text.delete(index - 1, index)
                }
            }
        }
    }

    private fun checkAudioPermission() {
        val granted = XXPermissions.isGranted(context, Permission.RECORD_AUDIO)
        if (!granted) {
            XXPermissions.with(context).permission(Permission.RECORD_AUDIO).request { _, all ->
                    if (!all) {
                        // TODO
                        ToastUtils.show("请开启录音权限")
                    }
                }
        } else {
            startRecordingAudio()
        }
    }

    private fun startRecordingAudio() {
        val contentProvider = IMUIManager.mediaProvider ?: return

        if (!contentProvider.isRecordingAudio()) {
            binding.btRecordVoice.text = "松开 发送"
            val path = IMCoreManager.storageModule.allocSessionFilePath(
                msgSender.getSession().id,
                "${System.currentTimeMillis() / 100}_audio.oga",
                IMFileFormat.Audio.value
            )
            val file = File(path)
            if (file.exists()) {
                if (!file.delete()) {
                    return
                }
            }

            if (!file.createNewFile()) {
                return
            }
            contentProvider.startRecordAudio(path, 60 * 1000, object : AudioCallback {

                override fun audioData(path: String, second: Int, db: Double, state: AudioStatus) {
                    LLog.v("notify $second, $db ${state.value}")
                    handler.post {
                        onAudioCallback(path, second, db, state)
                    }
                }
            })
        }
    }

    private fun onAudioCallback(path: String, second: Int, db: Double, state: AudioStatus) {
        when (state) {
            AudioStatus.Ing, AudioStatus.Waiting -> {
                ToastUtils.show("录音时长:$second, db: $db")
            }

            AudioStatus.Finished -> {
                if (!audioCancel) {
                    val audioMsgData = IMAudioMsgData()
                    audioMsgData.duration = second
                    audioMsgData.path = path
                    audioMsgData.played = true
                    msgSender.sendMessage(MsgType.Audio.value, audioMsgData)
                }
            }

            AudioStatus.Exited -> {
                val file = File(path)
                if (file.exists()) {
                    if (!file.delete()) {
                        return
                    }
                }
                ToastUtils.show("录音失败")
            }
        }
    }

    private fun stopAudioRecording() {
        binding.btRecordVoice.text = "按住 说话"
        val contentProvider = IMUIManager.mediaProvider ?: return
        contentProvider.stopRecordAudio()
    }

    fun setSelectMode(selected: Boolean) {
        if (selected) {
            binding.llMessageInput.visibility = View.GONE
            binding.llMessageOperator.visibility = View.VISIBLE
        } else {
            binding.llMessageInput.visibility = View.VISIBLE
            binding.llMessageOperator.visibility = View.GONE
        }
    }


}