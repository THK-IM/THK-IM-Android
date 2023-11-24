package com.thk.im.android.ui.provider.msg.viewholder

import android.view.View
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import com.google.gson.Gson
import com.thk.im.android.core.db.entity.Message
import com.thk.im.android.core.db.entity.Session
import com.thk.im.android.ui.R
import com.thk.im.android.ui.fragment.viewholder.BaseMsgVH
import com.thk.im.android.ui.manager.IMRecordMsgBody
import com.thk.im.android.ui.protocol.internal.IMMsgVHOperator

class IMRecordMsgVH(liftOwner: LifecycleOwner, itemView: View, viewType: Int) :
    BaseMsgVH(liftOwner, itemView, viewType) {
    override fun getContentId(): Int {
        return R.layout.itemview_msg_record
    }

    override fun onViewBind(
        position: Int,
        messages: List<Message>,
        session: Session,
        msgVHOperator: IMMsgVHOperator
    ) {
        super.onViewBind(position, messages, session, msgVHOperator)
        val titleView: TextView = itemView.findViewById(R.id.tv_record_title)
        val contentView: TextView = itemView.findViewById(R.id.tv_record_content)

        val recordBody = Gson().fromJson(message.content, IMRecordMsgBody::class.java)
        titleView.text = recordBody.title
        contentView.text = recordBody.content
    }
}
