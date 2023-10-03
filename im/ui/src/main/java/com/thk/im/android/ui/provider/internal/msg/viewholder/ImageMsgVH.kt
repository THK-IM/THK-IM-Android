package com.thk.im.android.ui.provider.internal.msg.viewholder

import android.view.View
import android.widget.ImageView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.google.gson.Gson
import com.thk.im.android.base.IMImageLoader
import com.thk.im.android.base.extension.dp2px
import com.thk.im.android.core.IMCoreManager
import com.thk.im.android.core.IMEvent
import com.thk.im.android.core.IMLoadProgress
import com.thk.im.android.core.IMMsgResourceType
import com.thk.im.android.core.event.XEventBus
import com.thk.im.android.db.entity.Message
import com.thk.im.android.db.entity.Session
import com.thk.im.android.ui.R
import com.thk.im.android.ui.fragment.viewholder.BaseMsgVH
import com.thk.im.android.ui.manager.IMImageMsgBody
import com.thk.im.android.ui.manager.IMImageMsgData
import com.thk.im.android.ui.protocol.IMMsgVHOperator

class ImageMsgVH(liftOwner: LifecycleOwner, itemView: View, viewType: Int) :
    BaseMsgVH(liftOwner, itemView, viewType), Observer<IMLoadProgress> {

    override fun getContentId(): Int {
        return R.layout.itemview_msg_image
    }

    override fun onViewBind(
        position: Int,
        messages: List<Message>,
        session: Session,
        msgVHOperator: IMMsgVHOperator
    ) {
        super.onViewBind(position, messages, session, msgVHOperator)
        XEventBus.observe(IMEvent.MsgLoadStatusUpdate.value, this)

        if (!message.data.isNullOrEmpty()) {
            val imageMsgData = Gson().fromJson(message.data, IMImageMsgData::class.java)
            imageMsgData?.let {
                renderData(it)
                return@onViewBind
            }
        }

        if (!message.content.isNullOrEmpty()) {
            val imageMsgBody = Gson().fromJson(message.content, IMImageMsgBody::class.java)
            imageMsgBody?.let {
                renderBody(it)
                // 开始下载
                if (!it.thumbnailUrl.isNullOrEmpty()) {
                    IMCoreManager.getMessageModule().getMsgProcessor(message.type)
                        .downloadMsgContent(message, IMMsgResourceType.Thumbnail.value)
                }
            }
        }
    }

    private fun renderData(imageMsgData: IMImageMsgData) {
        if (imageMsgData.width != null && imageMsgData.height != null) {
            setLayoutParams(imageMsgData.width!!, imageMsgData.height!!)
        }
        if (imageMsgData.thumbnailPath != null) {
            renderImage(imageMsgData.thumbnailPath!!)
        }
    }

    private fun renderBody(imageMsgBody: IMImageMsgBody) {
        if (imageMsgBody.width != null && imageMsgBody.height != null) {
            setLayoutParams(imageMsgBody.width!!, imageMsgBody.height!!)
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

    private fun renderImage(path: String) {
        val imageView: ImageView = contentContainer.findViewById(R.id.iv_msg_content)
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