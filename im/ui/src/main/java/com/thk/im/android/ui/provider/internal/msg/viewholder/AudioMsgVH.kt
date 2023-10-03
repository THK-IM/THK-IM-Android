package com.thk.im.android.ui.provider.internal.msg.viewholder

import android.view.View
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.google.gson.Gson
import com.thk.im.android.core.IMCoreManager
import com.thk.im.android.core.IMEvent
import com.thk.im.android.core.IMLoadProgress
import com.thk.im.android.core.IMMsgResourceType
import com.thk.im.android.core.event.XEventBus
import com.thk.im.android.db.entity.Message
import com.thk.im.android.db.entity.Session
import com.thk.im.android.ui.R
import com.thk.im.android.ui.fragment.viewholder.BaseMsgVH
import com.thk.im.android.ui.manager.IMAudioMsgBody
import com.thk.im.android.ui.manager.IMAudioMsgData
import com.thk.im.android.ui.manager.IMMsgPosType
import com.thk.im.android.ui.protocol.IMMsgVHOperator
import com.thk.im.android.ui.utils.DateUtil

class AudioMsgVH(liftOwner: LifecycleOwner, itemView: View, viewType: Int) :
    BaseMsgVH(liftOwner, itemView, viewType), Observer<IMLoadProgress> {

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
        XEventBus.observe(IMEvent.MsgLoadStatusUpdate.value, this)

        if (!message.data.isNullOrEmpty()) {
            val audioMsgData = Gson().fromJson(message.data, IMAudioMsgData::class.java)
            audioMsgData?.let {
                renderData(it)
                return@onViewBind
            }
        }

        if (!message.content.isNullOrEmpty()) {
            val audioMsgBody = Gson().fromJson(message.content, IMAudioMsgBody::class.java)
            audioMsgBody?.let {
                renderBody(it)
                // 开始下载
                if (!it.url.isNullOrEmpty()) {
                    IMCoreManager.getMessageModule().getMsgProcessor(message.type)
                        .downloadMsgContent(message, IMMsgResourceType.Source.value)
                }
            }
        }
    }

    private fun renderData(audioData: IMAudioMsgData) {
        val audioDurationView: TextView = contentContainer.findViewById(R.id.tv_audio_duration)
        val audioStatusView: View =
            contentContainer.findViewById(R.id.iv_audio_status)
        if (audioData.duration != null) {
            audioDurationView.text = DateUtil.getDuration(audioData.duration!!)
        }
        if (audioData.played) {
            audioStatusView.visibility = View.GONE
        } else {
            if (getType() == IMMsgPosType.Right.value) {
                audioStatusView.visibility = View.GONE
            } else {
                audioStatusView.visibility = View.VISIBLE
            }
        }
    }

    private fun renderBody(imageMsgBody: IMAudioMsgBody) {
        val audioDurationView: TextView = contentContainer.findViewById(R.id.tv_audio_duration)
        audioDurationView.setOnClickListener(null)
        val audioStatusView: View =
            contentContainer.findViewById(R.id.iv_audio_status)
        if (imageMsgBody.duration != null) {
            audioDurationView.text = DateUtil.getDuration(imageMsgBody.duration!!)
        }
        if (getType() == IMMsgPosType.Right.value) {
            audioStatusView.visibility = View.GONE
        } else {
            audioStatusView.visibility = View.VISIBLE
        }

    }

    override fun onViewDetached() {
        super.onViewDetached()
        XEventBus.unObserve(IMEvent.MsgLoadStatusUpdate.value, this)
    }

    override fun onChanged(t: IMLoadProgress?) {
//        t?.let {
//            LLog.v("IMLoadProgress ${it.type} ${it.state} ${it.progress} ${it.key}")
//        }
    }

}