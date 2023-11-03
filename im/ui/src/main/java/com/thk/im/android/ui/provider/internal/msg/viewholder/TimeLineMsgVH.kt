package com.thk.im.android.ui.provider.internal.msg.viewholder

import android.view.View
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import com.thk.im.android.db.entity.Message
import com.thk.im.android.db.entity.Session
import com.thk.im.android.ui.R
import com.thk.im.android.ui.fragment.adapter.ViewHolderSelect
import com.thk.im.android.ui.fragment.viewholder.BaseMsgVH
import com.thk.im.android.ui.protocol.internal.IMMsgVHOperator
import com.thk.im.android.ui.utils.DateUtil

class TimeLineMsgVH(
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
        msgVHOperator: IMMsgVHOperator,
        viewHolderSelect: ViewHolderSelect
    ) {
        super.onViewBind(position, messages, session, msgVHOperator, viewHolderSelect)
        val tvTime: TextView = itemView.findViewById(R.id.tv_time)
        tvTime.text = DateUtil.getTimeline(message.cTime)
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