package com.thk.im.android.ui.manager

import android.app.Application
import androidx.emoji2.bundled.BundledEmojiCompatConfig
import androidx.emoji2.text.EmojiCompat
import com.thk.im.android.base.utils.IMKeyboardUtils
import com.thk.im.android.core.IMCoreManager
import com.thk.im.android.ui.protocol.IMPreviewer
import com.thk.im.android.ui.protocol.IMProvider
import com.thk.im.android.ui.provider.IMBaseFunctionIVProvider
import com.thk.im.android.ui.provider.IMBaseMessageIVProvider
import com.thk.im.android.ui.provider.IMBasePanelFragmentProvider
import com.thk.im.android.ui.provider.IMBaseSessionIVProvider
import com.thk.im.android.ui.provider.internal.function.IMAlbumFunctionIVProvider
import com.thk.im.android.ui.provider.internal.function.IMCameraFunctionIVProvider
import com.thk.im.android.ui.provider.internal.msg.IMAudioMsgIVProvider
import com.thk.im.android.ui.provider.internal.msg.IMImageMsgIVProvider
import com.thk.im.android.ui.provider.internal.msg.IMTextMsgIVProvider
import com.thk.im.android.ui.provider.internal.msg.IMTimeLineMsgIVProvider
import com.thk.im.android.ui.provider.internal.msg.IMUnSupportMsgIVProvider
import com.thk.im.android.ui.provider.internal.msg.IMVideoMsgIVProvider
import com.thk.im.android.ui.provider.internal.msg.proccessor.IMAudioMsgProcessor
import com.thk.im.android.ui.provider.internal.msg.proccessor.IMImageMsgProcessor
import com.thk.im.android.ui.provider.internal.msg.proccessor.IMTextMsgProcessor
import com.thk.im.android.ui.provider.internal.msg.proccessor.IMUnSupportMsgProcessor
import com.thk.im.android.ui.provider.internal.msg.proccessor.IMVideoMsgProcessor
import com.thk.im.android.ui.provider.internal.panel.IMUnicodeEmojiPanelProvider
import com.thk.im.android.ui.provider.internal.session.provider.SingleSessionIVProvider

object IMUIManager {

    private val messageIVProviders = HashMap<Int, IMBaseMessageIVProvider>()
    private val sessionIVProviders = HashMap<Int, IMBaseSessionIVProvider>()
    var panelFragmentProviders = HashMap<Int, IMBasePanelFragmentProvider>()
    var functionIVProviders = HashMap<Int, IMBaseFunctionIVProvider>()
    var mediaProvider: IMProvider? = null
    var mediaPreviewer: IMPreviewer? = null

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

        IMCoreManager.getMessageModule().registerMsgProcessor(IMUnSupportMsgProcessor())
        IMCoreManager.getMessageModule().registerMsgProcessor(IMTextMsgProcessor())
        IMCoreManager.getMessageModule().registerMsgProcessor(IMImageMsgProcessor())
        IMCoreManager.getMessageModule().registerMsgProcessor(IMAudioMsgProcessor())
        IMCoreManager.getMessageModule().registerMsgProcessor(IMVideoMsgProcessor())

        IMKeyboardUtils.init(app)
        val providers = arrayOf(
            IMTimeLineMsgIVProvider(),
            IMUnSupportMsgIVProvider(),
            IMTextMsgIVProvider(),
            IMImageMsgIVProvider(),
            IMAudioMsgIVProvider(),
            IMVideoMsgIVProvider()
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