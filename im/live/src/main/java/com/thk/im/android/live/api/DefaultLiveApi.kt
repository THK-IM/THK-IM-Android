package com.thk.im.android.live.api

import com.thk.im.android.core.api.internal.APITokenInterceptor
import com.thk.im.android.live.LiveApi
import com.thk.im.android.live.vo.CreateRoomReqVo
import com.thk.im.android.live.vo.CreateRoomResVo
import com.thk.im.android.live.vo.DelRoomVo
import com.thk.im.android.live.vo.JoinRoomReqVo
import com.thk.im.android.live.vo.JoinRoomResVo
import com.thk.im.android.live.vo.PlayStreamReqVo
import com.thk.im.android.live.vo.PlayStreamResVo
import com.thk.im.android.live.vo.PublishStreamReqVo
import com.thk.im.android.live.vo.PublishStreamResVo
import com.thk.im.android.live.vo.RefuseJoinRoomVo
import io.reactivex.Flowable
import okhttp3.ConnectionPool
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class DefaultLiveApi(var token: String, var serverUrl: String) : LiveApi {

    private val defaultTimeout: Long = 30
    private val maxIdleConnection = 8
    private val keepAliveDuration: Long = 60

    private val interceptor = APITokenInterceptor(token)

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


    private fun <T> getApi(cls: Class<T>): T {
        return retrofit.create(cls)
    }

    private val roomApi: RoomApi = getApi(RoomApi::class.java)
    private val rtcApi: RtcApi = getApi(RtcApi::class.java)

    init {
        interceptor.addValidEndpoint(serverUrl)
    }

    override fun getEndpoint(): String {
        return serverUrl
    }

    override fun publishStream(req: PublishStreamReqVo): Flowable<PublishStreamResVo> {
        return rtcApi.requestPublish(req)
    }

    override fun playStream(req: PlayStreamReqVo): Flowable<PlayStreamResVo> {
        return rtcApi.requestPlay(req)
    }

    override fun createRoom(req: CreateRoomReqVo): Flowable<CreateRoomResVo> {
        return roomApi.createRoom(req)
    }

    override fun joinRoom(req: JoinRoomReqVo): Flowable<JoinRoomResVo> {
        return roomApi.joinRoom(req)
    }

    override fun refuseJoinRoom(req: RefuseJoinRoomVo): Flowable<Void> {
        return roomApi.refuseJoinRoom(req)
    }

    override fun delRoom(req: DelRoomVo): Flowable<Void> {
        return roomApi.delRoom(req)
    }

}