package com.thk.im.android.ui.fragment

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
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
import com.thk.im.android.base.LLog
import com.thk.im.android.base.ToastUtils
import com.thk.im.android.base.popup.KeyboardPopupWindow
import com.thk.im.android.core.IMCoreManager
import com.thk.im.android.core.IMEvent
import com.thk.im.android.core.IMFileFormat
import com.thk.im.android.core.event.XEventBus
import com.thk.im.android.db.MsgType
import com.thk.im.android.db.entity.Message
import com.thk.im.android.db.entity.Session
import com.thk.im.android.ui.databinding.FragmentMessageBinding
import com.thk.im.android.ui.manager.IMAudioMsgData
import com.thk.im.android.ui.manager.IMFile
import com.thk.im.android.ui.manager.IMImageMsgBody
import com.thk.im.android.ui.manager.IMImageMsgData
import com.thk.im.android.ui.manager.IMUIManager
import com.thk.im.android.ui.manager.IMVideoMsgBody
import com.thk.im.android.ui.manager.IMVideoMsgData
import com.thk.im.android.ui.manager.ImageMediaItem
import com.thk.im.android.ui.manager.MediaItem
import com.thk.im.android.ui.manager.VideoMediaItem
import com.thk.im.android.ui.protocol.AudioCallback
import com.thk.im.android.ui.protocol.AudioStatus
import com.thk.im.android.ui.protocol.IMContentResult
import com.thk.im.android.ui.protocol.IMMsgPreviewer
import com.thk.im.android.ui.protocol.IMMsgSender

class IMMessageFragment : Fragment(), IMMsgPreviewer, IMMsgSender {
    private lateinit var keyboardPopupWindow: KeyboardPopupWindow
    private var keyboardShowing = false
    private var session: Session? = null
    private lateinit var binding: FragmentMessageBinding
    private var preview: View? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMessageBinding.inflate(
            inflater,
            container,
            false
        )
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        keyboardPopupWindow.dismiss()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        session = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getParcelable("session", Session::class.java) as Session
        } else {
            arguments?.getParcelable("session")!!
        }
        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)
        binding.rcvMessage.init(this, this, this)
        binding.llInputLayout.init(this, this, this)
        binding.llBottomLayout.init(this, this, this)
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
            binding.llAlwaysShow,
            "translationY",
            0 - bottomHeight.toFloat()
        )
        animator.duration = duration
        val lp = binding.llBottomLayout.layoutParams
        lp.height = bottomHeight
        binding.llBottomLayout.layoutParams = lp
        val bottomAnimator = ObjectAnimator.ofFloat(
            binding.llBottomLayout,
            "translationY",
            0 - bottomHeight.toFloat()
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
            IMUIManager.contentProvider?.pick(it,
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
            IMUIManager.contentProvider?.openCamera(it,
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
                    val imageMsgData = IMVideoMsgData()
                    imageMsgData.path = media.path
                    IMCoreManager.getMessageModule()
                        .sendMessage(imageMsgData, session!!.id, MsgType.VIDEO.value)
                } else if (media.mimeType.startsWith("image", true)) {
                    val imageMsgData = IMImageMsgData()
                    imageMsgData.path = media.path
                    IMCoreManager.getMessageModule()
                        .sendMessage(imageMsgData, session!!.id, MsgType.IMAGE.value)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    private fun initEventBus() {
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
    }

    override fun previewMessage(msg: Message, position: Int, originView: View) {
        LLog.v("previewMessage ${msg.type} $position, $msg")
        when (msg.type) {
            MsgType.Audio.value -> {
                msg.data?.let {
                    val data = Gson().fromJson(it, IMAudioMsgData::class.java)
                    data.path?.let { path ->
                        IMUIManager.contentProvider?.startPlayAudio(path, object : AudioCallback {
                            override fun audioData(
                                path: String,
                                second: Int,
                                db: Double,
                                state: AudioStatus
                            ) {
                                ToastUtils.show("play: $second, $db")
                            }
                        })
                    }
                }
            }

            MsgType.IMAGE.value,
            MsgType.VIDEO.value -> {
                previewImageAndVideo(msg, position, originView)
            }
        }
    }

    private fun previewImageAndVideo(msg: Message, position: Int, originView: View) {
        val messages = binding.rcvMessage.getMessages()
        val medias = ArrayList<MediaItem>()
        var count = 0
        val rightMedias = mutableListOf<MediaItem>()
        for (i in position until messages.size) {
            if (msg.type == MsgType.IMAGE.value || msg.type == MsgType.VIDEO.value) {
                val media = convertMessageToMediaItem(messages[i])
                media?.let {
                    rightMedias.add(media)
                    count++
                }
            }
            if (count == 5) {
                break
            }
        }

        medias.addAll(rightMedias.reversed())
        val previewPos = medias.size - 1
        count = 0
        for (i in 0 until position) {
            if (msg.type == MsgType.IMAGE.value || msg.type == MsgType.VIDEO.value) {
                val media = convertMessageToMediaItem(messages[position - 1 - i])
                media?.let {
                    medias.add(media)
                    count++
                }
            }
            if (count == 5) {
                break
            }
        }
        preview = originView
        activity?.let { IMUIManager.contentProvider?.preview(it, medias, originView, previewPos) }
    }

    private fun convertMessageToMediaItem(message: Message): MediaItem? {
        return when (message.type) {
            MsgType.IMAGE.value -> {
                val item = ImageMediaItem(0, 0, null, null, null, null)
                if (message.data != null) {
                    val data = Gson().fromJson(message.data, IMImageMsgData::class.java)
                    if (data != null) {
                        item.thumbnailPath = data.thumbnailPath
                        item.sourcePath = data.path
                        data.width?.let { width ->
                            item.width = width
                        }
                        data.height?.let { height ->
                            item.height = height
                        }
                    }
                }
                if (message.content != null) {
                    val content = Gson().fromJson(message.content, IMImageMsgBody::class.java)
                    if (content != null) {
                        item.thumbnailUrl = content.thumbnailUrl
                        item.sourceUrl = content.url
                        content.width?.let { width ->
                            item.width = width
                        }
                        content.height?.let { height ->
                            item.height = height
                        }
                    }
                }
                item
            }

            MsgType.VIDEO.value -> {
                val item = VideoMediaItem(0, 0, 0, null, null, null, null)
                if (message.data != null) {
                    val data = Gson().fromJson(message.data, IMVideoMsgData::class.java)
                    if (data != null) {
                        item.coverPath = data.thumbnailPath
                        item.sourcePath = data.path
                        data.duration?.let { duration ->
                            item.duration = duration
                        }
                        data.width?.let { width ->
                            item.width = width
                        }
                        data.height?.let { height ->
                            item.height = height
                        }
                    }
                }
                if (message.content != null) {
                    val content = Gson().fromJson(message.content, IMVideoMsgBody::class.java)
                    if (content != null) {
                        item.coverUrl = content.thumbnailUrl
                        item.sourceUrl = content.url
                        content.duration?.let { duration ->
                            item.duration = duration
                        }
                        content.width?.let { width ->
                            item.width = width
                        }
                        content.height?.let { height ->
                            item.height = height
                        }
                    }
                }
                item
            }

            else -> {
                null
            }
        }
    }

    override fun getSession(): Session {
        return session!!
    }

    override fun resendMessage(msg: Message) {
        IMCoreManager.getMessageModule().resend(msg)
    }

    override fun sendMessage(type: Int, body: Any) {
        IMCoreManager.getMessageModule().sendMessage(body, session!!.id, type)
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
            XXPermissions.with(it)
                .permission(Permission.CAMERA)
                .request { _, all ->
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

    override fun showMsgMultiChooseLayout() {

    }

    override fun dismissMsgMultiChooseLayout() {
    }

}