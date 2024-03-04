package com.thk.im.android.ui.provider.msg.viewholder

import android.view.View
import androidx.lifecycle.LifecycleOwner
import com.thk.im.android.core.db.entity.Message
import com.thk.im.android.core.db.entity.Session
import com.thk.im.android.ui.fragment.view.IMsgBodyView
import com.thk.im.android.ui.fragment.viewholder.msg.IMBaseMsgVH
import com.thk.im.android.ui.protocol.internal.IMMsgVHOperator
import com.thk.im.android.ui.provider.msg.view.IMTimeLineMsgView

class IMTimeLineMsgVHIM(
    lifecycleOwner: LifecycleOwner, itemView: View, viewType: Int
) : IMBaseMsgVH(
    lifecycleOwner, itemView, viewType,
) {
    private val view: IMTimeLineMsgView

    init {
        view = IMTimeLineMsgView(itemView.context)
    }

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