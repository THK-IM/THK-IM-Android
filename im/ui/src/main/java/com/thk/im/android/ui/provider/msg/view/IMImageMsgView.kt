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
import com.thk.im.android.core.db.entity.Message
import com.thk.im.android.core.db.entity.Session
import com.thk.im.android.ui.R
import com.thk.im.android.ui.databinding.ViewMsgImageBinding
import com.thk.im.android.ui.fragment.view.IMsgView
import com.thk.im.android.ui.manager.IMImageMsgBody
import com.thk.im.android.ui.manager.IMImageMsgData
import com.thk.im.android.ui.protocol.internal.IMMsgVHOperator

class IMImageMsgView : LinearLayout, IMsgView {

    private var binding: ViewMsgImageBinding

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    init {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.view_msg_image, this, true)
        binding = ViewMsgImageBinding.bind(view)
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
        if (!message.data.isNullOrEmpty()) {
            val imageMsgData = Gson().fromJson(message.data, IMImageMsgData::class.java)
            imageMsgData?.let {
                if (it.thumbnailPath != null) {
                    imagePath = it.thumbnailPath!!
                }
                if (it.width != null && it.width!! > 0) {
                    width = it.width!!
                }
                if (it.height != null && it.height!! > 0) {
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
                if (it.height != null && it.height!! > 0) {
                    height = it.height!!
                }
            }
        }

        if (width != 0 && height != 0) {
            setLayoutParams(width, height, isReply)
        }
        if (imagePath != "") {
            renderImage(imagePath)
        } else {
            IMCoreManager.messageModule.getMsgProcessor(message.type)
                .downloadMsgContent(message, IMMsgResourceType.Thumbnail.value)
        }
    }


    private fun setLayoutParams(width: Int, height: Int, isReply: Boolean) {
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
        if (isReply) {
            lp.width /= 4
            lp.height /= 4
        }
        binding.cardMsgContainer.layoutParams = lp
        binding.ivMsgContent.visibility = View.INVISIBLE
    }

    private fun renderImage(path: String) {
        binding.ivMsgContent.visibility = View.VISIBLE
        IMImageLoader.displayImageByPath(binding.ivMsgContent, path)
    }

}