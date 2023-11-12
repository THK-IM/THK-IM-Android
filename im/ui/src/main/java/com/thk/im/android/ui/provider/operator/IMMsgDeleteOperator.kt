package com.thk.im.android.ui.provider.operator

import com.thk.im.android.base.BaseSubscriber
import com.thk.im.android.base.RxTransform
import com.thk.im.android.core.IMCoreManager
import com.thk.im.android.db.entity.Message
import com.thk.im.android.ui.R
import com.thk.im.android.ui.protocol.IMMessageOperator
import com.thk.im.android.ui.protocol.internal.IMMsgSender

class IMMsgDeleteOperator : IMMessageOperator() {
    override fun id(): String {
        return "Delete"
    }

    override fun title(): String {
        return "删除"
    }

    override fun resId(): Int {
        return R.drawable.icon_msg_operate_forward
    }

    override fun onClick(sender: IMMsgSender, message: Message) {
        IMCoreManager.getMessageModule()
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
}