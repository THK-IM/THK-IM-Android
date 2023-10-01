package com.thk.im.android.ui.provider.internal.viewholder.msg

import android.view.View
import androidx.lifecycle.LifecycleOwner
import com.thk.im.android.ui.R
import com.thk.im.android.ui.fragment.viewholder.BaseMsgVH

class VideoMsgVH(liftOwner: LifecycleOwner, itemView: View, viewType: Int) :
    BaseMsgVH(liftOwner, itemView, viewType), View.OnClickListener {


    override fun getContentId(): Int {
        return R.layout.itemview_msg_video
    }

    private fun showView() {
//        val ivMsgThumbnail: ImageView = contentContainer.findViewById(R.id.iv_msg_video_thumbnail)
//        val rlMsgPrompt: RelativeLayout = contentContainer.findViewById(R.id.rl_msg_video_prompt)
//        val tvVideoDuration: TextView = contentContainer.findViewById(R.id.tv_video_duration)
//        if (body.height != null && body.width != null) {
//            val lp = ivMsgThumbnail.layoutParams
//            if (body.width!! > body.height!!) {
//                var calWidth = minOf(150.dp2px(), body.width!!)
//                calWidth = maxOf(60.dp2px(), calWidth)
//                var calHeight = maxOf(calWidth * body.height!! / body.width!!, 60.dp2px())
//                lp.width = calWidth
//                lp.height = calHeight
//            } else {
//                var calHeight = minOf(150.dp2px(), body.height!!)
//                calHeight = maxOf(60.dp2px(), calHeight)
//                var calWidth = maxOf(calHeight * body.width!! / body.height!!, 60.dp2px())
//                lp.width = calWidth
//                lp.height = calHeight
//            }
//            ivMsgThumbnail.layoutParams = lp
//            rlMsgPrompt.layoutParams = lp
//        }
//        body.thumbnailPath?.let {
//            IMImageLoader.displayImageByPath(ivMsgThumbnail, it)
//        }
//
//        body.duration?.let {
//            tvVideoDuration.text = it.toString()
//        }
    }

    override fun onClick(v: View?) {

    }
}