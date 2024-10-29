package com.thk.im.android.live.signal

interface LiveSignalProtocol {

    /**
     *  被请求呼叫
     */
    fun onCallBeingRequested(signal: BeingRequestedSignal)

    /**
     *  被请求呼叫结束
     */
    fun onCallCancelRequested(signal: CancelRequestedSignal)

    /**
     *  主动呼叫被拒绝
     */
    fun onCallRequestBeRejected(signal: RejectRequestSignal)

    /**
     *  主动呼叫被接受
     */
    fun onCallRequestBeAccepted(signal: AcceptRequestSignal)

    /**
     *  通话中挂断
     */
    fun onCallingBeHangup(signal: HangupSignal)

    /**
     *  通话结束
     */
    fun onCallingBeEnded(signal: EndCallSignal)
}