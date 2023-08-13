package com.thk.im.android.ui.viewholder.msg

import android.view.View
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import com.google.gson.Gson
import com.thk.im.android.core.processor.body.VoiceBody
import com.thk.im.android.db.entity.Message
import com.thk.im.android.db.entity.Session
import com.thk.im.android.ui.R

class VoiceMsgVH(liftOwner: LifecycleOwner, itemView: View, viewType: Int) :
    BaseMsgVH(liftOwner, itemView, viewType) {


    private lateinit var duration: TextView

    override fun getContentId(): Int {
        return if (getType() == MsgPosType.Left.value) {
            R.layout.itemview_msg_voice_left
        } else if (getType() == MsgPosType.Right.value) {
            R.layout.itemview_msg_voice_right
        } else {
            R.layout.itemview_msg_voice_mid
        }
    }

    override fun onViewBind(msg: Message, ses: Session) {
        super.onViewBind(msg, ses)
        duration = contentContainer.findViewById(R.id.audio_duration)
        val body = Gson().fromJson(msg.content, VoiceBody::class.java)
        duration.text = body.duration.toString() + " ”"

        when (getType()) {
            MsgPosType.Left.value -> {

            }

            MsgPosType.Right.value -> {

            }

            else -> {

            }
        }
    }
}