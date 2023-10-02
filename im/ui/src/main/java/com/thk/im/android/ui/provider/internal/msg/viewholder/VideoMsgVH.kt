package com.thk.im.android.ui.provider.internal.msg.viewholder

import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.google.gson.Gson
import com.thk.im.android.base.IMImageLoader
import com.thk.im.android.base.extension.dp2px
import com.thk.im.android.core.IMCoreManager
import com.thk.im.android.core.IMEvent
import com.thk.im.android.core.IMLoadProgress
import com.thk.im.android.core.IMMsgResourceType
import com.thk.im.android.core.IMVideoMsgBody
import com.thk.im.android.core.IMVideoMsgData
import com.thk.im.android.core.event.XEventBus
import com.thk.im.android.db.entity.Message
import com.thk.im.android.db.entity.Session
import com.thk.im.android.ui.R
import com.thk.im.android.ui.fragment.viewholder.BaseMsgVH
import com.thk.im.android.ui.utils.DateUtil

class VideoMsgVH(liftOwner: LifecycleOwner, itemView: View, viewType: Int) :
    BaseMsgVH(liftOwner, itemView, viewType), Observer<IMLoadProgress> {


    override fun getContentId(): Int {
        return R.layout.itemview_msg_video
    }

    override fun onViewBind(message: Message, session: Session) {
        super.onViewBind(message, session)
        XEventBus.observe(IMEvent.MsgLoadStatusUpdate.value, this)

        if (message.data.isNotEmpty() && message.data.isNotBlank()) {
            val videoMsgData = Gson().fromJson(message.data, IMVideoMsgData::class.java)
            videoMsgData?.let {
                renderData(it)
                return@onViewBind
            }
        }

        if (message.content.isNotEmpty() && message.content.isNotBlank()) {
            val videoMsgBody = Gson().fromJson(message.content, IMVideoMsgBody::class.java)
            videoMsgBody?.let {
                renderBody(it)
                // 开始下载
                if (it.thumbnailUrl.isNullOrEmpty()) {
                    IMCoreManager.getMessageModule().getMsgProcessor(message.type)
                        .downloadMsgContent(message, IMMsgResourceType.Thumbnail.value)
                }
            }
        }
    }

    private fun renderData(videoMsgData: IMVideoMsgData) {
        if (videoMsgData.width != null && videoMsgData.height != null) {
            setLayoutParams(videoMsgData.width!!, videoMsgData.height!!)
        }
        if (videoMsgData.thumbnailPath != null) {
            renderThumbnailImage(videoMsgData.thumbnailPath!!)
        }
        if (videoMsgData.duration != null) {
            renderDuration(videoMsgData.duration!!)
        }
    }

    private fun renderBody(videoMsgBody: IMVideoMsgBody) {
        if (videoMsgBody.width != null && videoMsgBody.height != null) {
            setLayoutParams(videoMsgBody.width!!, videoMsgBody.height!!)
        }
        if (videoMsgBody.duration != null) {
            renderDuration(videoMsgBody.duration!!)
        }
    }

    private fun setLayoutParams(width: Int, height: Int) {
        val contentView: RelativeLayout = contentContainer.findViewById(R.id.rl_msg_video_content)
        val lp = contentView.layoutParams
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
        contentView.layoutParams = lp
        val imageView: ImageView = contentContainer.findViewById(R.id.iv_msg_video_thumbnail)
        val durationView: TextView = contentContainer.findViewById(R.id.tv_video_duration)
        imageView.visibility = View.INVISIBLE
        durationView.visibility = View.INVISIBLE
    }


    private fun renderDuration(duration: Int) {
        val durationView: TextView = contentContainer.findViewById(R.id.tv_video_duration)
        durationView.visibility = View.VISIBLE
        durationView.text = DateUtil.getDuration(duration)
    }

    private fun renderThumbnailImage(path: String) {
        val imageView: ImageView = contentContainer.findViewById(R.id.iv_msg_video_thumbnail)
        imageView.visibility = View.VISIBLE
        IMImageLoader.displayImageByPath(imageView, path)
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