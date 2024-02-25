package com.thk.im.android.ui.provider.msg.viewholder

import android.graphics.Color
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.View
import androidx.core.content.ContextCompat
import androidx.emoji2.widget.EmojiTextView
import androidx.lifecycle.LifecycleOwner
import com.thk.im.android.core.IMCoreManager
import com.thk.im.android.core.base.BaseSubscriber
import com.thk.im.android.core.base.RxTransform
import com.thk.im.android.core.db.entity.Message
import com.thk.im.android.core.db.entity.Session
import com.thk.im.android.core.db.entity.User
import com.thk.im.android.ui.R
import com.thk.im.android.ui.fragment.viewholder.BaseMsgVH
import com.thk.im.android.ui.protocol.internal.IMMsgVHOperator
import io.reactivex.Flowable

class IMTextMsgVH(liftOwner: LifecycleOwner, itemView: View, viewType: Int) :
    BaseMsgVH(liftOwner, itemView, viewType) {


    override fun getContentId(): Int {
        return R.layout.itemview_msg_text
    }

    override fun onViewBind(
        position: Int,
        messages: List<Message>,
        session: Session,
        msgVHOperator: IMMsgVHOperator
    ) {
        super.onViewBind(position, messages, session, msgVHOperator)
        val tvMsgContent: EmojiTextView = itemView.findViewById(R.id.tv_msg_content)
        tvMsgContent.setTextColor(
            ContextCompat.getColor(
                tvMsgContent.context,
                R.color.font_main
            )
        )
        if (this.message.data != null) {
            renderMsg(this.message.data!!)
            return
        }
        if (this.message.atUsers.isNullOrBlank()) {
            tvMsgContent.text = message.content
        } else {
            val atUsers = this.message.atUsers!!.split("#")
            if (atUsers.isEmpty()) {
                tvMsgContent.text = message.content
            } else {
                requestAtUsersInfo(atUsers)
            }
        }
    }

    private fun requestAtUsersInfo(atUsers: List<String>) {
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
        val tvMsgContent: EmojiTextView = itemView.findViewById(R.id.tv_msg_content)
        val regex = "(?<=@)(.+?)(?=\\s)".toRegex()
        val sequence = regex.findAll(content)
        val count = sequence.count()
        if (count == 0) {
            tvMsgContent.text = content
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
        tvMsgContent.text = spannable
    }
}