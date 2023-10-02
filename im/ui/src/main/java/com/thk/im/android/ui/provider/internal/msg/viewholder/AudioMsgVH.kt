package com.thk.im.android.ui.provider.internal.msg.viewholder

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.google.gson.Gson
import com.thk.im.android.base.ToastUtils
import com.thk.im.android.core.IMAudioMsgBody
import com.thk.im.android.core.IMAudioMsgData
import com.thk.im.android.core.IMCoreManager
import com.thk.im.android.core.IMEvent
import com.thk.im.android.core.IMLoadProgress
import com.thk.im.android.core.IMMsgResourceType
import com.thk.im.android.core.event.XEventBus
import com.thk.im.android.db.entity.Message
import com.thk.im.android.db.entity.Session
import com.thk.im.android.media.audio.AudioCallback
import com.thk.im.android.media.audio.AudioStatus
import com.thk.im.android.media.audio.OggOpusPlayer
import com.thk.im.android.ui.R
import com.thk.im.android.ui.fragment.viewholder.BaseMsgVH
import com.thk.im.android.ui.utils.DateUtil

class AudioMsgVH(liftOwner: LifecycleOwner, itemView: View, viewType: Int) :
    BaseMsgVH(liftOwner, itemView, viewType), Observer<IMLoadProgress> {

    override fun getContentId(): Int {
        return R.layout.itemview_msg_audio
    }


    override fun onViewBind(message: Message, session: Session) {
        super.onViewBind(message, session)
        XEventBus.observe(IMEvent.MsgLoadStatusUpdate.value, this)

        if (message.data.isNotEmpty() && message.data.isNotBlank()) {
            val audioMsgData = Gson().fromJson(message.data, IMAudioMsgData::class.java)
            audioMsgData?.let {
                renderData(it)
                return@onViewBind
            }
        }

        if (message.content.isNotEmpty() && message.content.isNotBlank()) {
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
        audioDurationView.setOnClickListener {
            if (audioData.path != null)
            OggOpusPlayer.startPlay(audioData.path!!, object : AudioCallback {
                override fun notify(path: String, second: Int, db: Double, state: AudioStatus) {
                    ToastUtils.show("play: $second, $db")
                }

            })
        }
        val audioStatusView: View =
            contentContainer.findViewById(R.id.iv_audio_status)
        if (audioData.duration != null) {
            audioDurationView.text = DateUtil.getDuration(audioData.duration!!)
        }
        if (audioData.played) {
            audioStatusView.visibility = View.GONE
        } else {
            if (getType() == MsgPosType.Right.value) {
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
        if (getType() == MsgPosType.Right.value) {
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