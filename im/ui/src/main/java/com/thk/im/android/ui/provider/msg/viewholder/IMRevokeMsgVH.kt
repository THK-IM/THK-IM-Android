package com.thk.im.android.ui.provider.msg.viewholder

import android.view.View
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import com.google.gson.Gson
import com.thk.im.android.core.IMCoreManager
import com.thk.im.android.core.MsgType
import com.thk.im.android.core.db.entity.Message
import com.thk.im.android.core.db.entity.Session
import com.thk.im.android.ui.R
import com.thk.im.android.ui.fragment.viewholder.BaseMsgVH
import com.thk.im.android.ui.manager.IMRevokeMsgData
import com.thk.im.android.ui.protocol.internal.IMMsgVHOperator

class IMRevokeMsgVH(liftOwner: LifecycleOwner, itemView: View, viewType: Int) :
    BaseMsgVH(liftOwner, itemView, viewType) {
    override fun getContentId(): Int {
        return R.layout.itemview_msg_revoke
    }

    override fun onViewBind(
        position: Int,
        messages: List<Message>,
        session: Session,
        msgVHOperator: IMMsgVHOperator
    ) {
        super.onViewBind(position, messages, session, msgVHOperator)
        val contentView: TextView = itemView.findViewById(R.id.tv_who_revoke)
        val reeditView: TextView = itemView.findViewById(R.id.tv_reedit)

        val revokeData = Gson().fromJson(message.data, IMRevokeMsgData::class.java)
        if (revokeData != null) {
            contentView.text = "${revokeData.nick}撤回了一条消息"
            if (message.fUid == IMCoreManager.uId
                && revokeData.content != null
                && revokeData.type != null && revokeData.type == MsgType.TEXT.value) {
                reeditView.visibility = View.VISIBLE
                reeditView.isClickable = true
                reeditView.setOnClickListener {
                    msgVHOperator.setEditText(revokeData.content!!)
                }
            } else {
                reeditView.visibility = View.GONE
            }
        } else {
            contentView.text = "对方撤回了一条消息"
            reeditView.visibility = View.GONE
        }
    }

}