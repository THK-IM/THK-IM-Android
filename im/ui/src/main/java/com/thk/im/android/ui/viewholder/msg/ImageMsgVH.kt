package com.thk.im.android.ui.viewholder.msg

import android.view.View
import android.widget.ImageView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.google.gson.Gson
import com.thk.im.android.base.IMImageLoader
import com.thk.im.android.base.LLog
import com.thk.im.android.base.extension.dp2px
import com.thk.im.android.core.IMCoreManager
import com.thk.im.android.core.IMEvent
import com.thk.im.android.core.IMImageMsgBody
import com.thk.im.android.core.IMImageMsgData
import com.thk.im.android.core.IMLoadProgress
import com.thk.im.android.core.IMMsgResourceType
import com.thk.im.android.core.event.XEventBus
import com.thk.im.android.db.MsgType
import com.thk.im.android.db.entity.Message
import com.thk.im.android.db.entity.Session
import com.thk.im.android.ui.R

class ImageMsgVH(liftOwner: LifecycleOwner, itemView: View, viewType: Int) :
    BaseMsgVH(liftOwner, itemView, viewType), Observer<IMLoadProgress> {

    override fun getContentId(): Int {
        return R.layout.itemview_msg_image
    }

    override fun onViewBind(message: Message, session: Session) {
        super.onViewBind(message, session)
        LLog.v("onViewBind")
        XEventBus.observe(IMEvent.MsgLoadStatusUpdate.value, this)
        if (message.type != MsgType.IMAGE.value) {
            return
        }
        if (message.data.isNotEmpty() && message.data.isNotBlank()) {
            val imageMsgData = Gson().fromJson(message.data, IMImageMsgData::class.java)
            imageMsgData?.let { data ->
                if (data.thumbnailPath != null && data.width != null && data.height != null) {
                    renderImageData(data)
                    return@onViewBind
                }
            }
        }
        if (message.content.isNotEmpty() && message.content.isNotBlank()) {
            val imageMsgBody = Gson().fromJson(message.content, IMImageMsgBody::class.java)
            if (!imageMsgBody.thumbnailUrl.isNullOrEmpty()) {
                IMCoreManager.getMessageModule().getMsgProcessor(message.type)
                    .downloadMsgContent(message, IMMsgResourceType.Thumbnail.value)
            }
        }
    }

    private fun renderImageData(imageMsgData: IMImageMsgData) {
        val width = imageMsgData.width!!
        val height = imageMsgData.height!!
        val path = imageMsgData.thumbnailPath!!
        val imageView: ImageView = contentContainer.findViewById(R.id.iv_msg_content)
        val lp = imageView.layoutParams
        if (width > height) {
            var calWidth = minOf(150.dp2px(), width)
            calWidth = maxOf(60.dp2px(), calWidth)
            val calHeight = maxOf(calWidth * height / width, 60.dp2px())
            lp.width = calWidth
            lp.height = calHeight
        } else {
            var calHeight = minOf(150.dp2px(), height)
            calHeight = maxOf(60.dp2px(), calHeight)
            val calWidth = maxOf(calHeight * width / height, 60.dp2px())
            lp.width = calWidth
            lp.height = calHeight
        }
        IMImageLoader.displayImageByPath(imageView, path)
    }

    override fun onViewRecycled() {
        super.onViewRecycled()
        LLog.v("onViewRecycled")
        XEventBus.unObserve(IMEvent.MsgLoadStatusUpdate.value, this)
    }

    override fun onChanged(t: IMLoadProgress?) {
        t?.let {
            LLog.v("IMLoadProgress ${it.type} ${it.state} ${it.progress} ${it.key}")
        }
    }

}