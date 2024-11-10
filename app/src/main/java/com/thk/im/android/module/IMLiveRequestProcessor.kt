package com.thk.im.android.module

import android.app.Application
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.enums.PopupAnimation
import com.thk.im.android.IMApplication
import com.thk.im.android.live.BeingRequestedSignal
import com.thk.im.android.live.CancelBeingRequestedSignal
import com.thk.im.android.live.LiveRequestProcessor
import com.thk.im.android.ui.component.BeRequestedCallPopup

class IMLiveRequestProcessor(private val app: Application) : LiveRequestProcessor {

    private val popupMap = hashMapOf<String, BeRequestedCallPopup>()
    private val processedRoomIds = mutableSetOf<String>()

    override fun onBeingRequested(signal: BeingRequestedSignal) {
        val currentActivity = (app as? IMApplication)?.currentActivity()
        if (currentActivity != null) {
            if (processedRoomIds.contains(signal.roomId)) {
                return
            }
            processedRoomIds.add(signal.roomId)
            if (popupMap[signal.roomId] == null) {
                val beRequestedPopup = BeRequestedCallPopup(currentActivity)
                beRequestedPopup.signal = signal
                XPopup.Builder(currentActivity).isDestroyOnDismiss(true)
                    .popupAnimation(PopupAnimation.TranslateFromTop)
                    .asCustom(beRequestedPopup)
                    .show()
                popupMap[signal.roomId] = beRequestedPopup
            }
        } else {
            // 应用切入后台
        }
    }

    override fun onCancelBeingRequested(signal: CancelBeingRequestedSignal) {
        popupMap[signal.roomId]?.let {
            it.post {
                it.dismiss()
            }
        }
        popupMap.remove(signal.roomId)
    }
}