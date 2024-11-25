package com.thk.im.android.module.msg.call

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import com.google.gson.Gson
import com.thk.im.android.R
import com.thk.im.android.core.IMCoreManager
import com.thk.im.android.core.db.entity.Message
import com.thk.im.android.core.db.entity.Session
import com.thk.im.android.databinding.ViewMsgCallBinding
import com.thk.im.android.live.RoomMode
import com.thk.im.android.ui.fragment.view.IMsgBodyView
import com.thk.im.android.ui.manager.IMMsgPosType
import com.thk.im.android.ui.protocol.internal.IMMsgVHOperator

class IMCallMsgView : LinearLayout, IMsgBodyView {

    private var binding: ViewMsgCallBinding

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    init {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.view_msg_call, this, true)
        binding = ViewMsgCallBinding.bind(view)
    }

    override fun setPosition(position: IMMsgPosType) {
    }

    override fun setMessage(
        message: Message,
        session: Session?,
        delegate: IMMsgVHOperator?
    ) {
        val callMsg = Gson().fromJson(message.content, IMCallMsg::class.java)
        if (callMsg.roomMode == RoomMode.Audio.value) {
            binding.ivCallType.setImageResource(R.drawable.ic_audio_call)
        } else {
            binding.ivCallType.setImageResource(R.drawable.ic_video_call)
        }

        if (callMsg.accepted == 2) {
            binding.tvCallMsg.text = "通话时长: ${callMsg.duration / 1000}秒"
        } else if (callMsg.accepted == 1) {
            if (callMsg.roomOwnerId != IMCoreManager.uId) {
                binding.tvCallMsg.text = "已挂断"
            } else {
                binding.tvCallMsg.text = "对方已拒绝"
            }
        } else {
            if (callMsg.roomOwnerId != IMCoreManager.uId) {
                binding.tvCallMsg.text = "未接听"
            } else {
                binding.tvCallMsg.text = "对方未接听"
            }
        }
    }

    override fun contentView(): ViewGroup {
        return this
    }
}