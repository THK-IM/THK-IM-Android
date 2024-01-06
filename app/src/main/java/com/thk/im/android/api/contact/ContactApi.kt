package com.thk.im.android.api.contact

import com.thk.im.android.api.contact.vo.ApplyFriendVo
import com.thk.im.android.api.contact.vo.BlackVo
import com.thk.im.android.api.contact.vo.ContactSessionCreateVo
import com.thk.im.android.api.contact.vo.ContactVo
import com.thk.im.android.api.contact.vo.FollowVo
import com.thk.im.android.api.contact.vo.ReviewFriendApplyVo
import com.thk.im.android.api.contact.vo.UpdateNoteNameVo
import com.thk.im.android.core.api.vo.ListVo
import com.thk.im.android.core.api.vo.PageListVo
import com.thk.im.android.core.api.vo.SessionVo
import io.reactivex.Flowable
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ContactApi {

    /**
     * 设置备注名
     */
    @POST("/contact/note_name")
    fun updateNoteName(
        @Body body: UpdateNoteNameVo
    ): Flowable<Void>

    /**
     * 审核好友申请
     */
    @POST("/contact/friend/apply/review")
    fun reviewFriendApply(
        @Body body: ReviewFriendApplyVo
    ): Flowable<Void>


    /**
     * 申请好友
     */
    @POST("/contact/friend/apply")
    fun applyFriend(
        @Body body: ApplyFriendVo
    ): Flowable<Void>

    /**
     * 拉黑
     */
    @POST("/contact/black")
    fun black(
        @Body body: BlackVo
    ): Flowable<Void>

    /**
     * 取消拉黑
     */
    @DELETE("/contact/black")
    fun cancelBlack(
        @Body body: BlackVo
    ): Flowable<Void>


    /**
     * 关注
     */
    @POST("/contact/follow")
    fun follow(
        @Body body: FollowVo
    ): Flowable<Void>

    /**
     * 取消关注
     */
    @DELETE("/contact/follow")
    fun cancelFollow(
        @Body body: FollowVo
    ): Flowable<Void>


    /**
     * 创建会话
     */
    @POST("/contact/session")
    fun createContactSession(
        @Body body: ContactSessionCreateVo
    ): Flowable<SessionVo>


    @GET("/contact")
    fun queryContactList(
        @Query("u_id") uId: Long,
        @Query("relation_type") relationType: Int,
        @Query("count") count: Int,
        @Query("offset") offset: Int,
    ): Flowable<PageListVo<ContactVo>>

    @GET("/contact/latest")
    fun queryLatestContactList(
        @Query("u_id") uId: Long,
        @Query("m_time") mTime: Long,
        @Query("count") count: Int,
        @Query("offset") offset: Int,
    ): Flowable<ListVo<ContactVo>>

}