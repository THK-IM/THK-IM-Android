package com.thk.im.android.ui.fragment

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Color
import android.graphics.PointF
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.emoji2.widget.EmojiEditText
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.google.gson.Gson
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.enums.PopupAnimation
import com.thk.im.android.core.IMCoreManager
import com.thk.im.android.core.IMEvent
import com.thk.im.android.core.IMFileFormat
import com.thk.im.android.core.IMSendMsgCallback
import com.thk.im.android.core.MsgOperateStatus
import com.thk.im.android.core.MsgType
import com.thk.im.android.core.SessionType
import com.thk.im.android.core.SignalStatus
import com.thk.im.android.core.base.BaseSubscriber
import com.thk.im.android.core.base.LLog
import com.thk.im.android.core.base.RxTransform
import com.thk.im.android.core.base.extension.dp2px
import com.thk.im.android.core.base.extension.setShape
import com.thk.im.android.core.base.popup.KeyboardPopupWindow
import com.thk.im.android.core.base.utils.AppUtils
import com.thk.im.android.core.base.utils.ToastUtils
import com.thk.im.android.core.db.entity.Message
import com.thk.im.android.core.db.entity.Session
import com.thk.im.android.core.db.entity.SessionMember
import com.thk.im.android.core.db.entity.User
import com.thk.im.android.core.event.XEventBus
import com.thk.im.android.core.exception.CodeMsgException
import com.thk.im.android.ui.R
import com.thk.im.android.ui.databinding.FragmentMessageBinding
import com.thk.im.android.ui.fragment.popup.IMAtSessionMemberPopup
import com.thk.im.android.ui.fragment.popup.IMMessageOperatorPopup
import com.thk.im.android.ui.fragment.popup.IMSessionChoosePopup
import com.thk.im.android.ui.manager.IMAudioMsgData
import com.thk.im.android.ui.manager.IMChatFunction
import com.thk.im.android.ui.manager.IMFile
import com.thk.im.android.ui.manager.IMImageMsgData
import com.thk.im.android.ui.manager.IMUIManager
import com.thk.im.android.ui.manager.IMVideoMsgData
import com.thk.im.android.ui.protocol.AudioCallback
import com.thk.im.android.ui.protocol.AudioStatus
import com.thk.im.android.ui.protocol.IMContentResult
import com.thk.im.android.ui.protocol.internal.IMMsgPreviewer
import com.thk.im.android.ui.protocol.internal.IMMsgSender
import com.thk.im.android.ui.protocol.internal.IMSessionMemberAtDelegate
import com.thk.im.android.ui.utils.ScreenUtils
import io.reactivex.Flowable
import io.reactivex.disposables.CompositeDisposable
import java.util.Locale

open class IMMessageFragment : Fragment(), IMMsgPreviewer, IMMsgSender, IMSessionMemberAtDelegate {
    private lateinit var keyboardPopupWindow: KeyboardPopupWindow
    private lateinit var binding: FragmentMessageBinding
    private var keyboardShowing = false
    private var session: Session? = null
    private val disposables = CompositeDisposable()
    private val memberMap = mutableMapOf<Long, Pair<User, SessionMember?>>()
    private val atMessages = mutableListOf<Message>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentMessageBinding.inflate(
            inflater, container, false
        )
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        keyboardPopupWindow.dismiss()
        disposables.clear()
        saveDraft()
    }

    open fun saveDraft() {
        val session = session ?: return
        val content = binding.llInputLayout.getEditText().text.toString()
        Thread {
            kotlin.run {
                if (content != session.draft) {
                    if (session.draft != null || content.isNotEmpty()) {
                        IMCoreManager.db.sessionDao().updateDraft(session.id, content)
                        val updatedSession =
                            IMCoreManager.db.sessionDao().findById(session.id) ?: return@run
                        XEventBus.post(IMEvent.SessionUpdate.value, updatedSession)
                    }
                }
            }
        }.start()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)
        session = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getParcelable("session", Session::class.java) as Session
        } else {
            arguments?.getParcelable("session")!!
        }
        IMUIManager.uiResourceProvider?.inputBgColor()?.let { color ->
            binding.rcvMessage.setBackgroundColor(color)
        }
        IMUIManager.uiResourceProvider?.inputLayoutBgColor()?.let { color ->
            binding.root.setBackgroundColor(color)
        }
        binding.rcvMessage.init(this, session!!, this, this)
        binding.llInputLayout.init(this, session!!, this, this)
        binding.llBottomLayout.init(this, session!!, this, this)
        initKeyboardWindow()
        initEventBus()
        fetchSessionMembers()
        initMsgTipsView()

        session?.draft?.let { draft ->
            binding.llInputLayout.postDelayed(
                {
                    binding.llInputLayout.addInputContent(draft)
                }, 200
            )
        }

    }

    private fun initMsgTipsView() {
        val bgColor =
            IMUIManager.uiResourceProvider?.inputLayoutBgColor() ?: Color.parseColor("#DDDDDD")
        val textColor = IMUIManager.uiResourceProvider?.tintColor() ?: Color.parseColor("#1390f4")

        binding.tvUnreadTip.setShape(bgColor, floatArrayOf(6f, 6f, 6f, 6f), false)
        binding.tvUnreadTip.setTextColor(textColor)
        binding.tvUnreadTip.setOnClickListener {
            binding.tvUnreadTip.visibility = View.GONE
            binding.rcvMessage.scrollToUnReadMsg()
        }
        val unreadCount = session?.unReadCount ?: 0
        if (unreadCount > 0) {
            binding.tvUnreadTip.text = String.format(
                Locale.getDefault(),
                getString(R.string.x_message_unread),
                unreadCount
            )
            binding.tvUnreadTip.visibility = View.VISIBLE
        } else {
            binding.tvUnreadTip.visibility = View.GONE
        }

        binding.tvNewMsgTip.setShape(bgColor, floatArrayOf(6f, 6f, 6f, 6f), false)
        binding.tvNewMsgTip.setTextColor(textColor)
        binding.tvNewMsgTip.text = getString(R.string.new_message_tips)
        binding.tvNewMsgTip.setOnClickListener {
            binding.rcvMessage.scrollToLatestMsg(true)
        }

        binding.tvAtMsgTip.setShape(bgColor, floatArrayOf(6f, 6f, 6f, 6f), false)
        binding.tvAtMsgTip.setTextColor(textColor)
        binding.tvAtMsgTip.setOnClickListener {
            onAtTipsViewClick()
        }

        val subscriber = object : BaseSubscriber<List<Message>>() {
            override fun onNext(t: List<Message>?) {
                t?.let {
                    atMessages.addAll(it)
                    updateAtTipsView()
                }
            }

        }
        Flowable.just(session).flatMap {
            val unReadAtMeMessages =
                IMCoreManager.getImDataBase().messageDao().findSessionAtMeUnreadMessages(it.id)
            Flowable.just(unReadAtMeMessages)
        }.compose(RxTransform.flowableToMain()).subscribe(subscriber)
    }

    private fun updateUnreadMsgTips() {
        val unreadCount = session?.unReadCount ?: 0
        // 如果已经被隐藏 就不在刷新未读消息提示
        if (unreadCount > 0) {
            if (binding.tvUnreadTip.visibility == View.VISIBLE) {
                binding.tvUnreadTip.text = String.format(
                    Locale.getDefault(),
                    getString(R.string.x_message_unread),
                    unreadCount
                )
            }
        } else {
            binding.tvUnreadTip.visibility = View.GONE
        }
    }

    private fun updateAtTipsView() {
        if (atMessages.size <= 0) {
            binding.tvAtMsgTip.visibility = View.GONE
        } else {
            binding.tvAtMsgTip.text =
                String.format(getString(R.string.x_message_at_me), atMessages.size)
            binding.tvAtMsgTip.visibility = View.VISIBLE
        }
    }

    private fun onAtTipsViewClick() {
        if (atMessages.size > 0) {
            val msg = atMessages.firstOrNull()
            msg?.let {
                atMessages.removeAll { atMsg ->
                    atMsg.msgId == it.msgId
                }
                updateAtTipsView()
                binding.rcvMessage.scrollToMsg(it)
            }
        }
    }

    private fun fetchSessionMembers() {
        if (session == null) {
            return
        }
        val subscriber = object : BaseSubscriber<Map<Long, Pair<User, SessionMember?>>>() {
            override fun onNext(t: Map<Long, Pair<User, SessionMember?>>?) {
                t?.let {
                    updateSessionMember(it)
                }
                disposables.remove(this)
                binding.rcvMessage.loadMessages()
            }

            override fun onError(t: Throwable?) {
                super.onError(t)
                disposables.remove(this)
                binding.rcvMessage.loadMessages()
            }
        }
        val online = IMCoreManager.signalModule.signalStatus == SignalStatus.Connected
        IMCoreManager.messageModule.querySessionMembers(session!!.id, online)
            .flatMap { members ->
                val uIds = mutableSetOf<Long>()
                for (m in members) {
                    uIds.add(m.userId)
                }
                return@flatMap IMCoreManager.userModule.queryUsers(uIds).flatMap { userMap ->
                    val memberMap = mutableMapOf<Long, Pair<User, SessionMember?>>()
                    for ((k, v) in userMap) {
                        for (m in members) {
                            if (m.userId == k) {
                                val pair = Pair(v, m)
                                memberMap[k] = pair
                                break
                            }
                        }
                    }
                    Flowable.just(memberMap)
                }
            }.compose(RxTransform.flowableToMain())
            .subscribe(subscriber)
        disposables.add(subscriber)
    }

    private fun updateSessionMember(it: Map<Long, Pair<User, SessionMember?>>) {
        for ((k, v) in it) {
            memberMap[k] = v
        }
        binding.rcvMessage.refreshMessageUserInfo()
    }

    private fun initKeyboardWindow() {
        keyboardPopupWindow = KeyboardPopupWindow(binding.root) {
            if (it > 0) {
                keyboardShowing = true
                val isMultiWindow = ScreenUtils.isMultiWindowMode(requireActivity())
                if (isMultiWindow) {
                    moveLayout(true, 0, 300)
                } else {
                    moveLayout(true, it, 150)
                }
            } else {
                keyboardShowing = false
                val height = binding.llBottomLayout.getContentHeight()
                moveLayout(false, height, 150)
            }
        }
    }

    private fun moveLayout(isKeyboardShow: Boolean, bottomHeight: Int, duration: Long) {
        val animators = AnimatorSet()
        val msgAnimator = ValueAnimator.ofInt(binding.rcvMessage.paddingTop, bottomHeight)
        msgAnimator.addUpdateListener {
            val animatedValue = it.animatedValue as Int
            binding.rcvMessage.setPadding(0, animatedValue, 0, 0)
        }
        msgAnimator.setTarget(binding.rcvMessage)
        msgAnimator.duration = 150
        val animator = ObjectAnimator.ofFloat(
            binding.llAlwaysShow, "translationY", 0 - bottomHeight.toFloat()
        )
        animator.duration = duration
        val lp = binding.llBottomLayout.layoutParams
        lp.height = bottomHeight
        binding.llBottomLayout.layoutParams = lp
        val bottomAnimator = ObjectAnimator.ofFloat(
            binding.llBottomLayout, "translationY", 0 - bottomHeight.toFloat()
        )
        bottomAnimator.duration = duration
        animators.play(msgAnimator).with(animator).with(bottomAnimator)
        animators.start()

        animator.addUpdateListener {
            binding.llInputLayout.onKeyboardChange(isKeyboardShow, bottomHeight, duration)
            binding.llBottomLayout.onKeyboardChange(isKeyboardShow, bottomHeight, duration)
        }
    }

    private fun selectImage() {
        activity?.let {
            IMUIManager.mediaProvider?.pick(it,
                listOf(IMFileFormat.Image, IMFileFormat.Video),
                object : IMContentResult {
                    override fun onResult(result: List<IMFile>) {
                        onMediaResult(result)
                    }

                    override fun onCancel() {
                    }

                })
        }
    }


    private fun cameraMedia() {
        activity?.let {
            IMUIManager.mediaProvider?.openCamera(it,
                listOf(IMFileFormat.Image, IMFileFormat.Video),
                object : IMContentResult {
                    override fun onResult(result: List<IMFile>) {
                        onMediaResult(result)
                    }

                    override fun onCancel() {
                    }

                })
        }
    }

    private fun onMediaResult(result: List<IMFile>) {
        try {
            for (media in result) {
                if (media.mimeType.startsWith("video", true)) {
                    if (IMUIManager.uiResourceProvider?.supportFunction(
                            session!!,
                            IMChatFunction.Video.value
                        ) == false
                    ) {
                        showMessage(getString(R.string.do_not_allow_send_video), false)
                        return
                    }
                    val videoMsgData = IMVideoMsgData()
                    videoMsgData.path = media.path
                    sendMessage(MsgType.Video.value, null, videoMsgData)
                } else if (media.mimeType.startsWith("image", true)) {
                    if (IMUIManager.uiResourceProvider?.supportFunction(
                            session!!,
                            IMChatFunction.Image.value
                        ) == false
                    ) {
                        showMessage(getString(R.string.do_not_allow_send_image), false)
                        return
                    }
                    val imageMsgData = IMImageMsgData()
                    imageMsgData.path = media.path
                    sendMessage(MsgType.Image.value, null, imageMsgData)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    private fun initEventBus() {
        XEventBus.observe(this, IMEvent.BatchMsgNew.value, Observer<Pair<Long, List<Message>>> {
            session?.let { s ->
                if (it.first == s.id) {
                    binding.rcvMessage.insertMessages(it.second)
                }
            }

        })
        XEventBus.observe(this, IMEvent.MsgNew.value, Observer<Message> {
            if (it.sid == session!!.id) {
                if (it.oprStatus.and(MsgOperateStatus.ClientRead.value) == 0 && it.isAtMe()) {
                    var contained = false
                    atMessages.forEach { msg ->
                        if (msg.msgId == it.msgId) {
                            contained = true
                        }
                    }
                    if (!contained) {
                        atMessages.add(it)
                        updateAtTipsView()
                    }
                }
                binding.rcvMessage.insertMessage(it)
            }
        })
        XEventBus.observe(this, IMEvent.MsgUpdate.value, Observer<Message> {
            if (it.sid == session!!.id) {
                if (it.oprStatus.and(MsgOperateStatus.ClientRead.value) == 0 && it.isAtMe()) {
                    var contained = false
                    atMessages.forEach { msg ->
                        if (msg.msgId == it.msgId) {
                            contained = true
                        }
                    }
                    if (!contained) {
                        atMessages.add(it)
                        updateAtTipsView()
                    }
                }
                binding.rcvMessage.updateMessage(it)
            }
        })
        XEventBus.observe(this, IMEvent.MsgDelete.value, Observer<Message> {
            if (it.sid == session!!.id) {
                atMessages.removeAll { atMsg ->
                    atMsg.msgId == it.msgId
                }
                updateAtTipsView()
                binding.rcvMessage.deleteMessage(it)
            }
        })
        XEventBus.observe(this, IMEvent.BatchMsgDelete.value, Observer<List<Message>> { messages ->
            val deleteMessages = mutableListOf<Message>()
            messages.forEach {
                if (it.sid == session!!.id) {
                    deleteMessages.add(it)
                }
            }
            binding.rcvMessage.deleteMessages(deleteMessages)
        })

        XEventBus.observe(this, IMEvent.SessionMessageClear.value, Observer<Session> {
            session?.let { session ->
                if (it.id == session.id) {
                    binding.rcvMessage.clearMessages()
                }
            }
        })

        XEventBus.observe(this, IMEvent.SessionUpdate.value, Observer<Session> {
            updateSession(it)
        })

        XEventBus.observe(this, IMEvent.SessionNew.value, Observer<Session> {
            updateSession(it)
        })
    }

    private fun updateSession(s: Session) {
        if (session?.id == s.id) {
            session?.merge(s)
            session?.unReadCount = s.unReadCount
            updateUnreadMsgTips()
        }
    }

    override fun previewMessage(msg: Message, position: Int, originView: View) {
        val interceptor = IMUIManager.getMsgIVProviderByMsgType(msg.type).onMsgBodyClick(
            requireContext(), msg, session, originView
        )
        if (!interceptor) {
            onMsgClick(msg, position, originView)
        }
    }

    open fun onMsgClick(msg: Message, position: Int, originView: View) {
        when (msg.type) {
            MsgType.Audio.value -> {
                msg.data?.let {
                    val data = Gson().fromJson(it, IMAudioMsgData::class.java)
                    data.path?.let { path ->
                        val currentPath = IMUIManager.mediaProvider?.currentPlayingPath()
                        if (path == currentPath) {
                            IMUIManager.mediaProvider?.stopPlayAudio()
                            return
                        }
                        val success =
                            IMUIManager.mediaProvider?.startPlayAudio(path, object : AudioCallback {
                                override fun audioData(
                                    path: String, second: Int, db: Double, state: AudioStatus
                                ) {

                                }
                            })
                        if (success == true) {
                            if (msg.oprStatus.and(MsgOperateStatus.ClientRead.value) == 0) {
                                readMessage(msg)
                            }
                        } else {
                            showToast(getString(R.string.play_failed))
                        }
                    }
                }
            }

            MsgType.Image.value, MsgType.Video.value -> {
                previewImageAndVideo(msg, position, originView)
            }

            MsgType.Record.value -> {
                previewRecord(msg)
            }
        }
    }

    override fun setSelectMode(selected: Boolean, message: Message?) {
        binding.llInputLayout.setSelectMode(selected)
        binding.rcvMessage.setSelectMode(selected, message)
    }

    override fun deleteSelectedMessages() {
        val messages = binding.rcvMessage.getSelectMessages()
        session?.let {
            IMCoreManager.messageModule.deleteMessages(it.id, messages.toList(), true)
                .compose(RxTransform.flowableToMain()).subscribe(object : BaseSubscriber<Void>() {

                    override fun onNext(t: Void?) {}

                    override fun onError(t: Throwable?) {
                        t?.message?.let { error ->
                            LLog.e(error)
                        }
                    }
                })
        }
    }

    override fun readMessage(message: Message) {
        // 对于不是自己发的消息才能发送已读消息
        if (message.fUid != IMCoreManager.uId && message.msgId > 0) {
            IMCoreManager.messageModule
                .sendMessage(
                    message.sid, MsgType.Read.value,
                    null, null, null, message.msgId
                )
        }
    }

    override fun popupMessageOperatorPanel(view: View, message: Message) {
        if (isKeyboardShowing()) {
            closeKeyboard()
        }
        if (this.session == null) {
            return
        }
        val locations = IntArray(2)
        view.getLocationOnScreen(locations)
        context?.let {
            val point = PointF()
            var popupWidth = 20.dp2px()
            val operators = IMUIManager.getMsgOperators(message, this.session!!)
            val rowCount = 5
            popupWidth += if (operators.size < 5) {
                operators.size * (60.dp2px())
            } else {
                300.dp2px()
            }
            var popupHeight = ((operators.size / rowCount) * 60 + 30).dp2px()
            if (operators.size % rowCount > 0) {
                popupHeight += 60.dp2px()
            }
            point.x =
                (AppUtils.instance().screenWidth / 2).toFloat()
            if (locations[1] <= 300.dp2px() && (locations[1] + view.height) >= (AppUtils.instance().screenHeight - 300.dp2px())) {
                point.y =
                    ((AppUtils.instance().screenHeight - popupHeight) / 2).toFloat()
            } else if (locations[1] > (300.dp2px())) {
                point.y = (locations[1] - popupHeight).toFloat()
            } else {
                point.y = (locations[1] + view.height).toFloat()
            }
            val popupView = IMMessageOperatorPopup(it)
            popupView.message = message
            popupView.sender = this
            popupView.operators = operators
            XPopup.Builder(context).popupAnimation(PopupAnimation.ScaleAlphaFromCenter)
                .shadowBgColor(Color.TRANSPARENT).hasShadowBg(false).isViewMode(true)
                .isCenterHorizontal(true).isDestroyOnDismiss(true).popupWidth(popupWidth)
                .popupHeight(popupHeight).hasBlurBg(false).atPoint(point)
                .asCustom(popupView).show()
        }
    }

    override fun showLoading(text: String) {
    }

    override fun dismissLoading() {

    }

    override fun showToast(text: String) {
        ToastUtils.show(text)
    }

    override fun showError(throwable: Throwable) {
        throwable.message?.let {
            showToast(it)
        }
    }

    override fun showMessage(text: String, success: Boolean) {
        dismissLoading()
        activity?.let {
            it.runOnUiThread {
                ToastUtils.show(text)
            }
        }
    }

    override fun forwardMessageToSession(messages: List<Message>, forwardType: Int) {
        context?.let { ctx ->
            session?.let {
                val popup = IMSessionChoosePopup(ctx)
                popup.session = it
                popup.messages = messages
                popup.sender = this
                popup.forwardType = forwardType
                XPopup.Builder(ctx).isDestroyOnDismiss(true)
                    .isLightStatusBar(false)
                    .hasShadowBg(false)
                    .isViewMode(true)
                    .moveUpToKeyboard(false)
                    .enableDrag(false)
                    .asCustom(popup)
                    .show()
            }
        }
    }

    override fun forwardSelectedMessages(forwardType: Int) {
        val messages = binding.rcvMessage.getSelectMessages().toList()
        context?.let { ctx ->
            session?.let {
                val popup = IMSessionChoosePopup(ctx)
                popup.session = it
                popup.messages = messages
                popup.sender = this
                popup.forwardType = forwardType
                XPopup.Builder(ctx).isDestroyOnDismiss(true)
                    .isLightStatusBar(false)
                    .hasShadowBg(false)
                    .isViewMode(true)
                    .moveUpToKeyboard(false)
                    .enableDrag(false)
                    .asCustom(popup)
                    .show()
            }
        }
    }

    private fun previewImageAndVideo(msg: Message, position: Int, originView: View) {
        val messages = binding.rcvMessage.getMessages()
        val mediaMessages = ArrayList<Message>()
        var count = 0
        val rightMessages = mutableListOf<Message>()
        for (i in position until messages.size) {
            if (messages[i].type == MsgType.Image.value || messages[i].type == MsgType.Video.value) {
                rightMessages.add(messages[i])
                count++
            }
            if (count == 5) {
                break
            }
        }

        mediaMessages.addAll(rightMessages.reversed())
        count = 0
        for (i in 0 until position) {
            if (messages[position - 1 - i].type == MsgType.Image.value || messages[position - 1 - i].type == MsgType.Video.value) {
                mediaMessages.add(messages[position - 1 - i])
                count++
            }
            if (count == 5) {
                break
            }
        }
        activity?.let {
            IMUIManager.mediaPreviewer?.previewMediaMessage(
                it, mediaMessages, originView, msg.msgId
            )
        }
    }

    private fun previewRecord(msg: Message) {
        activity?.let {
            session?.let { session ->
                IMUIManager.mediaPreviewer?.previewRecordMessage(it, session, msg)
            }
        }
    }

    override fun showNewMsgTipsView(isHidden: Boolean) {
        binding.tvNewMsgTip.visibility = if (isHidden) View.GONE else View.VISIBLE
    }

    override fun context(): Context {
        return requireContext()
    }

    override fun getSession(): Session {
        return session!!
    }

    override fun resendMessage(msg: Message) {
        val callback = object : IMSendMsgCallback {
            override fun onResult(message: Message, e: Throwable?) {
                e?.let {
                    showError(it)
                }
            }
        }
        IMCoreManager.messageModule.resend(msg, callback)
    }

    override fun sendMessage(type: Int, body: Any?, data: Any?, atUser: String?) {
        val callback = object : IMSendMsgCallback {
            override fun onResult(message: Message, e: Throwable?) {
                e?.let {
                    showError(it)
                }
            }
        }
        val referMsg = binding.llInputLayout.getReplyMessage()
        if (referMsg != null) {
            binding.llInputLayout.clearReplyMessage()
        }
        IMCoreManager.messageModule.sendMessage(
            session!!.id,
            type,
            body,
            data,
            atUser,
            referMsg?.msgId,
            callback
        )
    }

    override fun addInputContent(text: String) {
        binding.llInputLayout.addInputContent(text)
    }

    override fun getEditText(): EmojiEditText {
        return binding.llInputLayout.getEditText()
    }

    override fun deleteContent(count: Int) {
        binding.llInputLayout.deleteContent(count)
    }

    override fun choosePhoto() {
        context?.let {
            XXPermissions.with(it)
                .permission(Permission.READ_MEDIA_VIDEO, Permission.READ_MEDIA_IMAGES)
                .request { _, all ->
                    if (all) {
                        selectImage()
                    }
                }
        }
    }

    override fun openCamera() {
        context?.let {
            XXPermissions.with(it).permission(Permission.CAMERA).request { _, all ->
                if (all) {
                    cameraMedia()
                }
            }
        }
    }

    override fun moveToLatestMessage() {
        binding.rcvMessage.scrollToLatestMsg()
    }

    override fun showBottomPanel(position: Int) {
        binding.llBottomLayout.showBottomPanel(position)
    }

    override fun closeBottomPanel(): Boolean {
        return if (binding.llBottomLayout.getContentHeight() > 0) {
            binding.llBottomLayout.closeBottomPanel()
            true
        } else {
            false
        }
    }

    override fun moveUpAlwaysShowView(isKeyboardShow: Boolean, bottomHeight: Int, duration: Long) {
        moveLayout(isKeyboardShow, bottomHeight, duration)
    }

    override fun openKeyboard(): Boolean {
        return binding.llInputLayout.openKeyboard()
    }

    override fun isKeyboardShowing(): Boolean {
        return keyboardShowing
    }

    override fun closeKeyboard(): Boolean {
        return binding.llInputLayout.closeKeyboard()
    }

    override fun onSessionMemberAt(user: User, sessionMember: SessionMember?) {
        binding.llInputLayout.addAtSessionMember(user, sessionMember)
        binding.llInputLayout.openKeyboard()
    }

    override fun openAtPopupView() {
        context?.let {
            session?.let { session ->
                if (session.type != SessionType.SuperGroup.value &&
                    session.type != SessionType.Group.value
                ) {
                    return
                }
                closeKeyboard()
                val popup = IMAtSessionMemberPopup(it)
                popup.session = session
                popup.sessionMemberAtDelegate = this
                XPopup.Builder(it).isDestroyOnDismiss(true)
                    .hasShadowBg(false)
                    .isViewMode(true)
                    .maxHeight((AppUtils.instance().screenHeight * 0.6).toInt())
                    .moveUpToKeyboard(true)
                    .enableDrag(false)
                    .asCustom(popup)
                    .show()
            }
        }
    }

    override fun addAtUser(user: User, sessionMember: SessionMember?) {
        binding.llInputLayout.addAtSessionMember(user, sessionMember)
    }

    override fun replyMessage(msg: Message) {
        binding.llInputLayout.showReplyMessage(msg)
        if (!isKeyboardShowing()) {
            openKeyboard()
            binding.rcvMessage.scrollToLatestMsg()
        }
    }

    override fun closeReplyMessage() {
        binding.llInputLayout.clearReplyMessage()
    }

    override fun reeditMessage(message: Message) {
        binding.llInputLayout.setReeditMessage(message)
    }

    override fun syncGetSessionMemberInfo(userId: Long): Pair<User, SessionMember?>? {
        if (userId == -1L) {
            return Pair(User.all, null)
        }
        return memberMap[userId]
    }


    override fun syncGetSessionMemberUserIdByNickname(nick: String): Long? {
        for ((k, v) in this.memberMap) {
            if (v.second?.noteName == nick || v.first.nickname == nick) {
                return k
            }
        }
        return null
    }

    override fun saveSessionMemberInfo(info: Pair<User, SessionMember?>) {
        memberMap[info.first.id] = info
    }

    override fun asyncGetSessionMemberInfo(userId: Long): Flowable<Pair<User, SessionMember?>> {
        val session = this.session
        return IMCoreManager.userModule.queryUser(userId)
            .flatMap { user ->
                if (session != null && session.id > 0) {
                    val sessionMember =
                        IMCoreManager.db.sessionMemberDao().findSessionMember(session.id, userId)
                    return@flatMap Flowable.just(Pair(user, sessionMember))
                } else {
                    return@flatMap Flowable.just(Pair(user, null))
                }
            }
    }

}