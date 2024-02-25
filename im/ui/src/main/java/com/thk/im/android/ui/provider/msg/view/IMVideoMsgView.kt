package com.thk.im.android.ui.provider.msg.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.google.gson.Gson
import com.thk.im.android.core.IMCoreManager
import com.thk.im.android.core.IMMsgResourceType
import com.thk.im.android.core.base.IMImageLoader
import com.thk.im.android.core.base.extension.dp2px
import com.thk.im.android.core.base.utils.DateUtils
import com.thk.im.android.core.db.entity.Message
import com.thk.im.android.core.db.entity.Session
import com.thk.im.android.ui.R
import com.thk.im.android.ui.databinding.ItemviewMsgVideoBinding
import com.thk.im.android.ui.manager.IMVideoMsgBody
import com.thk.im.android.ui.manager.IMVideoMsgData
import com.thk.im.android.ui.msg.view.IMsgView
import com.thk.im.android.ui.protocol.internal.IMMsgVHOperator

class IMVideoMsgView : LinearLayout, IMsgView {

    private var binding: ItemviewMsgVideoBinding

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    init {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.itemview_msg_video, this, true)
        binding = ItemviewMsgVideoBinding.bind(view)
    }

    override fun contentView(): ViewGroup {
        return this
    }

    override fun setMessage(
        message: Message,
        session: Session?,
        delegate: IMMsgVHOperator?,
        isReply: Boolean
    ) {

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
            IMCoreManager.messageModule.getMsgProcessor(message.type)
                .downloadMsgContent(message, IMMsgResourceType.Thumbnail.value)
        }
    }

    private fun setLayoutParams(width: Int, height: Int) {
        val lp = binding.cardMsgContainer.layoutParams
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
        binding.cardMsgContainer.layoutParams = lp
        binding.ivMsgVideoThumbnail.visibility = View.INVISIBLE
        binding.tvVideoDuration.visibility = View.INVISIBLE
    }


    private fun renderDuration(duration: Int) {
        binding.tvVideoDuration.visibility = View.VISIBLE
        binding.tvVideoDuration.text = DateUtils.secondToDuration(duration)
    }

    private fun renderThumbnailImage(path: String) {
        binding.ivMsgVideoThumbnail.visibility = View.VISIBLE
        IMImageLoader.displayImageByPath(binding.ivMsgVideoThumbnail, path)
    }
}