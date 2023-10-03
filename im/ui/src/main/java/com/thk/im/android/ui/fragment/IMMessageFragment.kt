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
import com.luck.picture.lib.basic.PictureSelector
import com.luck.picture.lib.config.SelectMimeType
import com.luck.picture.lib.engine.CompressFileEngine
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.interfaces.OnResultCallbackListener
import com.thk.im.android.base.LLog
import com.thk.im.android.base.MediaUtils
import com.thk.im.android.base.ToastUtils
import com.thk.im.android.base.popup.KeyboardPopupWindow
import com.thk.im.android.core.IMCoreManager
import com.thk.im.android.core.IMEvent
import com.thk.im.android.core.event.XEventBus
import com.thk.im.android.db.MsgType
import com.thk.im.android.db.entity.Message
import com.thk.im.android.db.entity.Session
import com.thk.im.android.media.audio.AudioCallback
import com.thk.im.android.media.audio.AudioStatus
import com.thk.im.android.media.audio.OggOpusPlayer
import com.thk.im.android.media.picker.AlbumStyleUtils
import com.thk.im.android.media.picker.GlideEngine
import com.thk.im.android.ui.databinding.FragmentMessageBinding
import com.thk.im.android.ui.manager.IMAudioMsgData
import com.thk.im.android.ui.manager.IMImageMsgData
import com.thk.im.android.ui.manager.IMVideoMsgData
import com.thk.im.android.ui.protocol.IMMsgPreviewer
import com.thk.im.android.ui.protocol.IMMsgSender
import top.zibin.luban.CompressionPredicate
import top.zibin.luban.Luban
import top.zibin.luban.OnNewCompressListener
import java.io.File

class IMMessageFragment : Fragment(), IMMsgPreviewer, IMMsgSender {
    private lateinit var keyboardPopupWindow: KeyboardPopupWindow
    private var keyboardShowing = false
    private var session: Session? = null
    private lateinit var binding: FragmentMessageBinding

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
        PictureSelector.create(context).openGallery(SelectMimeType.ofAll())
            .setImageEngine(GlideEngine.createGlideEngine())
            .setCompressEngine(CompressFileEngine { context, source, call ->
                if (source != null && source.size > 0) {
                    Luban.with(context).load(source).ignoreBy(300)
                        .filter(object : CompressionPredicate {
                            override fun apply(path: String?): Boolean {
                                path?.let {
                                    val isGif = MediaUtils.isGif(it)
                                    return !isGif
                                }
                                return true
                            }
                        })
                        .setCompressListener(object : OnNewCompressListener {
                            override fun onStart() {
                            }

                            override fun onSuccess(source: String?, compressFile: File?) {
                                call.onCallback(source, compressFile?.absolutePath)
                            }

                            override fun onError(source: String?, e: Throwable?) {
                                LLog.e("compress error $e")
                                call.onCallback(source, null)
                            }

                        }).launch()
                }
            })
            .isOriginalControl(true)
            .setSelectorUIStyle(AlbumStyleUtils.getStyle(context))
            .isDisplayCamera(false)
            .isGif(true)
            .isPreviewImage(true)
            .isWithSelectVideoImage(true)
            .isPreviewZoomEffect(true)
            .isPreviewFullScreenMode(false)
            .isPreviewVideo(true)
            .forResult(object :
                OnResultCallbackListener<LocalMedia> {
                override fun onResult(result: ArrayList<LocalMedia>) {
                    onMediaResult(result)
                }

                override fun onCancel() {
                }

            })
    }


    private fun cameraMedia() {
        PictureSelector.create(context)
            .openCamera(SelectMimeType.ofAll())
            .setCompressEngine(CompressFileEngine { context, source, call ->
                if (source != null && source.size > 0) {
                    Luban.with(context).load(source).ignoreBy(300)
                        .setCompressListener(object : OnNewCompressListener {
                            override fun onStart() {
                            }

                            override fun onSuccess(source: String?, compressFile: File?) {
                                call.onCallback(source, compressFile?.absolutePath)
                            }

                            override fun onError(source: String?, e: Throwable?) {
                                call.onCallback(source, null)
                            }

                        }).launch()
                }
            })
            .forResult(object : OnResultCallbackListener<LocalMedia> {
                override fun onResult(result: ArrayList<LocalMedia>) {
                    onMediaResult(result)
                }

                override fun onCancel() {
                }
            })
    }

    private fun onMediaResult(result: ArrayList<LocalMedia>) {
        try {
            for (media in result) {
                if (media.mimeType.startsWith("video", true)) {
                    val imageMsgData = IMVideoMsgData()
                    imageMsgData.path = media.realPath
                    IMCoreManager.getMessageModule()
                        .sendMessage(imageMsgData, session!!.id, MsgType.VIDEO.value)
                } else {
                    var path = media.compressPath
                    if (path == null || media.isOriginal) {
                        path = media.realPath
                    }
                    val imageMsgData = IMImageMsgData()
                    imageMsgData.path = path
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
        if (msg.type == MsgType.Audio.value) {
            msg.data?.let {
                val data = Gson().fromJson(it, IMAudioMsgData::class.java)
                data.path?.let {path ->
                    OggOpusPlayer.startPlay(path, object : AudioCallback {
                        override fun notify(
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
        XXPermissions.with(context)
            .permission(Permission.READ_MEDIA_VIDEO, Permission.READ_MEDIA_IMAGES)
            .request { _, all ->
                if (all) {
                    selectImage()
                }
            }
    }

    override fun openCamera() {
        XXPermissions.with(context)
            .permission(Permission.CAMERA)
            .request { _, all ->
                if (all) {
                    cameraMedia()
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