package com.thk.im.android.module

import android.app.Application
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.enums.PopupAnimation
import com.thk.im.android.IMApplication
import com.thk.im.android.live.BeingRequestedSignal
import com.thk.im.android.live.CancelBeingRequestedSignal
import com.thk.im.android.live.LiveRequestProcessor
import com.thk.im.android.ui.call.popup.BeRequestedCallingPopup

class IMLiveRequestProcessor(private val app: Application) : LiveRequestProcessor {

    private val processedRoomIds = mutableSetOf<String>()

    override fun onBeingRequested(signal: BeingRequestedSignal) {
        val currentActivity = (app as? IMApplication)?.currentActivity()
        if (currentActivity != null) {
            if (processedRoomIds.contains(signal.roomId)) {
                return
            }
            processedRoomIds.add(signal.roomId)
            val beRequestedPopup = BeRequestedCallingPopup(currentActivity)
            beRequestedPopup.signal = signal
            XPopup.Builder(currentActivity).isDestroyOnDismiss(true)
                .dismissOnTouchOutside(false)
                .popupAnimation(PopupAnimation.TranslateFromTop)
                .asCustom(beRequestedPopup)
                .show()
        } else {
            // 应用切入后台
        }
    }

    override fun onCancelBeingRequested(signal: CancelBeingRequestedSignal) {
    }

}