package com.thk.im.android.live.signal

interface LiveSignalProtocol {

    /**
     *  收到信令
     */
    fun onSignalReceived(signal: LiveSignal)


}