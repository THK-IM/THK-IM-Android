package com.thk.im.android.ui.provider.msg.view

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.google.gson.Gson
import com.thk.im.android.core.IMCoreManager
import com.thk.im.android.core.IMMsgResourceType
import com.thk.im.android.core.MsgOperateStatus
import com.thk.im.android.core.base.utils.DateUtils
import com.thk.im.android.core.db.entity.Message
import com.thk.im.android.core.db.entity.Session
import com.thk.im.android.ui.R
import com.thk.im.android.ui.databinding.ViewMsgAudioBinding
import com.thk.im.android.ui.fragment.view.IMsgBodyView
import com.thk.im.android.ui.manager.IMAudioMsgBody
import com.thk.im.android.ui.manager.IMAudioMsgData
import com.thk.im.android.ui.protocol.internal.IMMsgVHOperator

class IMAudioMsgView : LinearLayout, IMsgBodyView {

    private var binding: ViewMsgAudioBinding

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    init {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.view_msg_audio, this, true)
        binding = ViewMsgAudioBinding.bind(view)
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
        var duration = 0
        var path: String? = null
        val played = (message.fUid == IMCoreManager.uId) ||
                (message.oprStatus.and(MsgOperateStatus.ClientRead.value) != 0)
        if (!message.data.isNullOrEmpty()) {
            val audioMsgData = Gson().fromJson(message.data, IMAudioMsgData::class.java)
            audioMsgData?.let {
                if (it.duration != null) {
                    duration = it.duration!!
                }
                if (it.path != null) {
                    path = it.path!!
                }
            }
        }

        if (!message.content.isNullOrEmpty()) {
            val audioMsgBody = Gson().fromJson(message.content, IMAudioMsgBody::class.java)
            audioMsgBody?.let {
                if (it.duration != null) {
                    duration = it.duration!!
                }
            }
        }

        render(duration, played)
        if (path == null) {
            IMCoreManager.messageModule.getMsgProcessor(message.type)
                .downloadMsgContent(message, IMMsgResourceType.Thumbnail.value)
        }

        if (isReply) {
            binding.tvAudioDuration.textSize = 12.0f
            binding.tvAudioDuration.setTextColor(Color.parseColor("#ff999999"))
        } else {
            binding.tvAudioDuration.textSize = 16.0f
            binding.tvAudioDuration.setTextColor(Color.BLACK)
        }
    }

    private fun render(seconds: Int, played: Boolean) {
        binding.tvAudioDuration.text = DateUtils.secondToDuration(seconds)
        if (played) {
            binding.ivAudioStatus.visibility = View.GONE
        } else {
            binding.ivAudioStatus.visibility = View.VISIBLE
        }
    }
}