package com.thk.im.android.core.module

import com.thk.im.android.core.api.bean.GroupApplyBean
import com.thk.im.android.core.api.bean.GroupApplyMessageBean
import com.thk.im.android.core.api.bean.GroupBean
import com.thk.im.android.core.api.bean.GroupMemberBean
import com.thk.im.android.db.entity.Group
import com.thk.im.android.db.entity.GroupMember
import io.reactivex.Flowable


interface GroupModule : CommonModule {

    /**
     * 【收到服务器通知】 收到进入新群通知
     */
    fun onNewGroup(groupBean: GroupBean)

    /**
     * 【收到服务器通知】 收到离开群通知
     */
    fun onLeveGroup(reason: Int, groupId: Long)

    /**
     * 【收到服务器通知】 群信息更新
     */
    fun onGroupInfoUpdate(groupBean: GroupBean)

    /**
     * 【收到服务器通知】 收到群申请通知
     */
    fun onNewGroupApply(groupApplyBean: GroupApplyBean)

    /**
     * 【收到服务器通知】 收到新群申请留言
     */
    fun onNewGroupApplyMessage(groupApplyMessageBean: GroupApplyMessageBean)

    /**
     * 【收到服务器通知】 收到新群增员通知
     */
    fun onNewGroupMember(groupMember: GroupMember)

    /**
     * 【收到服务器通知】 收到新群减员通知
     */
    fun onRemoveGroupMember(groupMember: GroupMember)

    /**
     * 【用户主动发起】创建新群
     */
    fun createGroup(name: String, members: List<Int>): Flowable<GroupBean>

    /**
     * 【用户主动发起】调用接口创建新群
     */
    fun createGroupByApi(name: String, members: List<Int>): Flowable<GroupBean>

    /**
     * 【用户主动发起】申请加入群
     */
    fun applyGroup(gid: Long): Flowable<GroupApplyBean>

    /**
     * 【用户主动发起】回复群申请
     */
    fun replyGroupApply(applyId: Long, message: String, passed: Int): Flowable<Void>

    /**
     * 【用户主动发起】离开群
     */
    fun leaveGroup(gid: Long, reason: String): Flowable<Void>

    /**
     * 【用户主动发起】更新群信息
     */
    fun updateGroupInfo(groupBean: GroupBean): Flowable<Void>

    /**
     * 【用户主动发起】获取单个群信息
     */
    fun getGroupInfo(gid: Long): Flowable<Group>


    /**
     * 【用户主动发起】从服务器获取单个群信息
     */
    fun getServerGroupInfo(gid: Long): Flowable<GroupBean>

    /**
     * 【用户主动发起】同步用户的群列表
     */
    fun syncUserGroups()


    /**
     * 【用户主动发起】邀请加入群
     */
    fun invite(gid: Long, members: List<Int>): Flowable<Void>

    /**
     * 【用户主动发起】移除群成员
     */
    fun remove(gid: Long, members: List<Int>, reason: String): Flowable<Void>

    /**
     * 【用户主动发起】查询群成员
     */
    fun queryGroupMembersFromServer(gid: Long): Flowable<List<GroupMemberBean>>

    /**
     * 【用户主动发起】查询群成员
     */
    fun queryGroupMembers(gid: Long): Flowable<List<GroupMember>>

    /**
     * 【用户主动发起】查询单个成员
     * @param gid 群id
     * @param uid 用户id
     */
    fun queryGroupMemberByUid(gid: Long, uid: Long): Flowable<GroupMember>

    /**
     * 【用户主动发起】查询单个成员从服务端
     * @param gid 群id
     * @param uid 用户id
     */
    fun queryGroupMemberByUidFromServer(gid: Long, uid: Long): Flowable<GroupMemberBean>


    /**
     * 同步群成员
     */
    fun syncGroupMembers(gid: Long, cTime: Long, offset: Int, size: Int)

    /**
     * 从服务端同步群成员
     */
    fun syncGroupMembersFromServer(
        gid: Long,
        cTime: Long,
        offset: Int,
        size: Int
    ): Flowable<List<GroupMemberBean>>

}
