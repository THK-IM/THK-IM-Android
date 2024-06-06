package com.thk.im.android.core.base

import io.reactivex.subscribers.DisposableSubscriber

abstract class BaseSubscriber<T> : DisposableSubscriber<T>() {

    override fun onError(t: Throwable?) {
        t?.printStackTrace()
    }

    override fun onComplete() {
        this.dispose()
    }
}