package com.thk.im.android.live.signal

interface LiveSignalProtocol {

    /**
     *  被请求呼叫
     */
    fun onSignalReceived(signal: LiveSignal)


}