package com.thk.im.android.ui.provider.operator

import com.thk.im.android.core.IMCoreManager
import com.thk.im.android.core.base.BaseSubscriber
import com.thk.im.android.core.base.RxTransform
import com.thk.im.android.core.db.entity.Message
import com.thk.im.android.core.db.entity.Session
import com.thk.im.android.ui.R
import com.thk.im.android.ui.protocol.IMMessageOperator
import com.thk.im.android.ui.protocol.internal.IMMsgSender

class IMMsgDeleteOperator : IMMessageOperator() {
    override fun id(): String {
        return "Delete"
    }

    override fun title(): String {
        return IMCoreManager.app.getString(R.string.delete)
    }

    override fun resId(): Int {
        return R.drawable.ic_msg_opr_delete
    }

    override fun onClick(sender: IMMsgSender, message: Message) {
        IMCoreManager.messageModule
            .deleteMessages(message.id, listOf(message), true)
            .compose(RxTransform.flowableToMain())
            .subscribe(object : BaseSubscriber<Void>() {
                override fun onNext(t: Void?) {

                }

                override fun onError(t: Throwable?) {
                    super.onError(t)
                }
            })

    }

    override fun supportMessage(message: Message, session: Session): Boolean {
        return true
    }
}