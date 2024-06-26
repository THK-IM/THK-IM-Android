package com.thk.im.android.ui.manager

import android.app.Application
import androidx.annotation.Keep
import androidx.emoji2.bundled.BundledEmojiCompatConfig
import androidx.emoji2.text.EmojiCompat
import com.thk.im.android.core.IMCoreManager
import com.thk.im.android.core.base.utils.AppUtils
import com.thk.im.android.core.base.utils.IMKeyboardUtils
import com.thk.im.android.core.base.utils.ToastUtils
import com.thk.im.android.core.db.entity.Message
import com.thk.im.android.core.db.entity.Session
import com.thk.im.android.core.db.entity.SessionMember
import com.thk.im.android.core.db.entity.User
import com.thk.im.android.ui.protocol.IMBaseEmojiFragmentProvider
import com.thk.im.android.ui.protocol.IMBaseFunctionIVProvider
import com.thk.im.android.ui.protocol.IMBaseMessageIVProvider
import com.thk.im.android.ui.protocol.IMBaseSessionIVProvider
import com.thk.im.android.ui.protocol.IMMessageOperator
import com.thk.im.android.ui.protocol.IMPageRouter
import com.thk.im.android.ui.protocol.IMPreviewer
import com.thk.im.android.ui.protocol.IMProvider
import com.thk.im.android.ui.protocol.IMUIResourceProvider
import com.thk.im.android.ui.provider.emoji.IMUnicodeEmojiEmojiProvider
import com.thk.im.android.ui.provider.function.IMAlbumFunctionIVProvider
import com.thk.im.android.ui.provider.function.IMCameraFunctionIVProvider
import com.thk.im.android.ui.provider.msg.IMAudioMsgIVProvider
import com.thk.im.android.ui.provider.msg.IMImageMsgIVProvider
import com.thk.im.android.ui.provider.msg.IMRecordMsgIVProvider
import com.thk.im.android.ui.provider.msg.IMRevokeMsgIVProvider
import com.thk.im.android.ui.provider.msg.IMTextMsgIVProvider
import com.thk.im.android.ui.provider.msg.IMTimeLineMsgIVProvider
import com.thk.im.android.ui.provider.msg.IMUnSupportMsgIVProvider
import com.thk.im.android.ui.provider.msg.IMVideoMsgIVProvider
import com.thk.im.android.ui.provider.msg.proccessor.IMAudioMsgProcessor
import com.thk.im.android.ui.provider.msg.proccessor.IMImageMsgProcessor
import com.thk.im.android.ui.provider.msg.proccessor.IMReadMessageProcessor
import com.thk.im.android.ui.provider.msg.proccessor.IMRecordMsgProcessor
import com.thk.im.android.ui.provider.msg.proccessor.IMReeditMessageProcessor
import com.thk.im.android.ui.provider.msg.proccessor.IMRevokeMsgProcessor
import com.thk.im.android.ui.provider.msg.proccessor.IMTextMsgProcessor
import com.thk.im.android.ui.provider.msg.proccessor.IMUnSupportMsgProcessor
import com.thk.im.android.ui.provider.msg.proccessor.IMVideoMsgProcessor
import com.thk.im.android.ui.provider.operator.IMMsgCopyOperator
import com.thk.im.android.ui.provider.operator.IMMsgDeleteOperator
import com.thk.im.android.ui.provider.operator.IMMsgEditOperator
import com.thk.im.android.ui.provider.operator.IMMsgForwardOperator
import com.thk.im.android.ui.provider.operator.IMMsgMultiSelectOperator
import com.thk.im.android.ui.provider.operator.IMMsgReplyOperator
import com.thk.im.android.ui.provider.operator.IMMsgRevokeOperator
import com.thk.im.android.ui.provider.session.provider.GroupSessionIVProvider
import com.thk.im.android.ui.provider.session.provider.SingleSessionIVProvider
import com.thk.im.android.ui.provider.session.provider.SuperGroupSessionIVProvider

@Keep
object IMUIManager {

    private val messageIVProviders = HashMap<Int, IMBaseMessageIVProvider>()
    private val sessionIVProviders = HashMap<Int, IMBaseSessionIVProvider>()
    val emojiFragmentProviders = HashMap<Int, IMBaseEmojiFragmentProvider>()
    val functionIVProviders = HashMap<Int, IMBaseFunctionIVProvider>()
    private val msgOperators = HashMap<String, IMMessageOperator>()
    var mediaProvider: IMProvider? = null
    var mediaPreviewer: IMPreviewer? = null
    var pageRouter: IMPageRouter? = null
    var uiResourceProvider: IMUIResourceProvider? = null

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

    fun getFunctionProvider(session: Session): List<IMBaseFunctionIVProvider> {
        return functionIVProviders.values.toList().filter { f ->
            f.supportSession(session)
        }.sortedBy { f -> f.position() }
    }

    fun getMsgOperators(message: Message, session: Session): List<IMMessageOperator> {
        return msgOperators.values.toList().filter { opr ->
            opr.supportMessage(
                message, session
            )
        }.sortedBy { opr -> opr.id() }
    }

    fun init(app: Application) {
        IMKeyboardUtils.init(app)
        AppUtils.instance().init(app)
        ToastUtils.init(app)
        EmojiCompat.init(BundledEmojiCompatConfig(app))

        IMCoreManager.messageModule.registerMsgProcessor(IMReadMessageProcessor())
        IMCoreManager.messageModule.registerMsgProcessor(IMUnSupportMsgProcessor())
        IMCoreManager.messageModule.registerMsgProcessor(IMTextMsgProcessor())
        IMCoreManager.messageModule.registerMsgProcessor(IMImageMsgProcessor())
        IMCoreManager.messageModule.registerMsgProcessor(IMAudioMsgProcessor())
        IMCoreManager.messageModule.registerMsgProcessor(IMVideoMsgProcessor())
        IMCoreManager.messageModule.registerMsgProcessor(IMRevokeMsgProcessor())
        IMCoreManager.messageModule.registerMsgProcessor(IMRecordMsgProcessor())
        IMCoreManager.messageModule.registerMsgProcessor(IMReeditMessageProcessor())

        val providers = arrayOf(
            IMTimeLineMsgIVProvider(),
            IMUnSupportMsgIVProvider(),
            IMTextMsgIVProvider(),
            IMImageMsgIVProvider(),
            IMAudioMsgIVProvider(),
            IMVideoMsgIVProvider(),
            IMRevokeMsgIVProvider(),
            IMRecordMsgIVProvider()
        )
        registerMsgIVProvider(*providers)
        registerSessionIVProvider(SingleSessionIVProvider())
        registerSessionIVProvider(GroupSessionIVProvider())
        registerSessionIVProvider(SuperGroupSessionIVProvider())

        val unicodeEmojiProvider = IMUnicodeEmojiEmojiProvider(0)
        emojiFragmentProviders[unicodeEmojiProvider.position()] = unicodeEmojiProvider

        val cameraFunctionProvider = IMCameraFunctionIVProvider()
        val albumFunctionIVProvider = IMAlbumFunctionIVProvider()
        functionIVProviders[cameraFunctionProvider.position()] = cameraFunctionProvider
        functionIVProviders[albumFunctionIVProvider.position()] = albumFunctionIVProvider

        registerMsgOperator(IMMsgDeleteOperator())
        registerMsgOperator(IMMsgRevokeOperator())
        registerMsgOperator(IMMsgCopyOperator())
        registerMsgOperator(IMMsgForwardOperator())
        registerMsgOperator(IMMsgReplyOperator())
        registerMsgOperator(IMMsgMultiSelectOperator())
        registerMsgOperator(IMMsgEditOperator())
    }

    fun nicknameForSessionMember(user: User, sessionMember: SessionMember?): String {
        var nickname = user.nickname
        sessionMember?.let {
            if (!it.noteName.isNullOrBlank()) {
                nickname = it.noteName!!
            }
        }
        return nickname
    }

    fun avatarForSessionMember(user: User, sessionMember: SessionMember?): String? {
        return if (sessionMember?.noteAvatar != null && sessionMember.noteAvatar!!.isNotEmpty()) {
            sessionMember.noteAvatar!!
        } else {
            user.avatar
        }
    }

}