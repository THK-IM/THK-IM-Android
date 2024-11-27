package com.thk.im.android.ui.fragment.viewholder.session

import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.emoji2.widget.EmojiTextView
import androidx.lifecycle.LifecycleOwner
import com.google.gson.Gson
import com.thk.im.android.core.IMCoreManager
import com.thk.im.android.core.MsgOperateStatus
import com.thk.im.android.core.MsgSendStatus
import com.thk.im.android.core.MsgType
import com.thk.im.android.core.SessionStatus
import com.thk.im.android.core.base.BaseSubscriber
import com.thk.im.android.core.base.RxTransform
import com.thk.im.android.core.base.utils.DateUtils
import com.thk.im.android.core.base.utils.StringUtils
import com.thk.im.android.core.db.entity.Message
import com.thk.im.android.core.db.entity.Session
import com.thk.im.android.ui.R
import com.thk.im.android.ui.fragment.viewholder.IMBaseVH
import com.thk.im.android.ui.protocol.internal.IMSessionVHOperator
import com.thk.im.android.ui.utils.AtStringUtils
import io.reactivex.Flowable
import io.reactivex.disposables.CompositeDisposable


abstract class IMBaseSessionVH(liftOwner: LifecycleOwner, itemView: View) :
    IMBaseVH(liftOwner, itemView) {

    val nickView: EmojiTextView = itemView.findViewById(R.id.tv_nickname)
    val avatarView: AppCompatImageView = itemView.findViewById(R.id.iv_avatar)

    val ivSendStatusView: AppCompatImageView = itemView.findViewById(R.id.iv_send_status)
    val tvSenderNameView: EmojiTextView = itemView.findViewById(R.id.tv_sender_name)
    val tvAInfoView: EmojiTextView = itemView.findViewById(R.id.tv_at_info)
    val lastMsgView: EmojiTextView = itemView.findViewById(R.id.tv_last_message)
    val lastTimeView: AppCompatTextView = itemView.findViewById(R.id.tv_last_time)
    val unReadCountView: AppCompatTextView = itemView.findViewById(R.id.tv_unread_count)

    val statusView: AppCompatImageView = itemView.findViewById(R.id.iv_session_status)
    val deleteView = itemView.findViewById<TextView>(R.id.tv_session_delete)
    val topView = itemView.findViewById<TextView>(R.id.tv_session_top)
    val muteView = itemView.findViewById<TextView>(R.id.tv_session_mute)
    val container = itemView.findViewById<ConstraintLayout>(R.id.cl_container)

    lateinit var session: Session
    private var sessionVHOperator: IMSessionVHOperator? = null
    val disposable = CompositeDisposable()

    init {
        deleteView.setOnClickListener {
            this.sessionVHOperator?.deleteSession(this.session)
        }
        muteView.setOnClickListener {
            this.session.status = this.session.status.xor(SessionStatus.Silence.value)
            this.sessionVHOperator?.updateSession(this.session)
        }
        topView.setOnClickListener {
            if (this.session.topTimestamp > 0) {
                this.session.topTimestamp = 0
            } else {
                this.session.topTimestamp = IMCoreManager.severTime
            }
            this.sessionVHOperator?.updateSession(this.session)
        }
        container.setOnClickListener {
            this.sessionVHOperator?.openSession(this.session)
        }

        container.setOnLongClickListener {
            this.sessionVHOperator?.longClickSession(this.session) ?: false
        }
    }

    /**
     * ViewHolder 绑定数据触发设置界面ui
     */
    open fun onViewBind(session: Session, sessionVHOperator: IMSessionVHOperator) {
        this.sessionVHOperator = sessionVHOperator
        updateSession(session)
    }

    open fun updateSession(session: Session) {
        this.session = session
        renderSessionEntityInfo()
        renderSessionStatus()
        renderSessionMessage()
    }

    open fun renderSessionEntityInfo() {

    }

    open fun renderSessionStatus() {
        if (session.status.and(SessionStatus.Silence.value) > 0) {
            statusView.visibility = View.VISIBLE
            unReadCountView.visibility = View.GONE
            muteView.text = itemView.context.getString(R.string.cancel_silence)
        } else {
            statusView.visibility = View.GONE
            if (session.unReadCount == 0) {
                unReadCountView.visibility = View.GONE
            } else {
                unReadCountView.visibility = View.VISIBLE
                unReadCountView.text = StringUtils.getMessageCount(session.unReadCount)
            }
            muteView.text = itemView.context.getString(R.string.silence)
        }

        lastTimeView.text = DateUtils.timeToMsgTime(session.mTime, IMCoreManager.severTime)
        if (session.topTimestamp > 0) {
            topView.text = itemView.context.getString(R.string.cancel_top)
        } else {
            topView.text = itemView.context.getString(R.string.top)
        }
    }

    open fun renderSessionMessage() {
        ivSendStatusView.visibility = View.GONE
        tvAInfoView.visibility = View.GONE
        tvSenderNameView.visibility = View.GONE
        lastMsgView.visibility = View.VISIBLE
        try {
            val msg = Gson().fromJson(session.lastMsg, Message::class.java)
            renderMessage(msg)
        } catch (e: Exception) {
            renderMessage(session.lastMsg)
            e.printStackTrace()
        }
    }

    open fun renderMessage(message: Message) {
        // 发送状态
        when (message.sendStatus) {
            MsgSendStatus.SendFailed.value -> {
                ivSendStatusView.visibility = View.VISIBLE
                ivSendStatusView.setImageResource(R.drawable.ic_msg_failed)
            }

            MsgSendStatus.Success.value -> {
                ivSendStatusView.visibility = View.GONE
            }

            else -> {
                ivSendStatusView.visibility = View.VISIBLE
                ivSendStatusView.setImageResource(R.drawable.ic_sending)
            }
        }
        // @人视图
        if (session.unReadCount > 0 && message.isAtMe() && message.oprStatus.and(MsgOperateStatus.ClientRead.value) == 0) {
            tvAInfoView.visibility = View.VISIBLE
        } else {
            tvAInfoView.visibility = View.GONE
        }
        // 消息发件人姓名展示
        renderSenderName(message)
        // 消息内容展示
        if (message.type == MsgType.Text.value) {
            if (message.getAtUIds().isNotEmpty()) {
                val subscriber = object : BaseSubscriber<String>() {
                    override fun onNext(t: String) {
                        lastMsgView.text = t
                        lastMsgView.visibility = View.VISIBLE
                    }
                }
                Flowable.just(message).flatMap { msg ->
                    val replaceContent =
                        AtStringUtils.replaceAtUIdsToNickname(msg.content!!, msg.getAtUIds()) {
                            val name = IMCoreManager.messageModule.getMsgProcessor(msg.type)
                                .getUserSessionName(msg.sid, it)
                            return@replaceAtUIdsToNickname name ?: ""
                        }
                    return@flatMap Flowable.just(replaceContent)
                }.compose(RxTransform.flowableToMain()).subscribe(subscriber)
                disposable.add(subscriber)
            } else {
                lastMsgView.text = message.content
                lastMsgView.visibility = View.VISIBLE
            }
        } else {
            lastMsgView.text =
                IMCoreManager.messageModule.getMsgProcessor(message.type).msgDesc(message)
            lastMsgView.visibility = View.VISIBLE
        }
    }

    open fun renderSenderName(message: Message) {
        if (message.fUid > 0) {
            val subscriber = object : BaseSubscriber<String>() {
                override fun onNext(t: String) {
                    tvSenderNameView.visibility = View.VISIBLE
                    tvSenderNameView.text = t + ": "
                }
            }
            Flowable.just(message).flatMap { msg ->
                val name = IMCoreManager.messageModule.getMsgProcessor(msg.type)
                    .getUserSessionName(msg.sid, msg.fUid)
                return@flatMap Flowable.just(name)
            }.compose(RxTransform.flowableToMain()).subscribe(subscriber)
            disposable.add(subscriber)
        }
    }

    open fun renderMessage(text: String?) {
        lastMsgView.text = text
    }


    override fun onViewDetached() {
        disposable.clear()
    }

    override fun onViewRecycled() {
        super.onViewRecycled()
        sessionVHOperator = null
    }


}