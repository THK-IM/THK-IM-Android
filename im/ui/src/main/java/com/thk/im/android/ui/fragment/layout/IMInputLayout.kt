package com.thk.im.android.ui.fragment.layout

import android.annotation.SuppressLint
import android.content.Context
import android.content.Context.INPUT_METHOD_SERVICE
import android.graphics.Color
import android.os.Bundle
import android.os.ResultReceiver
import android.text.Editable
import android.text.Spanned
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.text.getSpans
import androidx.emoji2.widget.EmojiEditText
import androidx.lifecycle.LifecycleOwner
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import com.thk.im.android.core.IMCoreManager
import com.thk.im.android.core.IMFileFormat
import com.thk.im.android.core.MsgType
import com.thk.im.android.core.base.LLog
import com.thk.im.android.core.base.utils.ToastUtils
import com.thk.im.android.core.db.entity.Session
import com.thk.im.android.core.db.entity.SessionMember
import com.thk.im.android.core.db.entity.User
import com.thk.im.android.ui.R
import com.thk.im.android.ui.databinding.LayoutMessageInputBinding
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

    private lateinit var lifecycleOwner: LifecycleOwner
    private lateinit var session: Session
    private var msgPreviewer: IMMsgPreviewer? = null
    private var msgSender: IMMsgSender? = null

    private var audioEventY = 0f
    private var audioCancel = false
    private var atMap = mutableMapOf<String, String>()

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
                val data = it.toString()
                val regex = "(?<=@)(.+?)(?=\\s)".toRegex()
                var atUsers = ""
                val body = regex.replace(data) { result ->
                    var replacement = result.value
                    for ((k, v) in atMap) {
                        if (v == result.value) {
                            replacement = k
                            if (atUsers.isNotEmpty()) {
                                atUsers += "#"
                            }
                            atUsers += k
                        }
                    }
                    replacement
                }
                msgSender?.sendMessage(MsgType.TEXT.value, body, data, atUsers.ifEmpty { null })
                atMap.clear()
                binding.etMessage.text.clearSpans()
                binding.etMessage.text.clear()
                binding.etMessage.text = null
            }
        }

        binding.etMessage.setOnKeyListener(object : View.OnKeyListener {
            override fun onKey(v: View?, keyCode: Int, event: KeyEvent?): Boolean {
                if (event?.action == KeyEvent.ACTION_DOWN) {
                    if (keyCode == KeyEvent.KEYCODE_DEL) {
                        deleteContent(1)
                        return true
                    }
                }

                return false
            }

        })
        binding.etMessage.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (p0 != null) {
                    LLog.d(p0.toString())
                }
            }

            override fun afterTextChanged(p0: Editable?) {
                p0?.let {
                    val selectionStart = binding.etMessage.selectionStart
                    if (selectionStart > 0 && it.length > selectionStart - 1) {
                        if (it[selectionStart - 1] == '@') {
                            showAtSessionMemberPopup()
                        }
                    }
                }
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
                msgSender?.let {
                    // 关闭键盘 显示表情
                    if (it.isKeyboardShowing()) {
                        closeKeyboard(object : ResultReceiver(handler) {
                            override fun onReceiveResult(resultCode: Int, resultData: Bundle?) {
                                super.onReceiveResult(resultCode, resultData)
                                it.showBottomPanel(0)
                            }
                        })
                    } else {
                        it.showBottomPanel(0)
                    }
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
                msgSender?.let {
                    // 关闭键盘 显示更多
                    if (it.isKeyboardShowing()) {
                        closeKeyboard(object : ResultReceiver(handler) {
                            override fun onReceiveResult(resultCode: Int, resultData: Bundle?) {
                                super.onReceiveResult(resultCode, resultData)
                                it.showBottomPanel(1)
                            }
                        })
                    } else {
                        it.showBottomPanel(1)
                    }
                }
            } else {
                // 关闭更多 显示键盘
                msgSender?.openKeyboard()
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

        binding.ivMsgOprCancel.setOnClickListener {
            msgSender?.setSelectMode(false, null)
        }

        binding.ivMsgOprDelete.setOnClickListener {
            msgSender?.deleteSelectedMessages()
            msgSender?.setSelectMode(false, null)
        }

        binding.ivMsgOprForward.setOnClickListener {
            msgSender?.forwardSelectedMessages(1)
            msgSender?.setSelectMode(false, null)
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

    fun onKeyboardChange(isKeyShowing: Boolean, height: Int, duration: Long) {
        if (isKeyShowing || height <= 0) {
            binding.ivAddMore.isSelected = false
            binding.ivEmo.isSelected = false
        }
    }

    fun addInputContent(text: String) {
        if (binding.etMessage.text != null) {
            val index: Int = binding.etMessage.selectionStart
            binding.etMessage.text!!.insert(index, text)
        } else {
            binding.etMessage.setText(text.toCharArray(), 0, text.toCharArray().size)
        }
    }

    fun getEditText(): EmojiEditText {
        return binding.etMessage
    }

    fun deleteContent(count: Int) {
        val atSpans = binding.etMessage.text.getSpans<ForegroundColorSpan>()
        val selectionStart = binding.etMessage.selectionStart
        val selectionEnd = binding.etMessage.selectionEnd
        var deleted = false
        for (span in atSpans) {
            val spanStart = binding.etMessage.text.getSpanStart(span)
            val spanEnd = binding.etMessage.text.getSpanEnd(span)
            LLog.d("$spanStart $spanEnd $selectionStart $selectionEnd")
            if (selectionStart + 1 in spanStart..spanEnd || selectionEnd - 1 in spanStart..spanEnd) {
                deleted = true
                binding.etMessage.text.removeSpan(span)
                binding.etMessage.text.delete(spanStart, spanEnd)
            }
        }
        if (deleted) {
            return
        }
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
                session.id,
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
                    msgSender?.sendMessage(MsgType.Audio.value, null, audioMsgData)
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

    private fun showAtSessionMemberPopup() {
        msgSender?.openAtPopupView()
    }

    fun addAtSessionMember(user: User, sessionMember: SessionMember?) {
        atMap["${user.id}"] = user.nickname
        LLog.d("insertAtSessionMember $sessionMember, $user")
        val selectionStart = binding.etMessage.selectionStart
        val content = binding.etMessage.text
        if (selectionStart > 0 && content.length > selectionStart - 1) {
            if (content[selectionStart - 1] == '@') {
                binding.etMessage.text.insert(selectionStart, "${user.nickname} ")
                val atSpan = ForegroundColorSpan(Color.parseColor("#1b7ae8"))
                binding.etMessage.text.setSpan(
                    atSpan,
                    selectionStart - 1,
                    selectionStart + user.nickname.length,
                    Spanned.SPAN_INCLUSIVE_INCLUSIVE
                )
                return
            }
        }

        binding.etMessage.text.insert(selectionStart, "@${user.nickname} ")
        val atSpan = ForegroundColorSpan(Color.parseColor("#1b7ae8"))
        binding.etMessage.text.setSpan(
            atSpan,
            selectionStart,
            selectionStart + user.nickname.length + 1,
            Spanned.SPAN_INCLUSIVE_INCLUSIVE
        )
    }


}