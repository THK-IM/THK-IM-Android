package com.thk.im.android.base

import io.reactivex.FlowableTransformer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.Executors

object RxTransform {

    private val schedulers = Schedulers.from(Executors.newFixedThreadPool(4))

    /**
     * Flowable 主线程订阅
     */
    fun <T> flowableToMain(): FlowableTransformer<T, T> {
        return FlowableTransformer { upstream ->
            upstream.subscribeOn(schedulers)
                .observeOn(AndroidSchedulers.mainThread())
        }
    }

    /**
     * Flowable 子线程订阅
     */
    fun <T> flowableToIo(): FlowableTransformer<T, T> {
        return FlowableTransformer { upstream ->
            upstream.subscribeOn(schedulers)
                .observeOn(schedulers)
        }
    }
}

