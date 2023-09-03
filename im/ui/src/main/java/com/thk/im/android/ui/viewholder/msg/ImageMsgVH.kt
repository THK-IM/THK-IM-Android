package com.thk.im.android.ui.viewholder.msg

import android.view.View
import androidx.lifecycle.LifecycleOwner
import com.thk.im.android.db.entity.Message
import com.thk.im.android.db.entity.Session
import com.thk.im.android.ui.R

class ImageMsgVH(liftOwner: LifecycleOwner, itemView: View, viewType: Int) :
    BaseMsgVH(liftOwner, itemView, viewType), View.OnClickListener {

    override fun getContentId(): Int {
        return R.layout.itemview_msg_image
    }

    override fun onViewCreated() {
        super.onViewCreated()
        contentContainer.setOnClickListener(this)
    }


    override fun onClick(v: View?) {

    }
}