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
import com.thk.im.android.core.MsgType
import com.thk.im.android.core.db.entity.Message
import com.thk.im.android.core.db.entity.Session
import com.thk.im.android.ui.R
import com.thk.im.android.ui.databinding.ViewMsgRevokeBinding
import com.thk.im.android.ui.fragment.view.IMsgBodyView
import com.thk.im.android.ui.manager.IMRevokeMsgData
import com.thk.im.android.ui.protocol.internal.IMMsgVHOperator

class IMRevokeMsgView : LinearLayout, IMsgBodyView {

    private var binding: ViewMsgRevokeBinding

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    init {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.view_msg_revoke, this, true)
        binding = ViewMsgRevokeBinding.bind(view)
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
        if (isReply) {
            binding.tvReedit.textSize = 12.0f
            binding.tvReedit.setTextColor(Color.parseColor("#ff999999"))
            binding.tvWhoRevoke.textSize = 12.0f
            binding.tvWhoRevoke.setTextColor(Color.parseColor("#ff999999"))
        }
        val revokeData = Gson().fromJson(message.data, IMRevokeMsgData::class.java)
        if (revokeData != null) {
            binding.tvWhoRevoke.text = "${revokeData.nick}撤回了一条消息"
            if (message.fUid == IMCoreManager.uId
                && revokeData.content != null
                && revokeData.type != null && revokeData.type == MsgType.Text.value
            ) {
                binding.tvReedit.visibility = View.VISIBLE
                binding.tvReedit.isClickable = true
                binding.tvReedit.setOnClickListener {
                    delegate?.msgSender()?.addInputContent(revokeData.content!!)
                    delegate?.msgSender()?.openKeyboard()
                }
            } else {
                binding.tvReedit.visibility = View.GONE
            }
        } else {
            binding.tvWhoRevoke.text = "对方撤回了一条消息"
            binding.tvReedit.visibility = View.GONE
        }
    }


}