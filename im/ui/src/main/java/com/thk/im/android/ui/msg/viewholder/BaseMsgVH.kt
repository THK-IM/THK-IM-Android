package com.thk.im.android.ui.msg.viewholder

import android.graphics.Color
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.view.children
import androidx.lifecycle.LifecycleOwner
import com.thk.im.android.core.IMCoreManager
import com.thk.im.android.core.MsgOperateStatus
import com.thk.im.android.core.MsgSendStatus
import com.thk.im.android.core.SessionType
import com.thk.im.android.core.base.BaseSubscriber
import com.thk.im.android.core.base.IMImageLoader
import com.thk.im.android.core.base.LLog
import com.thk.im.android.core.base.RxTransform
import com.thk.im.android.core.base.extension.setShape
import com.thk.im.android.core.db.entity.Message
import com.thk.im.android.core.db.entity.Session
import com.thk.im.android.core.db.entity.User
import com.thk.im.android.ui.R
import com.thk.im.android.ui.manager.IMMsgPosType
import com.thk.im.android.ui.manager.IMUIManager
import com.thk.im.android.ui.msg.view.IMReplyMsgContainerView
import com.thk.im.android.ui.msg.view.IMsgView
import com.thk.im.android.ui.protocol.internal.IMMsgVHOperator
import io.reactivex.disposables.CompositeDisposable

abstract class BaseMsgVH(liftOwner: LifecycleOwner, itemView: View, open val viewType: Int) :
    BaseVH(liftOwner, itemView) {

    open lateinit var message: Message
    open lateinit var session: Session
    private var msgVHOperator: IMMsgVHOperator? = null
    private var pos: Int = 0

    private var disposable = CompositeDisposable()
    open val ivAvatarView: ImageView? = itemView.findViewById(R.id.iv_avatar)
    open val tvNicknameView: TextView? = itemView.findViewById(R.id.tv_nickname)
    open val ivMsgFailedView: ImageView? = itemView.findViewById(R.id.iv_msg_fail)
    open val pbMsgFailedView: ProgressBar? = itemView.findViewById(R.id.pb_sending)
    open val selectView: ImageView = itemView.findViewById(R.id.iv_msg_select)
    open val msgContentView: LinearLayout = itemView.findViewById(R.id.msg_content)
    open val msgBodyContentView: LinearLayout = itemView.findViewById(R.id.msg_body_content)
    open val msgReplyContentView: IMReplyMsgContainerView =
        itemView.findViewById(R.id.msg_reply_content)


    abstract fun msgBodyView(): IMsgView

    /**
     * ViewHolder 绑定数据触发设置界面ui
     */
    open fun onViewBind(
        position: Int,
        messages: List<Message>,
        session: Session,
        msgVHOperator: IMMsgVHOperator
    ) {
        this.pos = position
        this.message = messages[position]
        this.session = session
        this.msgVHOperator = msgVHOperator
        onViewDetached()
        attachLayout()
        onViewAttached()
    }

    override fun onViewAttached() {
        super.onViewAttached()
        renderUserInfo()
        renderMsgStatus()
        updateSelectMode()
        readMessage()
        renderReplyMsg()
    }

    private fun attachLayout() {
        // 内容视图layout
        if (hasBubble()) {
            when (getPositionType()) {
                IMMsgPosType.Left.value -> {
                    msgContentView.setShape(
                        Color.parseColor("#ffffff"),
                        Color.parseColor("#ffffff"),
                        1,
                        floatArrayOf(0f, 10f, 0f, 10f)
                    )
                }

                IMMsgPosType.Right.value -> {
                    msgContentView.setShape(
                        Color.parseColor("#d1e3fe"),
                        Color.parseColor("#d1e3fe"),
                        1,
                        floatArrayOf(10f, 0f, 10f, 0f)
                    )
                }

                else -> {
                    msgContentView.setShape(
                        Color.parseColor("#20000000"),
                        Color.parseColor("#20000000"),
                        0,
                        floatArrayOf(10f, 10f, 10f, 10f),
                        false
                    )
                }
            }
        }

        msgBodyContentView.children.forEach {
            if (it is IMsgView) {
                msgBodyContentView.removeView(it)
            }
        }
        msgBodyContentView.addView(msgBodyView().contentView())

        ivAvatarView?.setOnClickListener {
            msgVHOperator?.onMsgSenderClick(message, pos, it)
        }
        ivAvatarView?.setOnLongClickListener {
            msgVHOperator?.onMsgSenderLongClick(message, pos, it)
            true
        }

        tvNicknameView?.setOnClickListener {
            msgVHOperator?.onMsgSenderClick(message, pos, it)
        }

        ivMsgFailedView?.setOnClickListener {
            resend()
        }

        selectView.setOnClickListener {
            msgVHOperator?.let {
                selectView.isSelected = !selectView.isSelected
                it.onSelected(message, selectView.isSelected)
            }
        }

        msgReplyContentView.setOnClickListener { view ->
            message.referMsg?.let {
                msgVHOperator?.onMsgReferContentClick(it, view)
            }
        }

        msgBodyContentView.setOnClickListener {
            msgVHOperator?.onMsgCellClick(message, pos, it)
        }
        msgBodyContentView.setOnLongClickListener {
            if (canSelect()) {
                msgVHOperator?.onMsgCellLongClick(message, pos, it)
                true
            } else {
                false
            }
        }
    }

    open fun hasBubble(): Boolean {
        return IMUIManager.getMsgIVProviderByMsgType(message.type).hasBubble()
    }

    open fun canSelect(): Boolean {
        return IMUIManager.getMsgIVProviderByMsgType(message.type).canSelect()
    }

    private fun readMessage() {
        if (message.msgId <= 0) {
            return
        }
        if (message.oprStatus.and(MsgOperateStatus.ClientRead.value) > 0 &&
            (message.oprStatus.and(MsgOperateStatus.ServerRead.value) > 0 || session.type == SessionType.SuperGroup.value)
        ) {
            return
        }
        LLog.v("readMessage ${message.id} ${message.oprStatus}")
        msgVHOperator?.readMessage(message)
        message.oprStatus = message.oprStatus.or(MsgOperateStatus.ClientRead.value)
            .or(MsgOperateStatus.ServerRead.value)
    }

    open fun renderUserInfo() {
        tvNicknameView?.let {
            if (session.type != SessionType.Single.value) {
                it.visibility = View.VISIBLE
            } else {
                it.visibility = View.GONE
            }
        }
        if (message.fUid != 0L) {
            val subscriber = object : BaseSubscriber<User>() {
                override fun onNext(t: User) {
                    tvNicknameView?.text = t.nickname
                    ivAvatarView?.let { iv ->
                        t.avatar?.let { avatar ->
                            displayAvatar(iv, avatar)
                        }
                    }
                }
            }
            IMCoreManager.userModule.queryUser(message.fUid)
                .compose(RxTransform.flowableToMain())
                .subscribe(subscriber)
            disposable.add(subscriber)
        }
    }

    open fun renderMsgStatus() {
        when (message.sendStatus) {
            MsgSendStatus.SendFailed.value -> {
                pbMsgFailedView?.visibility = View.GONE
                ivMsgFailedView?.visibility = View.VISIBLE
            }

            MsgSendStatus.Success.value -> {
                pbMsgFailedView?.visibility = View.GONE
                ivMsgFailedView?.visibility = View.GONE
            }

            else -> {
                pbMsgFailedView?.visibility = View.VISIBLE
                ivMsgFailedView?.visibility = View.GONE
            }
        }
    }

    private fun renderReplyMsg() {
        if (message.rMsgId != null && message.referMsg != null) {
            msgReplyContentView.visibility = View.VISIBLE
            val subscriber = object : BaseSubscriber<User>() {
                override fun onNext(t: User?) {
                    t?.let { user ->
                        message.referMsg?.let {
                            msgReplyContentView.setMessage(
                                user, it, session, msgVHOperator
                            )
                        }
                    }
                }

                override fun onComplete() {
                    super.onComplete()
                    disposable.remove(this)
                }
            }
            IMCoreManager.userModule.queryUser(message.referMsg!!.fUid)
                .compose(RxTransform.flowableToMain())
                .subscribe(subscriber)
            disposable.add(subscriber)
        } else {
            msgReplyContentView.visibility = View.GONE
        }
    }

    open fun resend() {
        msgVHOperator?.onMsgResendClick(message)
    }

    override fun onViewDetached() {
        super.onViewDetached()
        disposable.clear()
        msgBodyView().onViewDetached()
    }

    override fun onViewRecycled() {
        super.onViewRecycled()
        msgVHOperator = null
        msgBodyView().onViewDestroyed()
    }

    open fun displayAvatar(imageView: ImageView, url: String) {
        IMImageLoader.displayImageUrl(imageView, url)
    }


    fun updateSelectMode() {
        if (!canSelect()) {
            selectView.visibility = View.GONE
            return
        }
        msgVHOperator?.let {
            if (it.isSelectMode()) {
                selectView.visibility = View.VISIBLE
                selectView.isSelected = it.isItemSelected(message)
            } else {
                selectView.visibility = View.GONE
            }
        }
    }

    private fun getPositionType(): Int {
        return viewType % 3
    }

    fun highlightFlashing(times: Int) {
        if (times == 0) {
            return
        }
        if (times % 2 == 0) {
            itemView.setBackgroundColor(Color.parseColor("#2008AAFF"))
        } else {
            itemView.setBackgroundColor(Color.TRANSPARENT)
        }
        itemView.postDelayed({
            highlightFlashing(times - 1)
        }, 350)
    }

}