package com.thk.android.im.live.api

import okhttp3.ConnectionPool
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiManager {

    private const val defaultTimeout: Long = 30
    private const val maxIdleConnection = 8
    private const val keepAliveDuration: Long = 60
    private var apiEndpoint = "http://192.168.1.3:18100"

    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(defaultTimeout, TimeUnit.SECONDS)
        .writeTimeout(defaultTimeout, TimeUnit.SECONDS)
        .readTimeout(defaultTimeout, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .connectionPool(ConnectionPool(maxIdleConnection, keepAliveDuration, TimeUnit.SECONDS))
        .build()

    private val retrofit: Retrofit = Retrofit.Builder()
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .baseUrl(apiEndpoint)
        .build()

    fun setApiEndpoint(endpoint: String) {
        apiEndpoint = endpoint
    }

    fun <T> getApi(cls: Class<T>): T {
        return retrofit.create(cls)
    }
}