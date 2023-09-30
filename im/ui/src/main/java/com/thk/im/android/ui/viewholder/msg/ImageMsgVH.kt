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
        XEventBus.unObserve(IMEvent.MsgLoadStatusUpdate.value, this)
        XEventBus.observe(IMEvent.MsgLoadStatusUpdate.value, this)
        if (message.data.isNotEmpty() && message.data.isNotBlank()) {
            val imageMsgData = Gson().fromJson(message.data, IMImageMsgData::class.java)
            imageMsgData?.let { data ->
                if (data.thumbnailPath != null && data.width != null && data.height != null) {
                    setLayoutParams(data.width!!, data.height!!)
                    renderImageData(data)
                    return@onViewBind
                }
            }
        }
        if (message.content.isNotEmpty() && message.content.isNotBlank()) {
            val imageMsgBody = Gson().fromJson(message.content, IMImageMsgBody::class.java)
            if (imageMsgBody.width != null && imageMsgBody.height != null) {
                setLayoutParams(imageMsgBody.width!!, imageMsgBody.height!!)
            }
            if (!imageMsgBody.thumbnailUrl.isNullOrEmpty()) {
                IMCoreManager.getMessageModule().getMsgProcessor(message.type)
                    .downloadMsgContent(message, IMMsgResourceType.Thumbnail.value)
            }
        }
    }

    private fun setLayoutParams(width: Int, height: Int) {
        val imageView: ImageView = contentContainer.findViewById(R.id.iv_msg_content)
        val lp = imageView.layoutParams
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
        imageView.layoutParams = lp
        imageView.visibility = View.INVISIBLE
    }

    private fun renderImageData(imageMsgData: IMImageMsgData) {
        imageMsgData.thumbnailPath?.let {
            val imageView: ImageView = contentContainer.findViewById(R.id.iv_msg_content)
            imageView.visibility = View.VISIBLE
            IMImageLoader.displayImageByPath(imageView, it)
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