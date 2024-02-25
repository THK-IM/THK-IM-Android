package com.thk.im.android.ui.provider.msg.viewholder

import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import com.thk.im.android.core.db.entity.Message
import com.thk.im.android.core.db.entity.Session
import com.thk.im.android.ui.msg.viewholder.BaseMsgVH
import com.thk.im.android.ui.protocol.internal.IMMsgVHOperator
import com.thk.im.android.ui.provider.msg.view.IMTimeLineMsgView

class IMTimeLineMsgVH(
    lifecycleOwner: LifecycleOwner, itemView: View, viewType: Int
) : BaseMsgVH(
    lifecycleOwner, itemView, viewType,
) {
    private val view: IMTimeLineMsgView

    init {
        view = IMTimeLineMsgView(itemView.context)
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