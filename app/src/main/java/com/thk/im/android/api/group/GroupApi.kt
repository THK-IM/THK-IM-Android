package com.thk.im.android.api.group

import com.thk.im.android.api.group.vo.CreateGroupVo
import com.thk.im.android.api.group.vo.DeleteGroupVo
import com.thk.im.android.api.group.vo.GroupVo
import com.thk.im.android.api.group.vo.JoinGroupVo
import com.thk.im.android.api.group.vo.TransferGroupVo
import com.thk.im.android.api.group.vo.UpdateGroupVo
import io.reactivex.Flowable
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface GroupApi {

    /**
     * 创建群
     */
    @POST("/group")
    fun createGroup(
        @Body body: CreateGroupVo
    ): Flowable<GroupVo>


    /**
     * 搜索群
     */
    @GET("/group")
    fun searchGroup(
        @Query("display_id") displayId: String,
    ): Flowable<GroupVo>

    /**
     * 查询群
     */
    @GET("/group/{id}")
    fun queryGroup(
        @Path("id") id: Long,
    ): Flowable<GroupVo>

    /**
     * 更新群
     */
    @PUT("/group/{id}")
    fun updateGroup(
        @Path("id") id: String,
        @Body body: UpdateGroupVo
    ): Flowable<GroupVo>


    /**
     * 加入群
     */
    @POST("/group/{id}/join")
    fun joinGroup(
        @Path("id") id: String,
        @Body body: JoinGroupVo
    ): Flowable<GroupVo>


    /**
     * 删除群
     */
    @DELETE("/group/{id}")
    fun deleteGroup(
        @Path("id") id: String,
        @Body body: DeleteGroupVo
    ): Flowable<Void>


    /**
     * 转让群
     */
    @POST("/group/{id}/transfer")
    fun transferGroup(
        @Path("id") id: String,
        @Body body: TransferGroupVo
    ): Flowable<Void>
}