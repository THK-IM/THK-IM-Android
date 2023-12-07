package com.thk.im.android.ui.provider.msg.viewholder

import android.view.View
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import com.thk.im.android.core.base.utils.DateUtils
import com.thk.im.android.core.IMCoreManager
import com.thk.im.android.core.db.entity.Message
import com.thk.im.android.core.db.entity.Session
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
        tvTime.text = DateUtils.timeToMsgTime(message.cTime, IMCoreManager.commonModule.getSeverTime())
    }

    override fun onViewDetached() {
    }

    override fun onLifeOwnerResume() {
    }

    override fun onLifeOwnerPause() {
    }

    override fun canSelect(): Boolean {
        return false
    }
}