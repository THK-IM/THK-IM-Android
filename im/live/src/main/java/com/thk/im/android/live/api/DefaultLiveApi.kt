package com.thk.im.android.live.api

import com.thk.im.android.core.api.internal.APITokenInterceptor
import com.thk.im.android.live.LiveApi
import com.thk.im.android.live.api.vo.CallRoomMemberReqVo
import com.thk.im.android.live.api.vo.CancelCallRoomMemberReqVo
import com.thk.im.android.live.api.vo.CreateRoomReqVo
import com.thk.im.android.live.api.vo.RoomResVo
import com.thk.im.android.live.api.vo.DelRoomVo
import com.thk.im.android.live.api.vo.InviteMemberReqVo
import com.thk.im.android.live.api.vo.JoinRoomReqVo
import com.thk.im.android.live.api.vo.JoinRoomResVo
import com.thk.im.android.live.api.vo.KickoffMemberReqVo
import com.thk.im.android.live.api.vo.LeaveRoomReqVo
import com.thk.im.android.live.api.vo.PlayStreamReqVo
import com.thk.im.android.live.api.vo.PlayStreamResVo
import com.thk.im.android.live.api.vo.PublishStreamReqVo
import com.thk.im.android.live.api.vo.PublishStreamResVo
import com.thk.im.android.live.api.vo.RefuseJoinRoomVo
import io.reactivex.Flowable
import okhttp3.ConnectionPool
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class DefaultLiveApi(private var token: String, private var serverUrl: String) : LiveApi {

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

    override fun createRoom(req: CreateRoomReqVo): Flowable<RoomResVo> {
        return roomApi.createRoom(req)
    }

    override fun queryRoom(id: String): Flowable<RoomResVo> {
        return roomApi.queryRoom(id)
    }

    override fun callRoomMember(req: CallRoomMemberReqVo): Flowable<Void> {
        return roomApi.callRoomMember(req)
    }

    override fun cancelCallRoomMember(req: CancelCallRoomMemberReqVo): Flowable<Void> {
        return roomApi.cancelCallRoomMember(req)
    }

    override fun joinRoom(req: JoinRoomReqVo): Flowable<JoinRoomResVo> {
        return roomApi.joinRoom(req)
    }

    override fun leaveRoom(req: LeaveRoomReqVo): Flowable<Void> {
        return roomApi.leaveRoom(req)
    }

    override fun inviteMember(req: InviteMemberReqVo): Flowable<Void> {
        return roomApi.inviteMember(req)
    }

    override fun refuseJoinRoom(req: RefuseJoinRoomVo): Flowable<Void> {
        return roomApi.refuseJoinRoom(req)
    }

    override fun kickRoomMember(req: KickoffMemberReqVo): Flowable<Void> {
        return roomApi.kickoffMember(req)
    }

    override fun delRoom(req: DelRoomVo): Flowable<Void> {
        return roomApi.delRoom(req)
    }

}