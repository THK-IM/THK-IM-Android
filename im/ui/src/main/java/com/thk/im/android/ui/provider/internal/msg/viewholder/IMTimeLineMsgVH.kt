package com.thk.im.android.ui.provider.internal.msg.viewholder

import android.view.View
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import com.thk.im.android.base.utils.DateUtils
import com.thk.im.android.core.IMCoreManager
import com.thk.im.android.db.entity.Message
import com.thk.im.android.db.entity.Session
import com.thk.im.android.ui.R
import com.thk.im.android.ui.fragment.viewholder.BaseMsgVH
import com.thk.im.android.ui.protocol.internal.IMMsgVHOperator

class IMTimeLineMsgVH(
    lifecycleOwner: LifecycleOwner, itemView: View, viewType: Int
) : BaseMsgVH(
    lifecycleOwner, itemView, viewType,
) {
    override fun getContentId(): Int {
        return R.layout.itemview_msg_timeline
    }

    override fun onViewBind(
        position: Int,
        messages: List<Message>,
        session: Session,
        msgVHOperator: IMMsgVHOperator
    ) {
        super.onViewBind(position, messages, session, msgVHOperator)
        val tvTime: TextView = itemView.findViewById(R.id.tv_time)
        tvTime.text = DateUtils.timeToMsgTime(message.cTime, IMCoreManager.getCommonModule().getSeverTime())
    }

    override fun onViewDetached() {
    }

    override fun onLifeOwnerResume() {
    }

    override fun onLifeOwnerPause() {
    }

    override fun supportSelect(): Boolean {
        return false
    }
}