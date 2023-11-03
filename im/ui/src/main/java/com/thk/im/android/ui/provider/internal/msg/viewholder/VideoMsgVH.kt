package com.thk.im.android.ui.provider.internal.msg.viewholder

import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.lifecycle.LifecycleOwner
import com.google.gson.Gson
import com.thk.im.android.base.IMImageLoader
import com.thk.im.android.base.LLog
import com.thk.im.android.base.extension.dp2px
import com.thk.im.android.core.IMCoreManager
import com.thk.im.android.core.IMMsgResourceType
import com.thk.im.android.db.entity.Message
import com.thk.im.android.db.entity.Session
import com.thk.im.android.ui.R
import com.thk.im.android.ui.fragment.adapter.ViewHolderSelect
import com.thk.im.android.ui.fragment.viewholder.BaseMsgVH
import com.thk.im.android.ui.manager.IMVideoMsgBody
import com.thk.im.android.ui.manager.IMVideoMsgData
import com.thk.im.android.ui.protocol.internal.IMMsgVHOperator
import com.thk.im.android.ui.utils.DateUtil

class VideoMsgVH(liftOwner: LifecycleOwner, itemView: View, viewType: Int) :
    BaseMsgVH(liftOwner, itemView, viewType) {


    override fun getContentId(): Int {
        return R.layout.itemview_msg_video
    }

    override fun onViewBind(
        position: Int,
        messages: List<Message>,
        session: Session,
        msgVHOperator: IMMsgVHOperator,
        viewHolderSelect: ViewHolderSelect
    ) {
        super.onViewBind(position, messages, session, msgVHOperator, viewHolderSelect)

        var imagePath = ""
        var width = 0
        var height = 0
        var duration = 0
        if (!message.data.isNullOrEmpty()) {
            val videoMsgData = Gson().fromJson(message.data, IMVideoMsgData::class.java)
            videoMsgData?.let {
                if (it.thumbnailPath != null) {
                    imagePath = it.thumbnailPath!!
                }
                if (it.width != null) {
                    width = it.width!!
                }
                if (it.height != null) {
                    height = it.height!!
                }
                if (it.duration != null) {
                    duration = it.duration!!
                }
            }
        }

        if (!message.content.isNullOrEmpty()) {
            val videoMsgBody = Gson().fromJson(message.content, IMVideoMsgBody::class.java)
            videoMsgBody?.let {
                if (it.width != null) {
                    width = it.width!!
                }
                if (it.height != null) {
                    height = it.height!!
                }
                if (it.duration != null) {
                    duration = it.duration!!
                }
            }
        }
        if (width != 0 && height != 0) {
            setLayoutParams(width, height)
        }
        renderDuration(duration)
        if (imagePath != "") {
            renderThumbnailImage(imagePath)
        } else {
            IMCoreManager.getMessageModule().getMsgProcessor(message.type)
                .downloadMsgContent(message, IMMsgResourceType.Thumbnail.value)
        }
    }

    private fun setLayoutParams(width: Int, height: Int) {
        val container: CardView = itemView.findViewById(R.id.card_msg_container)
        val lp = container.layoutParams
        if (width > height) {
            val calWidth = maxOf(80.dp2px(), minOf(200.dp2px(), width))
            val calHeight = maxOf(calWidth * height / width, 60.dp2px())
            lp.width = calWidth
            lp.height = calHeight
        } else {
            val calHeight = maxOf(80.dp2px(), minOf(200.dp2px(), height))
            val calWidth = maxOf(calHeight * width / height, 60.dp2px())
            lp.width = calWidth
            lp.height = calHeight
        }
        container.layoutParams = lp
        val imageView: ImageView = itemView.findViewById(R.id.iv_msg_video_thumbnail)
        val durationView: TextView = itemView.findViewById(R.id.tv_video_duration)
        imageView.visibility = View.INVISIBLE
        durationView.visibility = View.INVISIBLE
    }


    private fun renderDuration(duration: Int) {
        LLog.d("renderDuration ${duration}")
        val durationView: TextView = itemView.findViewById(R.id.tv_video_duration)
        durationView.visibility = View.VISIBLE
        durationView.text = DateUtil.getDuration(duration)
    }

    private fun renderThumbnailImage(path: String) {
        val imageView: ImageView = itemView.findViewById(R.id.iv_msg_video_thumbnail)
        imageView.visibility = View.VISIBLE
        IMImageLoader.displayImageByPath(imageView, path)
    }

    override fun onViewDetached() {
        super.onViewDetached()
    }
}