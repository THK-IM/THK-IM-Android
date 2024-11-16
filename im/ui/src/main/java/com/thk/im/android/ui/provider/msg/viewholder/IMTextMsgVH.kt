package com.thk.im.android.ui.provider.msg.viewholder

import android.view.View
import androidx.lifecycle.LifecycleOwner
import com.thk.im.android.core.db.entity.Message
import com.thk.im.android.core.db.entity.Session
import com.thk.im.android.ui.fragment.view.IMsgBodyView
import com.thk.im.android.ui.fragment.viewholder.msg.IMBaseMsgVH
import com.thk.im.android.ui.protocol.internal.IMMsgVHOperator
import com.thk.im.android.ui.provider.msg.view.IMTextMsgView

class IMTextMsgVH(liftOwner: LifecycleOwner, itemView: View, viewType: Int) :
    IMBaseMsgVH(liftOwner, itemView, viewType) {

    private val view: IMTextMsgView = IMTextMsgView(itemView.context)

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