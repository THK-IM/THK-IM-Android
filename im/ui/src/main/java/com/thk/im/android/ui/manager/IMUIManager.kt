package com.thk.im.android.ui.manager

import android.app.Application
import androidx.emoji2.bundled.BundledEmojiCompatConfig
import androidx.emoji2.text.EmojiCompat
import com.thk.im.android.core.base.utils.IMKeyboardUtils
import com.thk.im.android.core.IMCoreManager
import com.thk.im.android.core.db.entity.Message
import com.thk.im.android.ui.protocol.IMBaseFunctionIVProvider
import com.thk.im.android.ui.protocol.IMBaseMessageIVProvider
import com.thk.im.android.ui.protocol.IMBasePanelFragmentProvider
import com.thk.im.android.ui.protocol.IMBaseSessionIVProvider
import com.thk.im.android.ui.protocol.IMMessageOperator
import com.thk.im.android.ui.protocol.IMPreviewer
import com.thk.im.android.ui.protocol.IMProvider
import com.thk.im.android.ui.provider.msg.proccessor.IMAudioMsgProcessor
import com.thk.im.android.ui.provider.msg.proccessor.IMImageMsgProcessor
import com.thk.im.android.ui.provider.msg.proccessor.IMTextMsgProcessor
import com.thk.im.android.ui.provider.msg.proccessor.IMUnSupportMsgProcessor
import com.thk.im.android.ui.provider.msg.proccessor.IMVideoMsgProcessor
import com.thk.im.android.ui.provider.operator.IMMsgCopyOperator
import com.thk.im.android.ui.provider.operator.IMMsgDeleteOperator
import com.thk.im.android.ui.provider.operator.IMMsgForwardOperator
import com.thk.im.android.ui.provider.operator.IMMsgMultiSelectOperator
import com.thk.im.android.ui.provider.operator.IMMsgReplyOperator
import com.thk.im.android.ui.provider.operator.IMMsgRevokeOperator
import com.thk.im.android.ui.provider.panel.IMUnicodeEmojiPanelProvider
import com.thk.im.android.ui.provider.session.provider.SingleSessionIVProvider

object IMUIManager {

    private val messageIVProviders = HashMap<Int, IMBaseMessageIVProvider>()
    private val sessionIVProviders = HashMap<Int, IMBaseSessionIVProvider>()
    val panelFragmentProviders = HashMap<Int, IMBasePanelFragmentProvider>()
    val functionIVProviders = HashMap<Int, IMBaseFunctionIVProvider>()
    private val msgOperators = HashMap<String, IMMessageOperator>()
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

    fun registerMsgOperator(operator: IMMessageOperator) {
        msgOperators[operator.id()] = operator
    }

    fun getMsgOperators(message: Message): List<IMMessageOperator> {
        return msgOperators.values.toList()
    }

    fun init(app: Application) {
        EmojiCompat.init(BundledEmojiCompatConfig(app))

        IMCoreManager.getMessageModule().registerMsgProcessor(IMUnSupportMsgProcessor())
        IMCoreManager.getMessageModule().registerMsgProcessor(IMTextMsgProcessor())
        IMCoreManager.getMessageModule().registerMsgProcessor(IMImageMsgProcessor())
        IMCoreManager.getMessageModule().registerMsgProcessor(IMAudioMsgProcessor())
        IMCoreManager.getMessageModule().registerMsgProcessor(IMVideoMsgProcessor())

        com.thk.im.android.core.base.utils.IMKeyboardUtils.init(app)
        val providers = arrayOf(
            com.thk.im.android.ui.provider.msg.IMTimeLineMsgIVProvider(),
            com.thk.im.android.ui.provider.msg.IMUnSupportMsgIVProvider(),
            com.thk.im.android.ui.provider.msg.IMTextMsgIVProvider(),
            com.thk.im.android.ui.provider.msg.IMImageMsgIVProvider(),
            com.thk.im.android.ui.provider.msg.IMAudioMsgIVProvider(),
            com.thk.im.android.ui.provider.msg.IMVideoMsgIVProvider()
        )
        registerMsgIVProvider(*providers)
        registerSessionIVProvider(SingleSessionIVProvider())

        for (i in 0..10) {
            val unicodeEmojiProvider = IMUnicodeEmojiPanelProvider(i)
            panelFragmentProviders[unicodeEmojiProvider.position()] = unicodeEmojiProvider
        }

        val cameraFunctionProvider =
            com.thk.im.android.ui.provider.function.IMCameraFunctionIVProvider()
        val albumFunctionIVProvider =
            com.thk.im.android.ui.provider.function.IMAlbumFunctionIVProvider()

        functionIVProviders[cameraFunctionProvider.position()] = cameraFunctionProvider
        functionIVProviders[albumFunctionIVProvider.position()] = albumFunctionIVProvider

        registerMsgOperator(IMMsgDeleteOperator())
        registerMsgOperator(IMMsgRevokeOperator())
        registerMsgOperator(IMMsgCopyOperator())
        registerMsgOperator(IMMsgForwardOperator())
        registerMsgOperator(IMMsgReplyOperator())
        registerMsgOperator(IMMsgMultiSelectOperator())
    }

}