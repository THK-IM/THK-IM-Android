package com.thk.im.android.ui.provider.msg.viewholder

import android.view.View
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import com.google.gson.Gson
import com.thk.im.android.core.base.utils.DateUtils
import com.thk.im.android.core.IMCoreManager
import com.thk.im.android.core.IMMsgResourceType
import com.thk.im.android.core.db.entity.Message
import com.thk.im.android.core.db.entity.Session
import com.thk.im.android.ui.R
import com.thk.im.android.ui.fragment.viewholder.BaseMsgVH
import com.thk.im.android.ui.manager.IMAudioMsgBody
import com.thk.im.android.ui.manager.IMAudioMsgData
import com.thk.im.android.ui.manager.IMMsgPosType
import com.thk.im.android.ui.protocol.internal.IMMsgVHOperator

class IMAudioMsgVH(liftOwner: LifecycleOwner, itemView: View, viewType: Int) :
    BaseMsgVH(liftOwner, itemView, viewType) {

    override fun getContentId(): Int {
        return R.layout.itemview_msg_audio
    }

    override fun onViewBind(
        position: Int,
        messages: List<Message>,
        session: Session,
        msgVHOperator: IMMsgVHOperator
    ) {
        super.onViewBind(position, messages, session, msgVHOperator)
        var duration = 0
        var path = ""
        var played = false
        if (!message.data.isNullOrEmpty()) {
            val audioMsgData = Gson().fromJson(message.data, IMAudioMsgData::class.java)
            audioMsgData?.let {
                if (it.duration != null) {
                    duration = it.duration!!
                }
                if (it.path != null) {
                    path = it.path!!
                }
                played = it.played
            }
        }

        if (!message.content.isNullOrEmpty()) {
            val audioMsgBody = Gson().fromJson(message.content, IMAudioMsgBody::class.java)
            audioMsgBody?.let {
                if (it.duration != null) {
                    duration = it.duration!!
                }
            }
        }

        render(duration, played)
        if (path == "") {
            IMCoreManager.messageModule.getMsgProcessor(message.type)
                .downloadMsgContent(message, IMMsgResourceType.Thumbnail.value)
        }
    }

    private fun render(seconds: Int, played: Boolean) {
        val audioDurationView: TextView = itemView.findViewById(R.id.tv_audio_duration)
        val audioStatusView: View =
            itemView.findViewById(R.id.iv_audio_status)
        audioDurationView.text = com.thk.im.android.core.base.utils.DateUtils.secondToDuration(seconds)
        if (played) {
            audioStatusView.visibility = View.GONE
        } else {
            if (getPositionType() == IMMsgPosType.Right.value) {
                audioStatusView.visibility = View.GONE
            } else {
                audioStatusView.visibility = View.VISIBLE
            }
        }
    }

}