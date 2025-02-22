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
import androidx.core.content.ContextCompat
import androidx.core.text.getSpans
import androidx.emoji2.widget.EmojiEditText
import androidx.lifecycle.LifecycleOwner
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.core.BasePopupView
import com.thk.im.android.core.IMCoreManager
import com.thk.im.android.core.IMFileFormat
import com.thk.im.android.core.MsgType
import com.thk.im.android.core.SessionRole
import com.thk.im.android.core.base.LLog
import com.thk.im.android.core.base.extension.setShape
import com.thk.im.android.core.db.entity.Message
import com.thk.im.android.core.db.entity.Session
import com.thk.im.android.core.db.entity.SessionMember
import com.thk.im.android.core.db.entity.User
import com.thk.im.android.ui.R
import com.thk.im.android.ui.databinding.LayoutMessageInputBinding
import com.thk.im.android.ui.fragment.popup.IMVoiceDbPopup
import com.thk.im.android.ui.manager.IMAudioMsgData
import com.thk.im.android.ui.manager.IMChatFunction
import com.thk.im.android.ui.manager.IMReeditMsgData
import com.thk.im.android.ui.manager.IMUIManager
import com.thk.im.android.ui.protocol.AudioCallback
import com.thk.im.android.ui.protocol.AudioStatus
import com.thk.im.android.ui.protocol.internal.IMMsgPreviewer
import com.thk.im.android.ui.protocol.internal.IMMsgSender
import com.thk.im.android.ui.utils.AtStringUtils
import io.reactivex.disposables.CompositeDisposable
import java.io.File
import kotlin.math.abs

@SuppressLint("ClickableViewAccessibility")
class IMInputLayout : ConstraintLayout {

    private val binding: LayoutMessageInputBinding
    private val disposables = CompositeDisposable()
    private val recordPopup: IMVoiceDbPopup = IMVoiceDbPopup(context)
    private val recordPopupView: BasePopupView =
        XPopup.Builder(context).isViewMode(true).isDestroyOnDismiss(false).hasShadowBg(false)
            .asCustom(recordPopup)

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
        binding.tvSendMsg.setOnClickListener {
            sendInputContent()
        }
        val bgLayoutColor =
            IMUIManager.uiResourceProvider?.panelBgColor() ?: Color.parseColor("#FFFFFF")
        val inputTextColor =
            IMUIManager.uiResourceProvider?.inputTextColor() ?: Color.parseColor("#333333")
        val tipsTextColor =
            IMUIManager.uiResourceProvider?.tipTextColor() ?: Color.parseColor("#666666")
        val tintColor = IMUIManager.uiResourceProvider?.tintColor() ?: Color.BLUE
        val inputColor =
            IMUIManager.uiResourceProvider?.inputBgColor() ?: Color.parseColor("#EEEEEE")

        binding.root.setBackgroundColor(bgLayoutColor)
        binding.etMessage.setTextColor(inputTextColor)
        binding.etMessage.setHintTextColor(tipsTextColor)

        ContextCompat.getDrawable(context, R.drawable.ic_msg_emoji)?.let {
            it.setTint(inputTextColor)
            binding.ivEmo.setImageDrawable(it)
        }
        ContextCompat.getDrawable(context, R.drawable.ic_msg_voice)?.let {
            it.setTint(inputTextColor)
            binding.ivAudioRecord.setImageDrawable(it)
        }
        ContextCompat.getDrawable(context, R.drawable.ic_msg_more)?.let {
            it.setTint(inputTextColor)
            binding.ivAddMore.setImageDrawable(it)
        }
        ContextCompat.getDrawable(context, R.drawable.ic_reply_close)?.let {
            it.setTint(inputTextColor)
            binding.ivReplyClose.setImageDrawable(it)
        }
        ContextCompat.getDrawable(context, R.drawable.ic_msg_opr_delete)?.let {
            it.setTint(inputTextColor)
            binding.ivMsgOprDelete.setImageDrawable(it)
        }
        ContextCompat.getDrawable(context, R.drawable.ic_msg_opr_forward)?.let {
            it.setTint(inputTextColor)
            binding.ivMsgOprForward.setImageDrawable(it)
        }
        ContextCompat.getDrawable(context, R.drawable.ic_msg_opr_cancel)?.let {
            it.setTint(inputTextColor)
            binding.ivMsgOprCancel.setImageDrawable(it)
        }
        binding.tvSendMsg.setShape(tintColor, floatArrayOf(15f, 15f, 15f, 15f), false)
        binding.tvSendMsg.setTextColor(Color.WHITE)
        binding.vReplyLine.setShape(
            tintColor, floatArrayOf(2f, 2f, 2f, 2f), false
        )
        binding.tvReplyUserNick.setTextColor(inputTextColor)
        binding.tvReplyContent.setTextColor(tipsTextColor)
        binding.etMessage.setShape(
            inputColor, floatArrayOf(20f, 20f, 20f, 20f), false
        )
        binding.btRecordVoice.setShape(
            inputColor, floatArrayOf(20f, 20f, 20f, 20f), false
        )
        binding.viewMuted.setBackgroundColor(inputColor)

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
                        val tips = if (audioCancel) {
                            context.getString(R.string.release_to_cancel)
                        } else {
                            context.getString(R.string.release_to_send_voice)
                        }
                        binding.btRecordVoice.text = tips
                        if (recordPopupView.isShow) {
                            recordPopup.setTips(tips)
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


        binding.ivReplyClose.setOnClickListener {
            clearReplyMessage()
            disposables.clear()
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        disposables.clear()
        recordPopup.destroy()
        recordPopupView.destroy()
    }

    fun init(
        lifecycleOwner: LifecycleOwner,
        session: Session,
        sender: IMMsgSender?,
        previewer: IMMsgPreviewer?,
    ) {
        this.lifecycleOwner = lifecycleOwner
        this.session = session
        this.msgSender = sender
        this.msgPreviewer = previewer
        resetVisible()
    }

    private fun resetVisible() {
        if (IMUIManager.uiResourceProvider?.supportFunction(
                session,
                IMChatFunction.BaseInput.value
            ) == false
        ) {
            binding.llMessageInput.visibility = GONE
        } else {
            binding.llMessageInput.visibility = VISIBLE
        }
        if (IMUIManager.uiResourceProvider?.supportFunction(
                session,
                IMChatFunction.Audio.value
            ) == false
        ) {
            binding.ivAudioRecord.visibility = GONE
        } else {
            binding.ivAudioRecord.visibility = VISIBLE
        }
        if (IMUIManager.getFunctionProvider(session).isEmpty()) {
            binding.ivAddMore.visibility = GONE
        } else {
            binding.ivAddMore.visibility = VISIBLE
        }
        if (IMUIManager.uiResourceProvider?.supportFunction(
                session,
                IMChatFunction.Forward.value
            ) == false
        ) {
            binding.ivMsgOprForward.visibility = GONE
        } else {
            binding.ivMsgOprForward.visibility = VISIBLE
        }

        val muted = session.mute > 0 && session.role == SessionRole.Member.value
        if (muted) {
            binding.viewMuted.visibility = View.VISIBLE
        } else {
            binding.viewMuted.visibility = View.GONE
        }
    }

    private fun sendInputContent() {
        binding.etMessage.text?.let {
            val text = it.toString()
            val (_, atUIds) = AtStringUtils.replaceAtNickNamesToUIds(text) { nickname ->
                for ((k, v) in atMap) {
                    if (v.trim() == nickname) {
                        return@replaceAtNickNamesToUIds k.toLongOrNull() ?: 0L
                    }
                }
                val id = msgSender?.syncGetSessionMemberUserIdByNickname(nickname.trim())
                return@replaceAtNickNamesToUIds id ?: 0L
            }
            if (reeditMsg != null) {
                val msg = IMReeditMsgData(reeditMsg!!.sid, reeditMsg!!.msgId, text)
                msgSender?.sendMessage(MsgType.Reedit.value, msg, null, null)
                reeditMsg = null
            } else {
                msgSender?.sendMessage(MsgType.Text.value, text, null, atUIds)
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
                } else if (index == 1) {
                    binding.etMessage.text.delete(0, index)
                }
            }
        }
    }

    private fun checkAudioPermission() {
        val granted = XXPermissions.isGranted(context, Permission.RECORD_AUDIO)
        if (!granted) {
            XXPermissions.with(context).permission(Permission.RECORD_AUDIO).request { _, all ->
                if (!all) {
                    msgSender?.showToast(context.getString(R.string.please_open_record_permission))
                }
            }
        } else {
            startRecordingAudio()
        }
    }

    private fun startRecordingAudio() {
        val contentProvider = IMUIManager.mediaProvider ?: return

        val inputBgColor =
            IMUIManager.uiResourceProvider?.inputBgColor() ?: Color.parseColor("#EEEEEE")
        binding.btRecordVoice.setShape(
            inputBgColor, floatArrayOf(20f, 20f, 20f, 20f), false
        )
        if (!contentProvider.isRecordingAudio()) {
            binding.btRecordVoice.text = context.getString(R.string.release_to_send_voice)
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
        if (recordPopupView.isShow) {
            recordPopup.setDb(db)
        }
        when (state) {
            AudioStatus.Ing, AudioStatus.Waiting -> {
                if (!recordPopupView.isShow) {
                    recordPopupView.show()
                }
            }

            AudioStatus.Finished -> {
                if (recordPopupView.isShow) {
                    recordPopupView.dismiss()
                }
                if (!audioCancel) {
                    if (second == 0) {
                        msgSender?.showToast(context.getString(R.string.record_duration_too_short))
                        return
                    }
                    val audioMsgData = IMAudioMsgData()
                    audioMsgData.duration = second
                    audioMsgData.path = path
                    msgSender?.sendMessage(MsgType.Audio.value, null, audioMsgData)
                }
            }

            AudioStatus.Exited -> {
                if (recordPopupView.isShow) {
                    recordPopupView.dismiss()
                }
                val file = File(path)
                if (file.exists()) {
                    if (!file.delete()) {
                        return
                    }
                }
                msgSender?.showToast(context.getString(R.string.record_failed))
            }
        }
    }

    private fun stopAudioRecording() {
        if (recordPopupView.isShow) {
            recordPopupView.dismiss()
        }
        val inputLayoutBgColor =
            IMUIManager.uiResourceProvider?.inputBgColor() ?: Color.parseColor("#EEEEEE")
        binding.btRecordVoice.setShape(
            inputLayoutBgColor, floatArrayOf(20f, 20f, 20f, 20f), false
        )
        binding.btRecordVoice.text = context.getString(R.string.press_for_record_voice)
        val contentProvider = IMUIManager.mediaProvider ?: return
        contentProvider.stopRecordAudio()
    }

    fun setSelectMode(selected: Boolean) {
        if (selected) {
            binding.llMessageInput.visibility = View.GONE
            binding.llMessageOperator.visibility = View.VISIBLE
        } else {
            resetVisible()
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
        val highlightColor =
            IMUIManager.uiResourceProvider?.tintColor() ?: Color.parseColor("#1390f4")
        sequence.forEach { matchResult ->
            val range = matchResult.range
            val atSpan = ForegroundColorSpan(highlightColor)
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
            binding.tvReplyUserNick.text =
                IMUIManager.nicknameForSessionMember(info.first, info.second)
        }

        replyMsg?.let {
            binding.tvReplyContent.text =
                IMCoreManager.messageModule.getMsgProcessor(it.type).msgDesc(it)
        }
    }

    fun setReeditMessage(message: Message) {
        this.reeditMsg = message
        if (message.content != null) {
            var content = message.content!!
            if (message.atUsers != null) {
                content =
                    AtStringUtils.replaceAtUIdsToNickname(content, message.getAtUIds()) { id ->
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

    fun onSessionUpdate() {
        val muted = session.mute > 0 && session.role == SessionRole.Member.value
        if (muted) {
            stopAudioRecording()
            binding.etMessage.text.clearSpans()
            binding.etMessage.text.clear()
            binding.etMessage.text = null
            clearReplyMessage()
            closeKeyboard()
        }
        resetVisible()
    }

}