package com.thk.im.android.core.event;

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.jeremyliao.liveeventbus.LiveEventBus


/**
 * 对LiveEventBus的封装
 */
object XEventBus {

    fun <T> post(eventName: String, message: T) {
        LiveEventBus.get<T>(eventName).post(message)
    }

    fun <T> post(eventName: String, message: T, delay: Long) {
        LiveEventBus.get<T>(eventName).postDelay(message, delay)
    }

    fun <T> observe(
        owner: LifecycleOwner,
        eventName: String,
        observer: Observer<T>
    ) {
        LiveEventBus.get<T>(eventName).observe(owner, observer)
    }

    fun <T> observe(
        eventName: String,
        observer: Observer<T>
    ) {
        LiveEventBus.get<T>(eventName).observeForever(observer)
    }

    fun <T> unObserve(
        eventName: String,
        observer: Observer<T>
    ) {
        LiveEventBus.get<T>(eventName).removeObserver(observer)
    }

}
