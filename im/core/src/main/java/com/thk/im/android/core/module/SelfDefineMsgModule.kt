package com.thk.im.android.core.module

interface SelfDefineMsgModule : CommonModule {

    /**
     * 【服务器推送】 收到自定义消息推送
     */
    fun onNewMessage(type: Int, body: String)


    /**
     * 【用户主动发起】发送自定义消息
     */
    fun sendMessage(type: Int, body: String)

}