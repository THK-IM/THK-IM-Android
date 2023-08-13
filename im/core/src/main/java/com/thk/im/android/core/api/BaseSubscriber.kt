package com.thk.im.android.core.api

import io.reactivex.subscribers.DisposableSubscriber

abstract class BaseSubscriber<T> : DisposableSubscriber<T>() {

    override fun onError(t: Throwable?) {
        t?.printStackTrace()
        this.dispose()
    }

    override fun onComplete() {
        this.dispose()
    }
}