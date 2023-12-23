package com.thk.im.android.api

import com.thk.im.android.core.api.internal.TokenInterceptor
import okhttp3.ConnectionPool
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiFactory {

    private const val defaultTimeout: Long = 30
    private const val maxIdleConnection = 8
    private const val keepAliveDuration: Long = 60

    private lateinit var interceptor: TokenInterceptor

    private lateinit var okHttpClient: OkHttpClient

    private lateinit var retrofit: Retrofit
    fun init(token: String) {
        interceptor = TokenInterceptor(token)
        okHttpClient = OkHttpClient.Builder()
            .connectTimeout(defaultTimeout, TimeUnit.SECONDS)
            .writeTimeout(defaultTimeout, TimeUnit.SECONDS)
            .readTimeout(defaultTimeout, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .addInterceptor(interceptor)
            .connectionPool(ConnectionPool(maxIdleConnection, keepAliveDuration, TimeUnit.SECONDS))
            .build()
    }

    fun updateToken(token: String) {
        interceptor.updateToken(token)
    }


    fun <T> createApi(cls: Class<T>, serverUrl: String): T {
        retrofit = Retrofit.Builder()
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .baseUrl(serverUrl)
            .build()
        return retrofit.create(cls)
    }

}