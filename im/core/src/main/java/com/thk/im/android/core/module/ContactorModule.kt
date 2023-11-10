package com.thk.im.android.core.module

import com.thk.im.android.core.api.bean.ContactorApplyBean
import com.thk.im.android.core.api.bean.ContactorApplyMessageBean
import com.thk.im.android.core.api.bean.ContactorBean
import io.reactivex.Flowable

interface ContactorModule : BaseModule {

    /**
     * 【收到服务器通知】联系人信息更新
     */
    fun onContactorInfoUpdate(contract: ContactorBean)

    /**
     * 【收到服务器通知】新增联系人
     */
    fun onNewContactor(contract: ContactorBean)

    /**
     * 【收到服务器通知】移除联系人
     */
    fun onRemoveContactor(contract: ContactorBean)

    /**
     * 【收到服务器通知】新增黑名单
     */
    fun onNewBlack(contract: ContactorBean)

    /**
     * 【收到服务器通知】移除黑名单
     */
    fun onRemoveBlack(contract: ContactorBean)

    /**
     * 【收到服务器通知】新增联系人申请
     */
    fun onNewContactorApply(contactorApplyBean: ContactorApplyBean)

    /**
     * 【收到服务器通知】新增(联系人申请)回复
     */
    fun onNewContactorApplyReply(contractApplyBean: ContactorApplyMessageBean)

    /**
     * 【用户主动发起】设置黑名单
     */
    fun setBlack(uId: Long): Flowable<ContactorApplyBean>

    /**
     * 【用户主动发起】申请联系人
     */
    fun applyContactor(uId: Long): Flowable<ContactorApplyBean>

    /**
     * 【用户主动发起】回复申请
     */
    fun replyApply(applyId: Long, message: String, passed: Int): Flowable<ContactorApplyMessageBean>

    /**
     * 【用户主动发起】查询联系人申请详情
     */
    fun queryContactorApply(applyId: Long): Flowable<ContactorApplyBean>

    /**
     * 【用户主动发起】查询联系人申请回复详情
     */
    fun queryContactorApplyReply(applyId: Long): Flowable<List<ContactorApplyMessageBean>>

    /**
     * 【用户主动发起】同步联系人申请
     */
    fun syncContactorApply(): Flowable<List<ContactorApplyBean>>

    /**
     * 【用户主动发起】获取单个联系人信息
     */
    fun getContactorInfo(cid: Long): Flowable<ContactorBean>

    /**
     * 【用户主动发起】同步用户的联系人
     */
    fun syncUserContactors(): Flowable<List<ContactorApplyBean>>

}
