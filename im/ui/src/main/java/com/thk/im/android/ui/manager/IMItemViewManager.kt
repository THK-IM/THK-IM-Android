package com.thk.im.android.ui.manager

import com.thk.im.android.ui.provider.MsgItemViewProvider
import com.thk.im.android.ui.provider.SessionItemViewProvider
import com.thk.im.android.ui.provider.internal.*
import com.thk.im.android.ui.provider.internal.DefaultSsIVProvider
import com.thk.im.android.ui.provider.internal.TextMsgIVProvider
import com.thk.im.android.ui.provider.internal.TimeLineMsgIVProvider
import com.thk.im.android.ui.provider.internal.UnSupportMsgIVProvider
import com.thk.im.android.ui.provider.internal.VoiceMsgIVProvider

object IMItemViewManager {

    private val msgItemViewProviders = HashMap<Int, MsgItemViewProvider>()
    private var sessionItemViewProvider: SessionItemViewProvider = DefaultSsIVProvider()

    fun registerMsgIVProvider(vararg providers: MsgItemViewProvider) {
        for (p in providers) {
            msgItemViewProviders[p.messageType()] = p
        }
    }

    fun getMsgIVProviderByMsgType(msgType: Int): MsgItemViewProvider {
        val provider = msgItemViewProviders[msgType]
        return provider ?: msgItemViewProviders[0]!!
    }

    fun getMsgIVProviderByViewType(viewType: Int): MsgItemViewProvider {
        val messageType = viewType / 3
        val provider = msgItemViewProviders[messageType]
        return provider ?: msgItemViewProviders[0]!!
    }

    fun registerSessionIVProvider(provider: SessionItemViewProvider) {
        sessionItemViewProvider = provider
    }

    fun getSessionIVProvider(): SessionItemViewProvider {
        return sessionItemViewProvider
    }

    fun init() {
        val providers = arrayOf(
            TimeLineMsgIVProvider(),
            UnSupportMsgIVProvider(),
            TextMsgIVProvider(),
            ImageMsgIVProvider(),
            VoiceMsgIVProvider(),
            VideoMsgIVProvider()
        )
        registerMsgIVProvider(*providers)
    }

}