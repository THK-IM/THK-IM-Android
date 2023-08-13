package com.thk.im.android.ui.viewholder.msg

import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import com.google.gson.Gson
import com.thk.im.android.common.IMImageLoader
import com.thk.im.android.common.extension.dp2px
import com.thk.im.android.core.IMManager
import com.thk.im.android.core.fileloader.LoadListener
import com.thk.im.android.core.processor.body.VideoBody
import com.thk.im.android.db.entity.Message
import com.thk.im.android.db.entity.MsgType
import com.thk.im.android.db.entity.Session
import com.thk.im.android.ui.R
import com.thk.im.android.ui.manager.IMItemViewManager

class VideoMsgVH(liftOwner: LifecycleOwner, itemView: View, viewType: Int) :
    BaseMsgVH(liftOwner, itemView, viewType), View.OnClickListener {


    override fun getContentId(): Int {
        return R.layout.itemview_msg_video
    }

    override fun onViewCreated() {
        super.onViewCreated()
        contentContainer.setOnClickListener(this)
    }

    override fun onViewBind(msg: Message, ses: Session) {
        super.onViewBind(msg, ses)
        val body = Gson().fromJson(msg.content, VideoBody::class.java)
        if (body.path != null && body.path!!.isNotEmpty()) {
            showView(body)
        } else if (!body.url.isNullOrEmpty()) {
            val fileName = body.url!!.substringAfterLast("/", "")
            val localPath =
                IMManager.getStorageModule().allocLocalFilePath(msg.sid, fileName, "video")
            IMManager.getFileLoaderModule().download(body.url!!, localPath, object : LoadListener {

                override fun onProgress(progress: Int, state: Int, url: String, path: String) {
                    if (state == LoadListener.Success) {
                        body.path = path
                        val msgIVProvider =
                            IMItemViewManager.getMsgIVProviderByMsgType(MsgType.VIDEO.value)
                        val content = msgIVProvider.assembleMsgContent(messsage.sid, path)
                        content.let {
                            val assembleBody = Gson().fromJson(content, VideoBody::class.java)
                            body.height = assembleBody.height
                            body.width = assembleBody.width
                            body.thumbnailPath = assembleBody.thumbnailPath
                            body.duration = assembleBody.duration
                            body.ratio = assembleBody.ratio
                            msg.content = Gson().toJson(body)
                            IMManager.getMessageModule().getMessageProcessor(MsgType.VIDEO.value)
                                .updateDb(msg)
                        }
                    }
                }

                override fun notifyOnUiThread(): Boolean {
                    return false
                }

            })
        }
    }

    private fun showView(body: VideoBody) {
        val ivMsgThumbnail: ImageView = contentContainer.findViewById(R.id.iv_msg_video_thumbnail)
        val rlMsgPrompt: RelativeLayout = contentContainer.findViewById(R.id.rl_msg_video_prompt)
        val tvVideoDuration: TextView = contentContainer.findViewById(R.id.tv_video_duration)
        if (body.height != null && body.width != null) {
            val lp = ivMsgThumbnail.layoutParams
            if (body.width!! > body.height!!) {
                var calWidth = minOf(150.dp2px(), body.width!!)
                calWidth = maxOf(60.dp2px(), calWidth)
                var calHeight = maxOf(calWidth * body.height!! / body.width!!, 60.dp2px())
                lp.width = calWidth
                lp.height = calHeight
            } else {
                var calHeight = minOf(150.dp2px(), body.height!!)
                calHeight = maxOf(60.dp2px(), calHeight)
                var calWidth = maxOf(calHeight * body.width!! / body.height!!, 60.dp2px())
                lp.width = calWidth
                lp.height = calHeight
            }
            ivMsgThumbnail.layoutParams = lp
            rlMsgPrompt.layoutParams = lp
        }
        body.thumbnailPath?.let {
            IMImageLoader.displayImageByPath(ivMsgThumbnail, it)
        }

        body.duration?.let {
            tvVideoDuration.text = it.toString()
        }
    }

    override fun onClick(v: View?) {
        
    }
}