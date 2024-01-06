package com.thk.im.android.module

import android.content.Context
import com.thk.im.android.api.DataRepository
import com.thk.im.android.api.contact.vo.ContactVo
import com.thk.im.android.core.IMCoreManager
import com.thk.im.android.core.api.vo.ListVo
import com.thk.im.android.core.base.BaseSubscriber
import com.thk.im.android.core.base.RxTransform
import com.thk.im.android.core.module.internal.DefaultContactModule

class IMContactModule : DefaultContactModule() {

    private val spName = "THK_IM"
    private val lastSyncContactTime = "Last_Sync_Contact_Time"

    private fun setOfflineMsgSyncTime(time: Long): Boolean {
        val app = IMCoreManager.app
        val sp = app.getSharedPreferences(spName, Context.MODE_PRIVATE)
        val editor = sp.edit()
        editor.putLong("${lastSyncContactTime}_${IMCoreManager.uId}", time)
        return editor.commit()
    }

    private fun getOfflineMsgLastSyncTime(): Long {
        val app = IMCoreManager.app
        val sp = app.getSharedPreferences(spName, Context.MODE_PRIVATE)
        return sp.getLong("${lastSyncContactTime}_${IMCoreManager.uId}", 0)
    }

    override fun syncContacts() {
        val uId = IMCoreManager.uId
        val mTime = getOfflineMsgLastSyncTime()
        val count = 100
        val offset = 0

        val subscribe = object : BaseSubscriber<ListVo<ContactVo>>() {
            override fun onNext(t: ListVo<ContactVo>?) {
                t?.let { vo ->
                    val contactList = vo.data.map {
                        it.toContact()
                    }
                    IMCoreManager.db.contactDao().insertOrReplaceContacts(contactList)
                    if (t.data.isNotEmpty()) {
                        setOfflineMsgSyncTime(t.data.last().updateTime)
                    }
                    if (t.data.size >= count) {
                        syncContacts() // 递归调用
                    }

                }
            }
        }

        DataRepository.contactApi.queryLatestContactList(uId, mTime, count, offset)
            .compose(RxTransform.flowableToIo())
            .subscribe(subscribe)

    }
}