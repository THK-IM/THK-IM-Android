package com.thk.im.android.core.module.internal

import com.thk.im.android.core.bean.ContactorApplyBean
import com.thk.im.android.core.bean.ContactorApplyMessageBean
import com.thk.im.android.core.bean.ContactorBean
import com.thk.im.android.core.module.ContactorModule
import io.reactivex.Flowable

open class DefaultContactorModule : ContactorModule {

    override fun onContactorInfoUpdate(contract: ContactorBean) {

    }

    override fun onNewContactor(contract: ContactorBean) {

    }

    override fun onRemoveContactor(contract: ContactorBean) {

    }

    override fun onNewBlack(contract: ContactorBean) {

    }

    override fun onRemoveBlack(contract: ContactorBean) {

    }

    override fun onNewContactorApply(contactorApplyBean: ContactorApplyBean) {

    }

    override fun onNewContactorApplyReply(contractApplyBean: ContactorApplyMessageBean) {

    }

    override fun setBlack(uId: Long): Flowable<ContactorApplyBean> {
        TODO("Not yet implemented")
    }

    override fun applyContactor(uId: Long): Flowable<ContactorApplyBean> {
        TODO("Not yet implemented")
    }

    override fun replyApply(
        applyId: Long,
        message: String,
        passed: Int
    ): Flowable<ContactorApplyMessageBean> {
        TODO("Not yet implemented")
    }

    override fun queryContactorApply(applyId: Long): Flowable<ContactorApplyBean> {
        TODO("Not yet implemented")
    }

    override fun queryContactorApplyReply(applyId: Long): Flowable<List<ContactorApplyMessageBean>> {
        TODO("Not yet implemented")
    }

    override fun syncContactorApply(): Flowable<List<ContactorApplyBean>> {
        TODO("Not yet implemented")
    }

    override fun getContactorInfo(cid: Long): Flowable<ContactorBean> {
        TODO("Not yet implemented")
    }

    override fun syncUserContactors(): Flowable<List<ContactorApplyBean>> {
        TODO("Not yet implemented")
    }

    override fun onSignalReceived(subType: Int, body: String) {

    }
}