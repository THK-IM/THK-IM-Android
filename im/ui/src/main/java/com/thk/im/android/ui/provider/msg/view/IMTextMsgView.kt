package com.thk.im.android.ui.provider.msg.view

import android.content.Context
import android.graphics.Color
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import com.thk.im.android.core.MsgOperateStatus
import com.thk.im.android.core.db.entity.Message
import com.thk.im.android.core.db.entity.Session
import com.thk.im.android.ui.R
import com.thk.im.android.ui.databinding.ViewMsgTextBinding
import com.thk.im.android.ui.fragment.view.IMsgBodyView
import com.thk.im.android.ui.manager.IMMsgPosType
import com.thk.im.android.ui.manager.IMUIManager
import com.thk.im.android.ui.protocol.internal.IMMsgVHOperator
import com.thk.im.android.ui.utils.AtStringUtils
import java.lang.ref.WeakReference

class IMTextMsgView : LinearLayout, IMsgBodyView {

    private var binding: ViewMsgTextBinding

    private var delegate: WeakReference<IMMsgVHOperator>? = null

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    init {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.view_msg_text, this, true)
        binding = ViewMsgTextBinding.bind(view)
    }

    private var position = IMMsgPosType.Left
    override fun setPosition(position: IMMsgPosType) {
        this.position = position
    }

    override fun setMessage(
        message: Message,
        session: Session?,
        delegate: IMMsgVHOperator?
    ) {
        this.delegate = WeakReference(delegate)
        when (this.position) {
            IMMsgPosType.Reply -> {
                binding.tvMsgContent.setPadding(0, 0, 0, 0)
                binding.tvMsgContent.textSize = 12.0f
                binding.tvMsgContent.maxLines = 3
                binding.tvMsgContent.ellipsize = android.text.TextUtils.TruncateAt.END
                binding.tvMsgContent.setTextColor(Color.parseColor("#0A0E10"))
            }
            IMMsgPosType.Mid -> {
                binding.tvMsgContent.textSize = 12.0f
                binding.tvMsgContent.setTextColor(Color.parseColor("#FFFFFF"))
            }
            else -> {
                binding.tvMsgContent.textSize = 16.0f
                binding.tvMsgContent.setTextColor(Color.parseColor("#0A0E10"))
            }
        }
        var content = message.content ?: return
        if (!message.atUsers.isNullOrBlank()) {
            content = replaceIdToNickname(content, message.getAtUIds())
        }
        val updated = message.oprStatus.and(MsgOperateStatus.Update.value) != 0
        render(content, updated)
    }


    override fun contentView(): ViewGroup {
        return this
    }

    private fun replaceIdToNickname(content: String, atUIds: Set<Long>): String {
        return AtStringUtils.replaceAtUIdsToNickname(content, atUIds) { id ->
            val sender = delegate?.get()?.msgSender() ?: return@replaceAtUIdsToNickname "$id"
            val info = sender.syncGetSessionMemberInfo(id) ?: return@replaceAtUIdsToNickname "$id"
            return@replaceAtUIdsToNickname IMUIManager.nicknameForSessionMember(
                info.first,
                info.second
            )
        }
    }

    private fun render(content: String, updated: Boolean) {
        val regex = AtStringUtils.atRegex
        val sequence = regex.findAll(content)
        val highlightColor =
            IMUIManager.uiResourceProvider?.tintColor() ?: Color.parseColor("#1390f4")
        val contentSpannable = SpannableStringBuilder(content)
        sequence.forEach { matchResult ->
            val range = matchResult.range
            val atSpan = ForegroundColorSpan(highlightColor)
            contentSpannable.setSpan(
                atSpan,
                range.first - 1,
                range.last + 1,
                Spanned.SPAN_EXCLUSIVE_INCLUSIVE
            )
        }
        if (updated) {
            val editStr = context.getString(R.string.edited)
            val editSpan = ForegroundColorSpan(Color.parseColor("#999999"))
            val editSpannable = SpannableStringBuilder(editStr)
            editSpannable.setSpan(editSpan, 0, editStr.length, Spanned.SPAN_EXCLUSIVE_INCLUSIVE)
            contentSpannable.append(editSpannable)
        }
        binding.tvMsgContent.text = contentSpannable
    }
}