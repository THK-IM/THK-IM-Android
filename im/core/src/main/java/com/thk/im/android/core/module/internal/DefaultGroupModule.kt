package com.thk.im.android.core.module.internal

import com.thk.im.android.base.BaseSubscriber
import com.thk.im.android.base.RxTransform
import com.thk.im.android.core.IMCoreManager
import com.thk.im.android.core.api.bean.GroupApplyBean
import com.thk.im.android.core.api.bean.GroupApplyMessageBean
import com.thk.im.android.core.api.bean.GroupBean
import com.thk.im.android.core.api.bean.GroupMemberBean
import com.thk.im.android.core.module.GroupModule
import com.thk.im.android.db.entity.Group
import com.thk.im.android.db.entity.GroupMember
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable

open class DefaultGroupModule : GroupModule {

    override fun onNewGroup(groupBean: GroupBean) {
        IMCoreManager.getImDataBase().groupDao().insertGroup(groupBean.toGroup())
    }

    override fun onLeveGroup(reason: Int, groupId: Long) {
        IMCoreManager.getImDataBase().groupDao().deleteGroupById(groupId)
    }

    override fun onGroupInfoUpdate(groupBean: GroupBean) {

    }

    override fun onNewGroupApply(groupApplyBean: GroupApplyBean) {

    }

    override fun onNewGroupApplyMessage(groupApplyMessageBean: GroupApplyMessageBean) {
        TODO("Not yet implemented")
    }

    override fun onNewGroupMember(groupMember: GroupMember) {
        IMCoreManager.getImDataBase().groupMemberDao().insertGroupMembers(groupMember)
    }

    override fun onRemoveGroupMember(groupMember: GroupMember) {
        IMCoreManager.getImDataBase().groupMemberDao()
            .deleteGroupMemberById(groupMember.gid, groupMember.uid)
    }


    override fun createGroup(name: String, members: List<Int>): Flowable<GroupBean> {
        return createGroupByApi(name, members).flatMap {
            IMCoreManager.getImDataBase().groupDao().insertGroup(it.toGroup())
            Flowable.just(it)
        }

    }

    override fun createGroupByApi(name: String, members: List<Int>): Flowable<GroupBean> {
        TODO("Not yet implemented")
    }

    override fun applyGroup(gid: Long): Flowable<GroupApplyBean> {
        TODO("Not yet implemented")
    }

    override fun replyGroupApply(applyId: Long, message: String, passed: Int): Flowable<Void> {
        TODO("Not yet implemented")
    }

    override fun leaveGroup(gid: Long, reason: String): Flowable<Void> {
        TODO("Not yet implemented")
    }

    override fun updateGroupInfo(groupBean: GroupBean): Flowable<Void> {
        TODO("Not yet implemented")
    }

    override fun getGroupInfo(gid: Long): Flowable<Group> {
        return Flowable.create<Group>({
            val group = IMCoreManager.getImDataBase().groupDao().findGroup(gid)
            if (group != null) {
                it.onNext(group)
            } else {
                it.onNext(Group(gid))
            }
        }, BackpressureStrategy.LATEST).flatMap {
            if (it.cTime == 0L) {
                return@flatMap getServerGroupInfo(it.id).flatMap { bean ->
                    val group = bean.toGroup()
                    IMCoreManager.getImDataBase().groupDao().insertGroup(group)
                    Flowable.just(group)
                }
            } else {
                Flowable.just(it)
            }
        }.compose(RxTransform.flowableToMain())
    }

    override fun getServerGroupInfo(gid: Long): Flowable<GroupBean> {
        TODO("Not yet implemented")
    }

    override fun syncUserGroups() {
        TODO("Not yet implemented")
    }

    override fun invite(gid: Long, members: List<Int>): Flowable<Void> {
        TODO("Not yet implemented")
    }

    override fun remove(gid: Long, members: List<Int>, reason: String): Flowable<Void> {
        TODO("Not yet implemented")
    }


    override fun queryGroupMembersFromServer(gid: Long): Flowable<List<GroupMemberBean>> {
        TODO("Not yet implemented")
    }

    override fun queryGroupMembers(gid: Long): Flowable<List<GroupMember>> {
        return Flowable.create<List<GroupMember>>({
            val groupMembers = IMCoreManager.getImDataBase().groupMemberDao().queryGroupMembers(gid)
            it.onNext(groupMembers)
        }, BackpressureStrategy.LATEST).flatMap {
            if (it.isEmpty()) {
                return@flatMap queryGroupMembersFromServer(gid).flatMap { list ->
                    IMCoreManager.getImDataBase().groupMemberDao()
                        .insertGroupMembers(*list.map { l ->
                            l.toGroupMember()
                        }.toTypedArray())
                    Flowable.just(list.map { l ->
                        l.toGroupMember()
                    })
                }
            } else {
                Flowable.just(it)
            }
        }.compose(RxTransform.flowableToMain())
    }

    override fun queryGroupMemberByUid(gid: Long, uid: Long): Flowable<GroupMember> {
        return Flowable.create<GroupMember>({
            val groupMember = IMCoreManager.getImDataBase().groupMemberDao().queryGroupMember(uid)
            if (groupMember == null) {
                it.onNext(GroupMember(0L, 0L))
            } else {
                it.onNext(groupMember)
            }
        }, BackpressureStrategy.LATEST).flatMap {
            if (it.gid == 0L && it.uid == 0L) {
                return@flatMap queryGroupMemberByUidFromServer(gid, uid).flatMap { member ->
                    IMCoreManager.getImDataBase().groupMemberDao()
                        .insertGroupMembers(member.toGroupMember())
                    Flowable.just(member.toGroupMember())
                }
            } else {
                Flowable.just(it)
            }
        }.compose(RxTransform.flowableToMain())
    }

    override fun queryGroupMemberByUidFromServer(gid: Long, uid: Long): Flowable<GroupMemberBean> {
        TODO("Not yet implemented")
    }

    override fun syncGroupMembers(gid: Long, cTime: Long, offset: Int, size: Int) {
        syncGroupMembersFromServer(gid, cTime, offset, size)
            .compose(RxTransform.flowableToIo())
            .subscribe(object : BaseSubscriber<List<GroupMemberBean>>() {
                override fun onNext(t: List<GroupMemberBean>) {
                    for (bean in t) {
                        onNewGroupMember(bean.toGroupMember())
                    }
                    if (t.size >= size) {
                        syncGroupMembers(gid, cTime, offset + t.size, size)
                    }
                }

                override fun onError(t: Throwable?) {
                    super.onError(t)
                    t?.printStackTrace()
                }
            })
    }

    override fun syncGroupMembersFromServer(
        gid: Long,
        cTime: Long,
        offset: Int,
        size: Int
    ): Flowable<List<GroupMemberBean>> {
        TODO("Not yet implemented")
    }

    override fun onSignalReceived(subType: Int, body: String) {

    }
}