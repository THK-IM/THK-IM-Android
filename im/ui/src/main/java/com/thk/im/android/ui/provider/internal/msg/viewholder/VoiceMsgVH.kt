package com.thk.im.android.ui.provider.internal.msg.viewholder

import android.view.View
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import com.thk.im.android.ui.R
import com.thk.im.android.ui.fragment.viewholder.BaseMsgVH

class VoiceMsgVH(liftOwner: LifecycleOwner, itemView: View, viewType: Int) :
    BaseMsgVH(liftOwner, itemView, viewType) {


    private lateinit var duration: TextView

    override fun getContentId(): Int {
        return R.layout.itemview_msg_video
    }

}