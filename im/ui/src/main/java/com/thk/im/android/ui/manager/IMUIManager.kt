package com.thk.im.android.ui.manager

import android.app.Application
import androidx.emoji2.bundled.BundledEmojiCompatConfig
import androidx.emoji2.text.EmojiCompat
import com.thk.im.android.core.IMCoreManager
import com.thk.im.android.ui.protocol.IMMediaPreviewer
import com.thk.im.android.ui.protocol.IMMediaProvider
import com.thk.im.android.ui.provider.IMBaseFunctionIVProvider
import com.thk.im.android.ui.provider.IMBaseMessageIVProvider
import com.thk.im.android.ui.provider.IMBasePanelFragmentProvider
import com.thk.im.android.ui.provider.IMBaseSessionIVProvider
import com.thk.im.android.ui.provider.internal.function.IMAlbumFunctionIVProvider
import com.thk.im.android.ui.provider.internal.function.IMCameraFunctionIVProvider
import com.thk.im.android.ui.provider.internal.msg.AudioMsgIVProvider
import com.thk.im.android.ui.provider.internal.msg.ImageMsgIVProvider
import com.thk.im.android.ui.provider.internal.msg.TextMsgIVProvider
import com.thk.im.android.ui.provider.internal.msg.TimeLineMsgIVProvider
import com.thk.im.android.ui.provider.internal.msg.UnSupportMsgIVProvider
import com.thk.im.android.ui.provider.internal.msg.VideoMsgIVProvider
import com.thk.im.android.ui.provider.internal.msg.proccessor.AudioMsgProcessor
import com.thk.im.android.ui.provider.internal.msg.proccessor.ImageMsgProcessor
import com.thk.im.android.ui.provider.internal.msg.proccessor.TextMsgProcessor
import com.thk.im.android.ui.provider.internal.msg.proccessor.UnSupportMsgProcessor
import com.thk.im.android.ui.provider.internal.msg.proccessor.VideoMsgProcessor
import com.thk.im.android.ui.provider.internal.panel.IMUnicodeEmojiPanelProvider
import com.thk.im.android.ui.provider.internal.session.provider.SingleSessionIVProvider
import com.thk.im.android.ui.utils.IMKeyboardUtils

object IMUIManager {

    private val messageIVProviders = HashMap<Int, IMBaseMessageIVProvider>()
    private val sessionIVProviders = HashMap<Int, IMBaseSessionIVProvider>()
    var panelFragmentProviders = HashMap<Int, IMBasePanelFragmentProvider>()
    var functionIVProviders = HashMap<Int, IMBaseFunctionIVProvider>()
    var mediaProvider: IMMediaProvider? = null
    var mediaPreviewer: IMMediaPreviewer? = null

    fun registerMsgIVProvider(vararg providers: IMBaseMessageIVProvider) {
        for (p in providers) {
            messageIVProviders[p.messageType()] = p
        }
    }

    fun getMsgIVProviderByMsgType(msgType: Int): IMBaseMessageIVProvider {
        val provider = messageIVProviders[msgType]
        return provider ?: messageIVProviders[0]!!
    }

    fun getMsgIVProviderByViewType(viewType: Int): IMBaseMessageIVProvider {
        val messageType = viewType / 3
        val provider = messageIVProviders[messageType]
        return provider ?: messageIVProviders[0]!!
    }

    fun registerSessionIVProvider(provider: IMBaseSessionIVProvider) {
        sessionIVProviders[provider.sessionType()] = provider
    }

    fun getSessionIVProvider(type: Int): IMBaseSessionIVProvider {
        val provider = sessionIVProviders[type]
        return provider ?: sessionIVProviders[1]!!
    }

    fun init(app: Application) {
        EmojiCompat.init(BundledEmojiCompatConfig(app))

        IMCoreManager.getMessageModule().registerMsgProcessor(UnSupportMsgProcessor())
        IMCoreManager.getMessageModule().registerMsgProcessor(TextMsgProcessor())
        IMCoreManager.getMessageModule().registerMsgProcessor(ImageMsgProcessor())
        IMCoreManager.getMessageModule().registerMsgProcessor(AudioMsgProcessor())
        IMCoreManager.getMessageModule().registerMsgProcessor(VideoMsgProcessor())

        IMKeyboardUtils.init(app)
        val providers = arrayOf(
            TimeLineMsgIVProvider(),
            UnSupportMsgIVProvider(),
            TextMsgIVProvider(),
            ImageMsgIVProvider(),
            AudioMsgIVProvider(),
            VideoMsgIVProvider()
        )
        registerMsgIVProvider(*providers)
        registerSessionIVProvider(SingleSessionIVProvider())

        for (i in 0..10) {
            val unicodeEmojiProvider = IMUnicodeEmojiPanelProvider(i)
            panelFragmentProviders[unicodeEmojiProvider.position()] = unicodeEmojiProvider
        }

        val cameraFunctionProvider = IMCameraFunctionIVProvider()
        val albumFunctionIVProvider = IMAlbumFunctionIVProvider()

        functionIVProviders[cameraFunctionProvider.position()] = cameraFunctionProvider
        functionIVProviders[albumFunctionIVProvider.position()] = albumFunctionIVProvider
    }

}