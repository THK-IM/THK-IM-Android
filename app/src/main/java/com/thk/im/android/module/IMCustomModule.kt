package com.thk.im.android.module

import android.app.Application
import com.google.gson.Gson
import com.thk.im.android.core.base.LLog
import com.thk.im.android.core.module.internal.DefaultCustomModule
import com.thk.im.android.live.LiveManager
import com.thk.im.android.live.LiveSignal
import io.reactivex.disposables.CompositeDisposable


class IMCustomModule(val app: Application) : DefaultCustomModule() {

    private val compositeDisposable = CompositeDisposable()
    private val liveCallSignalType = 400
    override fun reset() {
        compositeDisposable.clear()
    }

    override fun onSignalReceived(type: Int, body: String) {
        LLog.d("onSignalReceived, $type  LiveSignal: $body")
        if (type == liveCallSignalType) {
            try {
                val signal = Gson().fromJson(body, LiveSignal::class.java)
                LiveManager.shared().onLiveSignalReceived(signal)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

}