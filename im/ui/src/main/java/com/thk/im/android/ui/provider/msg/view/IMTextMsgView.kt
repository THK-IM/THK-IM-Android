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
import androidx.core.content.ContextCompat
import com.thk.im.android.core.IMCoreManager
import com.thk.im.android.core.base.BaseSubscriber
import com.thk.im.android.core.base.RxTransform
import com.thk.im.android.core.db.entity.Message
import com.thk.im.android.core.db.entity.Session
import com.thk.im.android.core.db.entity.User
import com.thk.im.android.ui.R
import com.thk.im.android.ui.databinding.ViewMsgTextBinding
import com.thk.im.android.ui.msg.view.IMsgView
import com.thk.im.android.ui.protocol.internal.IMMsgVHOperator
import io.reactivex.Flowable

class IMTextMsgView : LinearLayout, IMsgView {

    private var binding: ViewMsgTextBinding

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

    override fun setMessage(
        message: Message,
        session: Session?,
        delegate: IMMsgVHOperator?,
        isReply: Boolean
    ) {
        if (isReply) {
            binding.tvMsgContent.textSize = 12.0f
            binding.tvMsgContent.setTextColor(Color.parseColor("#ff999999"))
        } else {
            binding.tvMsgContent.textSize = 16.0f
            binding.tvMsgContent.setTextColor(Color.parseColor("#333333"))
        }
        if (message.data != null) {
            renderMsg(message.data!!)
            return
        }
        if (message.atUsers.isNullOrBlank()) {
            binding.tvMsgContent.text = message.content
        } else {
            val atUsers = message.atUsers!!.split("#")
            if (atUsers.isEmpty()) {
                binding.tvMsgContent.text = message.content
            } else {
                requestAtUsersInfo(message, atUsers)
            }
        }
    }

    override fun contentView(): ViewGroup {
        return this
    }

    private fun requestAtUsersInfo(message: Message, atUsers: List<String>) {
        val uIds = mutableSetOf<Long>()
        try {
            for (atUser in atUsers) {
                uIds.add(atUser.toLong())
            }
            val subscriber = object : BaseSubscriber<String>() {
                override fun onNext(t: String?) {
                    t?.let {
                        renderMsg(it)
                    }
                }
            }
            IMCoreManager.userModule.queryUsers(uIds)
                .compose(RxTransform.flowableToMain())
                .flatMap {
                    val userMap = mutableMapOf<String, User>()
                    for ((k, v) in it) {
                        userMap[k.toString()] = v
                    }
                    val regex = "(?<=@)(.+?)(?=\\s)".toRegex()
                    val body = regex.replace(message.content!!) { result ->
                        return@replace if (userMap[result.value] == null) {
                            ""
                        } else {
                            userMap[result.value]!!.nickname
                        }
                    }
                    return@flatMap Flowable.just(body)
                }
                .subscribe(subscriber)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun renderMsg(content: String) {
        val regex = "(?<=@)(.+?)(?=\\s)".toRegex()
        val sequence = regex.findAll(content)
        val count = sequence.count()
        if (count == 0) {
            binding.tvMsgContent.text = content
            return
        }
        val spannable = SpannableStringBuilder(content)
        sequence.forEach { matchResult ->
            val range = matchResult.range
            val atSpan = ForegroundColorSpan(Color.parseColor("#1390f4"))
            spannable.setSpan(
                atSpan,
                range.first - 1,
                range.last + 1,
                Spanned.SPAN_EXCLUSIVE_INCLUSIVE
            )
        }
        binding.tvMsgContent.text = spannable
    }
}