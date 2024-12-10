package com.thk.im.android.module.msg.call

import android.view.View
import androidx.lifecycle.LifecycleOwner
import com.thk.im.android.core.db.entity.Message
import com.thk.im.android.core.db.entity.Session
import com.thk.im.android.ui.fragment.view.IMsgBodyView
import com.thk.im.android.ui.fragment.viewholder.msg.IMBaseMsgVH
import com.thk.im.android.ui.protocol.internal.IMMsgVHOperator

class IMCallMsgVH(liftOwner: LifecycleOwner, itemView: View, viewType: Int) :
    IMBaseMsgVH(liftOwner, itemView, viewType) {

    private val view = IMCallMsgView(itemView.context)

    override fun msgBodyView(): IMsgBodyView {
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