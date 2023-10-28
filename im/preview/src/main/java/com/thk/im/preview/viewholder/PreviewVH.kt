package com.thk.im.preview.viewholder

import android.view.View
import androidx.lifecycle.LifecycleOwner
import com.thk.im.android.base.BaseSubscriber
import com.thk.im.android.base.RxTransform
import com.thk.im.android.core.IMCoreManager
import com.thk.im.android.db.entity.Message
import com.thk.im.android.ui.fragment.viewholder.BaseVH
import io.reactivex.Flowable

open class PreviewVH(liftOwner: LifecycleOwner, itemView: View) :
    BaseVH(liftOwner, itemView) {

    protected var message: Message? = null
    open fun bindMessage(message: Message) {
        this.message = message
    }

    open fun startPreview() {

    }

    open fun stopPreview() {

    }

    open fun hide() {
    }

    open fun show() {

    }

    protected open fun updateDb(message: Message) {
        Flowable.just(message).compose(RxTransform.flowableToIo())
            .subscribe(object : BaseSubscriber<Message>() {
                override fun onNext(t: Message?) {
                    t?.let {
                        IMCoreManager.getMessageModule()
                            .getMsgProcessor(t.type)
                            .insertOrUpdateDb(
                                t,
                                notify = true,
                                notifySession = false
                            )
                    }

                }

            })
    }

}