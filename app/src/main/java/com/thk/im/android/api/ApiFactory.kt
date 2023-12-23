package com.thk.im.android.api

import com.thk.im.android.core.api.internal.TokenInterceptor
import okhttp3.ConnectionPool
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class ApiFactory(private var token: String, serverUrl: String) {

    private val defaultTimeout: Long = 30
    private val maxIdleConnection = 8
    private val keepAliveDuration: Long = 60

    private val interceptor = TokenInterceptor(token)

    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(defaultTimeout, TimeUnit.SECONDS)
        .writeTimeout(defaultTimeout, TimeUnit.SECONDS)
        .readTimeout(defaultTimeout, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .addInterceptor(interceptor)
        .connectionPool(ConnectionPool(maxIdleConnection, keepAliveDuration, TimeUnit.SECONDS))
        .build()

    private val retrofit: Retrofit = Retrofit.Builder()
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .baseUrl(serverUrl)
        .build()

    fun updateToken(token: String) {
        this.token = token
        interceptor.updateToken(token)
    }

    fun <T> createApi(cls: Class<T>): T {
        return retrofit.create(cls)
    }

}