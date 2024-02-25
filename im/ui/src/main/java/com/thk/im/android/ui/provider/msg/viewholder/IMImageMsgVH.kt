package com.thk.im.android.ui.provider.msg.viewholder

import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import com.thk.im.android.core.db.entity.Message
import com.thk.im.android.core.db.entity.Session
import com.thk.im.android.ui.msg.viewholder.BaseMsgVH
import com.thk.im.android.ui.protocol.internal.IMMsgVHOperator
import com.thk.im.android.ui.provider.msg.view.IMImageMsgView

class IMImageMsgVH(liftOwner: LifecycleOwner, itemView: View, viewType: Int) :
    BaseMsgVH(liftOwner, itemView, viewType) {

    private val view: IMImageMsgView

    init {
        view = IMImageMsgView(itemView.context)
    }

    override fun getContentView(): ViewGroup {
        return view
    }

    override fun onViewBind(
        position: Int,
        messages: List<Message>,
        session: Session,
        msgVHOperator: IMMsgVHOperator
    ) {
        super.onViewBind(position, messages, session, msgVHOperator)
        view.setMessage(message, session, msgVHOperator)
    }

}