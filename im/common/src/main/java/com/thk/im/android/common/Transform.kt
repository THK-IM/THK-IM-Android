package com.thk.im.android.common

import io.reactivex.FlowableTransformer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

object Transform {
    /**
     * Flowable 主线程订阅
     */
    fun <T> flowableToMain(): FlowableTransformer<T, T> {
        return FlowableTransformer { upstream ->
            upstream.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
        }
    }

    /**
     * Flowable 子线程订阅
     */
    fun <T> flowableToIo(): FlowableTransformer<T, T> {
        return FlowableTransformer { upstream ->
            upstream.subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
        }
    }
}

