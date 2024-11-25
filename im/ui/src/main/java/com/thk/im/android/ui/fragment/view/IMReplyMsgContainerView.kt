package com.thk.im.android.ui.fragment.view

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.core.view.children
import com.thk.im.android.core.IMCoreManager
import com.thk.im.android.core.base.BaseSubscriber
import com.thk.im.android.core.base.RxTransform
import com.thk.im.android.core.base.extension.setShape
import com.thk.im.android.core.db.entity.Message
import com.thk.im.android.core.db.entity.Session
import com.thk.im.android.core.db.entity.User
import com.thk.im.android.ui.R
import com.thk.im.android.ui.databinding.ViewReplyMsgContainerBinding
import com.thk.im.android.ui.manager.IMMsgPosType
import com.thk.im.android.ui.manager.IMUIManager
import com.thk.im.android.ui.protocol.internal.IMMsgVHOperator
import io.reactivex.disposables.CompositeDisposable

open class IMReplyMsgContainerView : LinearLayout {

    private val binding: ViewReplyMsgContainerBinding
    private var replyBodyView: IMsgBodyView? = null
    private val disposables = CompositeDisposable()

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int)
            : super(context, attrs, defStyleAttr)

    init {
        val view =
            LayoutInflater.from(context).inflate(R.layout.view_reply_msg_container, this, true)
        binding = ViewReplyMsgContainerBinding.bind(view)
        val color = IMUIManager.uiResourceProvider?.tintColor() ?: Color.parseColor("#08AAFF")
        binding.tvReplyMsgUserNick.setTextColor(color)
        binding.vReplyMsgLine.setShape(color, floatArrayOf(2f, 2f, 2f, 2f), false)
    }

    fun setMessage(
        message: Message,
        session: Session,
        delegate: IMMsgVHOperator?
    ) {
        binding.flReplyContent.children.forEach {
            binding.flReplyContent.removeView(it)
        }
        val view = IMUIManager.getMsgIVProviderByMsgType(message.type).msgBodyView(context, IMMsgPosType.Reply)
        binding.flReplyContent.addView(view.contentView())
        view.setMessage(message, session, delegate)
        replyBodyView = view

        val userInfo =
            delegate?.msgSender()?.syncGetSessionMemberInfo(message.fUid)
        userInfo?.let { info ->
            val nickname = IMUIManager.nicknameForSessionMember(info.first, info.second)
            binding.tvReplyMsgUserNick.text = nickname
            return
        }

        val subscriber = object : BaseSubscriber<User>() {
            override fun onNext(t: User?) {
                t?.let { user ->
                    binding.tvReplyMsgUserNick.text = user.nickname
                }
            }
        }
        IMCoreManager.userModule.queryUser(message.fUid)
            .compose(RxTransform.flowableToMain())
            .subscribe(subscriber)
        disposables.add(subscriber)
    }

    open fun onViewDetached() {
        replyBodyView?.onViewDetached()
    }

    open fun onViewRecycled() {
        replyBodyView?.onViewDestroyed()
    }
}