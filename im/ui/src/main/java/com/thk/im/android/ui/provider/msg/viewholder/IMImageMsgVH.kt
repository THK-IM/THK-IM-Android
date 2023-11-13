package com.thk.im.android.ui.provider.msg.viewholder

import android.view.View
import android.widget.ImageView
import androidx.cardview.widget.CardView
import androidx.lifecycle.LifecycleOwner
import com.google.gson.Gson
import com.thk.im.android.base.IMImageLoader
import com.thk.im.android.base.extension.dp2px
import com.thk.im.android.core.IMCoreManager
import com.thk.im.android.core.IMMsgResourceType
import com.thk.im.android.db.entity.Message
import com.thk.im.android.db.entity.Session
import com.thk.im.android.ui.R
import com.thk.im.android.ui.fragment.viewholder.BaseMsgVH
import com.thk.im.android.ui.manager.IMImageMsgBody
import com.thk.im.android.ui.manager.IMImageMsgData
import com.thk.im.android.ui.protocol.internal.IMMsgVHOperator

class IMImageMsgVH(liftOwner: LifecycleOwner, itemView: View, viewType: Int) :
    BaseMsgVH(liftOwner, itemView, viewType) {

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
        var imagePath = ""
        var width = 0
        var height = 0
        if (!message.data.isNullOrEmpty()) {
            val imageMsgData = Gson().fromJson(message.data, IMImageMsgData::class.java)
            imageMsgData?.let {
                if (it.thumbnailPath != null) {
                    imagePath = it.thumbnailPath!!
                }
                if (it.width != null && it.width!! > 0) {
                    width = it.width!!
                }
                if (it.height != null&& it.height!! > 0) {
                    height = it.height!!
                }
            }
        }

        if (!message.content.isNullOrEmpty()) {
            val imageMsgBody = Gson().fromJson(message.content, IMImageMsgBody::class.java)
            imageMsgBody?.let {
                if (it.width != null && it.width!! > 0) {
                    width = it.width!!
                }
                if (it.height != null&& it.height!! > 0) {
                    height = it.height!!
                }
            }
        }

        if (width != 0 && height != 0) {
            setLayoutParams(width, height)
        }
        if (imagePath != "") {
            renderImage(imagePath)
        } else {
            IMCoreManager.getMessageModule().getMsgProcessor(message.type)
                .downloadMsgContent(message, IMMsgResourceType.Thumbnail.value)
        }
    }

    private fun setLayoutParams(width: Int, height: Int) {
        val container: CardView = itemView.findViewById(R.id.card_msg_container)
        val imageView: ImageView = itemView.findViewById(R.id.iv_msg_content)
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
        imageView.visibility = View.INVISIBLE
    }

    private fun renderImage(path: String) {
        val imageView: ImageView = itemView.findViewById(R.id.iv_msg_content)
        imageView.visibility = View.VISIBLE
        IMImageLoader.displayImageByPath(imageView, path)
    }

    override fun onViewDetached() {
        super.onViewDetached()
    }

}