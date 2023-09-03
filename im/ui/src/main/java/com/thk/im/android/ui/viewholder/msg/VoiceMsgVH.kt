package com.thk.im.android.ui.viewholder.msg

import android.view.View
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
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

}