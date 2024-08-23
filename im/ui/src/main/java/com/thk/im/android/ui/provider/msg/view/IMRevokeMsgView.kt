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
import com.thk.im.android.core.db.entity.Message
import com.thk.im.android.core.db.entity.Session
import com.thk.im.android.ui.R
import com.thk.im.android.ui.databinding.ViewMsgRevokeBinding
import com.thk.im.android.ui.fragment.view.IMsgBodyView
import com.thk.im.android.ui.manager.IMRevokeMsgData
import com.thk.im.android.ui.manager.IMUIManager
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
        val reeditColor = IMUIManager.uiResourceProvider?.tintColor() ?: Color.parseColor("#1988f0")
        binding.tvReedit.setTextColor(reeditColor)
    }

    override fun contentView(): ViewGroup {
        return this
    }

    override fun setMessage(
        positionType: Int,
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
            if (message.fUid == IMCoreManager.uId) {
                binding.tvReedit.visibility = View.VISIBLE
                binding.tvReedit.isClickable = true
                binding.tvReedit.setOnClickListener {
                    revokeData.content?.let { content ->
                        delegate?.msgSender()?.addInputContent(content)
                        delegate?.msgSender()?.openKeyboard()
                    }
                }
                binding.tvWhoRevoke.text = context.getString(R.string.you_revoke_a_message)
            } else {
                binding.tvWhoRevoke.text = String.format(
                    context.getString(R.string.x_revoke_a_msg),
                    revokeData.nick
                )
                binding.tvReedit.visibility = View.GONE
            }
        } else {
            binding.tvWhoRevoke.text = String.format(
                context.getString(R.string.x_revoke_a_msg),
                context.getString(R.string.other_side),
            )
            binding.tvReedit.visibility = View.GONE
        }
    }


}