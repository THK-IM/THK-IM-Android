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
import com.thk.im.android.core.SessionMuted
import com.thk.im.android.core.SessionStatus
import com.thk.im.android.core.base.BaseSubscriber
import com.thk.im.android.core.base.LLog
import com.thk.im.android.core.base.RxTransform
import com.thk.im.android.core.base.extension.setShape
import com.thk.im.android.core.base.utils.ToastUtils
import com.thk.im.android.core.db.entity.Message
import com.thk.im.android.core.db.entity.Session
import com.thk.im.android.core.db.entity.SessionMember
import com.thk.im.android.core.db.entity.User
import com.thk.im.android.ui.R
import com.thk.im.android.ui.databinding.LayoutMessageInputBinding
import com.thk.im.android.ui.manager.IMAudioMsgData
import com.thk.im.android.ui.manager.IMChatFunction
import com.thk.im.android.ui.manager.IMReeditMsgData
import com.thk.im.android.ui.manager.IMUIManager
import com.thk.im.android.ui.protocol.AudioCallback
import com.thk.im.android.ui.protocol.AudioStatus
import com.thk.im.android.ui.protocol.internal.IMMsgPreviewer
import com.thk.im.android.ui.protocol.internal.IMMsgSender
import com.thk.im.android.ui.utils.AtStringUtils
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import java.io.File
import kotlin.math.abs

@SuppressLint("ClickableViewAccessibility")
class IMInputLayout : ConstraintLayout {

    private val binding: LayoutMessageInputBinding
    private val disposables = CompositeDisposable()

    private lateinit var lifecycleOwner: LifecycleOwner
    private lateinit var session: Session
    private var msgPreviewer: IMMsgPreviewer? = null
    private var msgSender: IMMsgSender? = null
    private var replyMsg: Message? = null
    private var reeditMsg: Message? = null

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
        binding.ivSendMsg.setOnClickListener {
            sendInputContent()
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
                    binding.ivSendMsg.visibility = View.VISIBLE
                    binding.ivAddMore.visibility = View.GONE
                } else {
                    binding.ivSendMsg.visibility = View.GONE
                    binding.ivAddMore.visibility = View.VISIBLE
                    this@IMInputLayout.reeditMsg = null
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

        binding.vReplyLine.setShape(
            Color.parseColor("#ff999999"), floatArrayOf(2f, 2f, 2f, 2f), false
        )

        binding.ivReplyClose.setOnClickListener {
            clearReplyMessage()
            disposables.clear()
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        disposables.clear()
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

        if (session.functionFlag.and(IMChatFunction.Audio.value) == 0L) {
            binding.ivAudioRecord.visibility = GONE
        }
        if (IMUIManager.getFunctionProvider(session).isEmpty()) {
            binding.ivAddMore.visibility = GONE
        }
        if (session.functionFlag.and(IMChatFunction.Forward.value) == 0L) {
            binding.ivMsgOprForward.visibility = GONE
        }
    }

    private fun sendInputContent() {
        binding.etMessage.text?.let {
            val text = it.toString()
            val (body, atUIds) = AtStringUtils.replaceAtNickNamesToUIds(text) { nickname ->
                for ((k, v) in atMap) {
                    if (v == nickname) {
                        return@replaceAtNickNamesToUIds k.toLongOrNull()
                            ?: return@replaceAtNickNamesToUIds 0L
                    }
                }
                return@replaceAtNickNamesToUIds 0L
            }
            if (reeditMsg != null) {
                val msg = IMReeditMsgData(reeditMsg!!.sid, reeditMsg!!.msgId, body)
                msgSender?.sendMessage(MsgType.Reedit.value, msg, null, null)
                reeditMsg = null
            } else {
                msgSender?.sendMessage(MsgType.Text.value, body, null, atUIds)
            }
            atMap.clear()
            binding.etMessage.text.clearSpans()
            binding.etMessage.text.clear()
            binding.etMessage.text = null
        }
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
        val content = binding.etMessage.text
        val index: Int = binding.etMessage.selectionStart
        content.insert(index, text)
        this.renderInputText(content)
    }

    fun getEditText(): EmojiEditText {
        return binding.etMessage
    }

    fun deleteContent(count: Int) {
        val atSpans = binding.etMessage.text.getSpans<ForegroundColorSpan>()
        val selectionStart = binding.etMessage.selectionStart
        val selectionEnd = binding.etMessage.selectionEnd
        for (span in atSpans) {
            val spanStart = binding.etMessage.text.getSpanStart(span)
            val spanEnd = binding.etMessage.text.getSpanEnd(span)
            LLog.d("$spanStart $spanEnd $selectionStart $selectionEnd")
            if (selectionStart + 1 in spanStart..spanEnd || selectionEnd - 1 in spanStart..spanEnd) {
                binding.etMessage.text.removeSpan(span)
                binding.etMessage.text.delete(spanStart, spanEnd)
                return
            }
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

    private fun addAtMap(user: User, sessionMember: SessionMember?) {
        atMap["${user.id}"] = IMUIManager.nicknameForSessionMember(user, sessionMember)
    }

    private fun atNickname(id: Long): String? {
        return atMap["$id"]
    }

    fun addAtSessionMember(user: User, sessionMember: SessionMember?) {
        this.addAtMap(user, sessionMember)
        val nickname = this.atNickname(user.id) ?: return
        val selectionStart = binding.etMessage.selectionStart
        val content = binding.etMessage.text
        if (selectionStart > 0 && content[selectionStart - 1] == '@') {
            content.insert(selectionStart, "$nickname ")
        } else {
            content.insert(selectionStart, "@$nickname ")
        }
        this.renderInputText(content)
    }

    private fun renderInputText(content: Editable) {
        val regex = AtStringUtils.atRegex
        val sequence = regex.findAll(content)
        sequence.forEach { matchResult ->
            val range = matchResult.range
            val atSpan = ForegroundColorSpan(Color.parseColor("#1390f4"))
            binding.etMessage.text.setSpan(
                atSpan,
                range.first - 1,
                range.last + 1,
                Spanned.SPAN_EXCLUSIVE_INCLUSIVE
            )
        }
    }

    fun showReplyMessage(message: Message) {
        replyMsg = message
        resetReplyLayout()
    }

    fun clearReplyMessage() {
        replyMsg = null
        resetReplyLayout()
    }

    fun getReplyMessage(): Message? {
        return replyMsg
    }

    private fun resetReplyLayout() {
        if (replyMsg == null) {
            binding.layoutReply.visibility = GONE
            return
        }
        binding.layoutReply.visibility = VISIBLE

        val info = msgSender?.syncGetSessionMemberInfo(replyMsg!!.fUid)
        if (info != null) {
            binding.tvReplyUserNick.text = IMUIManager.nicknameForSessionMember(info.first, info.second)
        }

        val subscriber = object : BaseSubscriber<String>() {
            override fun onNext(t: String?) {
                t?.let {
                    binding.tvReplyContent.text = it
                }
            }

            override fun onComplete() {
                super.onComplete()
                disposables.remove(this)
            }
        }
        Flowable.just(replyMsg)
            .flatMap { msg ->
                val content = IMCoreManager.messageModule.getMsgProcessor(msg.type).getSessionDesc(msg)
                return@flatMap Flowable.just(content)
            }
            .compose(RxTransform.flowableToMain())
            .subscribe(subscriber)
        disposables.add(subscriber)
    }

    fun setReeditMessage(message: Message) {
        this.reeditMsg = message
        if (message.content != null) {
            var content = message.content!!
            if (message.atUsers != null) {
                content = AtStringUtils.replaceAtUIdsToNickname(content, message.getAtUIds()) { id ->
                    val member = msgSender?.syncGetSessionMemberInfo(id)
                    if (member != null) {
                        addAtMap(member.first, member.second)
                        return@replaceAtUIdsToNickname atNickname(id) ?: "$id"
                    }
                    return@replaceAtUIdsToNickname "$id"
                }
            }
            addInputContent(content)

            msgSender?.let { sender ->
                if (!sender.isKeyboardShowing()) {
                    sender.openKeyboard()
                }
            }
        }

    }

}