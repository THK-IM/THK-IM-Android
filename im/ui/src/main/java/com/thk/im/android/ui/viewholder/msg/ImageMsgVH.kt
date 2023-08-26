package com.thk.im.android.ui.viewholder.msg

import android.view.View
import android.widget.ImageView
import androidx.lifecycle.LifecycleOwner
import com.google.gson.Gson
import com.thk.im.android.base.IMImageLoader
import com.thk.im.android.base.extension.dp2px
import com.thk.im.android.core.IMCoreManager
import com.thk.im.android.core.fileloader.LoadListener
import com.thk.im.android.core.processor.body.ImageBody
import com.thk.im.android.db.MsgType
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


    override fun onViewBind(msg: Message, ses: Session) {
        super.onViewBind(msg, ses)
        val ivMsgContent: ImageView = contentContainer.findViewById(R.id.iv_msg_content)
        val body = Gson().fromJson(msg.content, ImageBody::class.java)
        val lp = ivMsgContent.layoutParams
        if (body.width > body.height) {
            var calWidth = minOf(150.dp2px(), body.width)
            calWidth = maxOf(60.dp2px(), calWidth)
            var calHeight = maxOf(calWidth * body.height / body.width, 60.dp2px())
            lp.width = calWidth
            lp.height = calHeight
        } else {
            var calHeight = minOf(150.dp2px(), body.height)
            calHeight = maxOf(60.dp2px(), calHeight)
            var calWidth = maxOf(calHeight * body.width / body.height, 60.dp2px())
            lp.width = calWidth
            lp.height = calHeight
        }
        ivMsgContent.layoutParams = lp
        if (body.path != null && body.path!!.isNotEmpty()) {
            IMImageLoader.displayImageByPath(ivMsgContent, body.path!!)
        } else if (!body.url.isNullOrEmpty()) {
            val fileName = body.url!!.substringAfterLast("/", "")
            val localPath =
                IMCoreManager.getStorageModule().allocLocalFilePath(msg.sid, fileName, "img")
            IMCoreManager.getFileLoaderModule().download(body.url!!, localPath, object : LoadListener {

                override fun onProgress(progress: Int, state: Int, url: String, path: String) {
                    if (state == LoadListener.Success) {
                        body.path = path
                        msg.content = Gson().toJson(body)
                        IMCoreManager.getMessageModule()
                            .getMsgProcessor(MsgType.IMAGE.value)
                            .updateDb(msg)
                    }
                }

                override fun notifyOnUiThread(): Boolean {
                    return false
                }

            })
        }
    }

    override fun onClick(v: View?) {

    }
}