package com.thk.im.android.ui.fragment

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
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
import com.thk.im.android.core.MsgType
import com.thk.im.android.core.base.BaseSubscriber
import com.thk.im.android.core.base.LLog
import com.thk.im.android.core.base.RxTransform
import com.thk.im.android.core.base.extension.dp2px
import com.thk.im.android.core.base.popup.KeyboardPopupWindow
import com.thk.im.android.core.base.utils.ToastUtils
import com.thk.im.android.core.db.entity.Message
import com.thk.im.android.core.db.entity.Session
import com.thk.im.android.core.event.XEventBus
import com.thk.im.android.ui.databinding.FragmentMessageBinding
import com.thk.im.android.ui.fragment.popup.IMMessageOperatorPopup
import com.thk.im.android.ui.fragment.popup.IMSessionChoosePopup
import com.thk.im.android.ui.manager.IMAudioMsgData
import com.thk.im.android.ui.manager.IMFile
import com.thk.im.android.ui.manager.IMImageMsgData
import com.thk.im.android.ui.manager.IMUIManager
import com.thk.im.android.ui.manager.IMVideoMsgData
import com.thk.im.android.ui.protocol.AudioCallback
import com.thk.im.android.ui.protocol.AudioStatus
import com.thk.im.android.ui.protocol.IMContentResult
import com.thk.im.android.ui.protocol.internal.IMMsgPreviewer
import com.thk.im.android.ui.protocol.internal.IMMsgSender

class IMMessageFragment : Fragment(), IMMsgPreviewer, IMMsgSender {
    private lateinit var keyboardPopupWindow: KeyboardPopupWindow
    private var keyboardShowing = false
    private var session: Session? = null
    private lateinit var binding: FragmentMessageBinding

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
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)
        session = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getParcelable("session", Session::class.java) as Session
        } else {
            arguments?.getParcelable("session")!!
        }
        binding.rcvMessage.init(this, session!!, this, this)
        binding.llInputLayout.init(this, session!!, this, this)
        binding.llBottomLayout.init(this, session!!, this, this)
        initKeyboardWindow()
        initEventBus()
    }

    private fun initKeyboardWindow() {
        keyboardPopupWindow = KeyboardPopupWindow(binding.root) {
            if (it > 0) {
                keyboardShowing = true
                moveLayout(true, it, 150)
            } else {
                keyboardShowing = false
                val height = binding.llBottomLayout.getContentHeight()
                moveLayout(false, height, 150)
            }
        }
    }

    private fun moveLayout(isKeyboardShow: Boolean, bottomHeight: Int, duration: Long) {
        LLog.v("KeyboardWindow", "$isKeyboardShow, $bottomHeight, $duration")
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
                    val videoMsgData = IMVideoMsgData()
                    videoMsgData.path = media.path
                    sendMessage(MsgType.VIDEO.value, null, videoMsgData)
                } else if (media.mimeType.startsWith("image", true)) {
                    val imageMsgData = IMImageMsgData()
                    imageMsgData.path = media.path
                    sendMessage(MsgType.IMAGE.value, null, imageMsgData)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    private fun initEventBus() {
        XEventBus.observe(this, IMEvent.BatchMsgNew.value, Observer<List<Message>> {
            if (it.isNotEmpty()) {
                if (it[0].sid == session!!.id) {
                    binding.rcvMessage.insertMessages(it)
                }
            }
        })
        XEventBus.observe(this, IMEvent.MsgNew.value, Observer<Message> {
            it?.let {
                if (it.sid == session!!.id) {
                    binding.rcvMessage.insertMessage(it)
                }
            }
        })
        XEventBus.observe(this, IMEvent.MsgUpdate.value, Observer<Message> {
            it?.let {
                if (it.sid == session!!.id) {
                    binding.rcvMessage.updateMessage(it)
                }
            }
        })
        XEventBus.observe(this, IMEvent.MsgDelete.value, Observer<Message> {
            it?.let {
                if (it.sid == session!!.id) {
                    binding.rcvMessage.deleteMessage(it)
                }
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
    }

    override fun previewMessage(msg: Message, position: Int, originView: View) {
        LLog.v("previewMessage ${msg.type} $position, $msg")
        when (msg.type) {
            MsgType.Audio.value -> {
                msg.data?.let {
                    val data = Gson().fromJson(it, IMAudioMsgData::class.java)
                    data.path?.let { path ->
                        IMUIManager.mediaProvider?.startPlayAudio(path, object : AudioCallback {
                            override fun audioData(
                                path: String, second: Int, db: Double, state: AudioStatus
                            ) {
                                ToastUtils.show("play: $second, $db")
                            }
                        })
                    }
                }
            }

            MsgType.IMAGE.value, MsgType.VIDEO.value -> {
                previewImageAndVideo(msg, position, originView)
            }

            MsgType.RECORD.value -> {
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
                    override fun onComplete() {
                        super.onComplete()
                    }

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
                .sendMessage(message.sid, MsgType.READ.value,
                    null, null, null, message.msgId)
        }
    }

    override fun popupMessageOperatorPanel(view: View, message: Message) {
        if (isKeyboardShowing()) {
            closeKeyboard()
        }
        val locations = IntArray(2)
        view.getLocationOnScreen(locations)
        context?.let {
            val point = PointF()
            val popupWidth = 320.dp2px()
            val operators = IMUIManager.getMsgOperators(message)
            val rowCount = 5
            val popupHeight =
                ((operators.size / rowCount + operators.size % rowCount) * 60 + 30).dp2px()
            point.x =
                (com.thk.im.android.core.base.utils.AppUtils.instance().screenWidth / 2).toFloat()
            if (locations[1] <= 300.dp2px() && (locations[1] + view.height) >= (com.thk.im.android.core.base.utils.AppUtils.instance().screenHeight - 300.dp2px())) {
                point.y =
                    ((com.thk.im.android.core.base.utils.AppUtils.instance().screenHeight - popupHeight) / 2).toFloat()
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
                .popupHeight(popupHeight).hasBlurBg(false).atPoint(point).asCustom(popupView).show()
        }
    }

    override fun showLoading(text: String) {
    }

    override fun dismissLoading() {

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
                XPopup.Builder(ctx).isDestroyOnDismiss(true)
                    .isLightStatusBar(false)
                    .hasShadowBg(false)
                    .isViewMode(true)
                    .moveUpToKeyboard(false)
                    .enableDrag(false)
                    .asCustom(IMSessionChoosePopup(ctx, it, messages, forwardType))
                    .show()
            }
        }
    }

    override fun forwardSelectedMessages(forwardType: Int) {
        val messages = binding.rcvMessage.getSelectMessages().toList()
        context?.let { ctx ->
            session?.let {
                XPopup.Builder(ctx).isDestroyOnDismiss(true)
                    .isLightStatusBar(false)
                    .hasShadowBg(false)
                    .isViewMode(true)
                    .moveUpToKeyboard(false)
                    .enableDrag(false)
                    .asCustom(IMSessionChoosePopup(ctx, it, messages, forwardType))
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
            if (messages[i].type == MsgType.IMAGE.value || messages[i].type == MsgType.VIDEO.value) {
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
            if (messages[position - 1 - i].type == MsgType.IMAGE.value || messages[position - 1 - i].type == MsgType.VIDEO.value) {
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

    override fun getSession(): Session {
        return session!!
    }

    override fun resendMessage(msg: Message) {
        IMCoreManager.messageModule.resend(msg)
    }

    override fun sendMessage(
        type: Int,
        body: Any?,
        data: Any?,
        atUser: String?,
        referMsgId: Long?
    ) {
        val callback = object : IMSendMsgCallback {
            override fun onResult(message: Message, e: Exception?) {}
        }
        IMCoreManager.messageModule
            .sendMessage(session!!.id, type, body, data, atUser, referMsgId, callback)
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

}