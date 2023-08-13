package com.thk.im.android.core.api

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

    private val downloadClient = OkHttpClient.Builder()
        .connectTimeout(defaultTimeout, TimeUnit.SECONDS)
        .writeTimeout(defaultTimeout, TimeUnit.SECONDS)
        .readTimeout(defaultTimeout, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .followRedirects(true)
        .connectionPool(ConnectionPool(maxIdleConnection, keepAliveDuration, TimeUnit.SECONDS))
        .build()

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
        .baseUrl("")
        .build()

    fun <T> getImApi(cls: Class<T>): T {
        return retrofit.create(cls)
    }

    fun getDownloadHttpClient(): OkHttpClient {
        return downloadClient
    }
}