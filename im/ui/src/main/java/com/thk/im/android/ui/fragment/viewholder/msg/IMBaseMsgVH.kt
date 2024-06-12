package com.thk.im.android.ui.fragment.viewholder.msg

import android.graphics.Color
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.view.children
import androidx.lifecycle.LifecycleOwner
import com.thk.im.android.core.IMCoreManager
import com.thk.im.android.core.MsgOperateStatus
import com.thk.im.android.core.MsgSendStatus
import com.thk.im.android.core.SessionType
import com.thk.im.android.core.base.BaseSubscriber
import com.thk.im.android.core.base.IMImageLoader
import com.thk.im.android.core.base.RxTransform
import com.thk.im.android.core.base.extension.setShape
import com.thk.im.android.core.db.entity.Message
import com.thk.im.android.core.db.entity.Session
import com.thk.im.android.core.db.entity.SessionMember
import com.thk.im.android.core.db.entity.User
import com.thk.im.android.ui.R
import com.thk.im.android.ui.fragment.view.IMReadStatusView
import com.thk.im.android.ui.fragment.view.IMReplyMsgContainerView
import com.thk.im.android.ui.fragment.view.IMsgBodyView
import com.thk.im.android.ui.fragment.viewholder.IMBaseVH
import com.thk.im.android.ui.manager.IMMsgPosType
import com.thk.im.android.ui.manager.IMUIManager
import com.thk.im.android.ui.protocol.internal.IMMsgVHOperator
import io.reactivex.Flowable
import io.reactivex.disposables.CompositeDisposable
import java.lang.Integer.max

abstract class IMBaseMsgVH(liftOwner: LifecycleOwner, itemView: View, open val viewType: Int) :
    IMBaseVH(liftOwner, itemView) {

    open lateinit var message: Message
    open lateinit var session: Session
    private var msgVHOperator: IMMsgVHOperator? = null
    private var pos: Int = 0

    private var disposable = CompositeDisposable()
    private val cardAvatarContainerView: CardView? = itemView.findViewById(R.id.avatar_container)
    private val ivAvatarView: ImageView? = itemView.findViewById(R.id.iv_avatar)
    private val tvNicknameView: TextView? = itemView.findViewById(R.id.tv_nickname)
    private val resendView: ImageView? = itemView.findViewById(R.id.iv_msg_resend)
    private val pbMsgFailedView: ProgressBar? = itemView.findViewById(R.id.pb_sending)
    private val readStatusView: IMReadStatusView? = itemView.findViewById(R.id.read_status)
    private val selectView: ImageView = itemView.findViewById(R.id.iv_msg_select)
    private val msgContentView: LinearLayout = itemView.findViewById(R.id.msg_content)
    private val msgBodyContentView: LinearLayout = itemView.findViewById(R.id.msg_body_content)
    protected val msgReplyContentView: IMReplyMsgContainerView =
        itemView.findViewById(R.id.msg_reply_content)


    abstract fun msgBodyView(): IMsgBodyView

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
        renderMsgStatus()
        updateSelectMode()
        renderReplyMsg()
        fetchUserInfo()
    }

    override fun onViewAttached() {
        super.onViewAttached()
        onMessageShow()
    }

    private fun attachLayout() {
        // 内容视图layout
        if (hasBubble()) {
            when (getPositionType()) {
                IMMsgPosType.Left.value -> {
                    val bubble = IMUIManager.uiResourceProvider?.msgBubble(message.fUid, session)
                    if (bubble != null) {
                        msgContentView.background = bubble
                    } else {
                        msgContentView.setShape(
                            Color.parseColor("#ffdddddd"),
                            floatArrayOf(0f, 10f, 0f, 10f),
                            false
                        )
                    }
                }

                IMMsgPosType.Right.value -> {
                    val bubble = IMUIManager.uiResourceProvider?.msgBubble(message.fUid, session)
                    if (bubble != null) {
                        msgContentView.background = bubble
                    } else {
                        msgContentView.setShape(
                            Color.parseColor("#d1e3fe"),
                            floatArrayOf(10f, 0f, 10f, 0f),
                            false
                        )
                    }
                }

                else -> {
                    val bubble = IMUIManager.uiResourceProvider?.msgBubble(message.fUid, session)
                    if (bubble != null) {
                        msgContentView.background = bubble
                    } else {
                        msgContentView.setShape(
                            Color.parseColor("#20000000"),
                            floatArrayOf(10f, 10f, 10f, 10f),
                            false
                        )
                    }
                }
            }
        }

        msgBodyContentView.children.forEach {
            if (it is IMsgBodyView) {
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

        resendView?.setOnClickListener {
            resend()
        }

        readStatusView?.setOnClickListener {
            msgVHOperator?.onMsgReadStatusClick(message)
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

        msgReplyContentView.setOnLongClickListener {
            onLongClickContent(it)
        }

        msgBodyContentView.setOnLongClickListener {
            onLongClickContent(it)
        }

        msgContentView.setOnLongClickListener {
            onLongClickContent(it)
        }
    }

    private fun onLongClickContent(view: View): Boolean{
        return if (canSelect()) {
            msgVHOperator?.onMsgCellLongClick(message, pos, view)
            true
        } else {
            false
        }
    }

    open fun hasBubble(): Boolean {
        return IMUIManager.getMsgIVProviderByMsgType(message.type).hasBubble()
    }

    open fun canSelect(): Boolean {
        return IMUIManager.getMsgIVProviderByMsgType(message.type).canSelect()
    }

    open fun onMessageShow() {
        if (message.msgId <= 0) {
            return
        }
        if (message.oprStatus.and(MsgOperateStatus.ClientRead.value) > 0 &&
            (message.oprStatus.and(MsgOperateStatus.ServerRead.value) > 0 || session.type == SessionType.SuperGroup.value)
        ) {
            return
        }
        msgVHOperator?.msgSender()?.readMessage(message)
        message.oprStatus = message.oprStatus.or(MsgOperateStatus.ClientRead.value)
            .or(MsgOperateStatus.ServerRead.value)
    }

    open fun fetchUserInfo() {
        tvNicknameView?.let {
            if (session.type != SessionType.Single.value) {
                it.visibility = View.VISIBLE
            } else {
                it.visibility = View.GONE
            }
        }
        if (message.fUid != 0L) {
            val userInfo = msgVHOperator?.msgSender()?.syncGetSessionMemberInfo(message.fUid)
            userInfo?.let { info ->
                renderUserInfo(info.first, info.second)
                return
            }
            val subscriber = object : BaseSubscriber<User>() {
                override fun onNext(t: User) {
                    renderUserInfo(t, null)
                }
            }
            IMCoreManager.userModule.queryUser(message.fUid)
                .compose(RxTransform.flowableToMain())
                .subscribe(subscriber)
            disposable.add(subscriber)
        }
    }

    open fun renderUserInfo(user: User, sessionMember: SessionMember?) {
        tvNicknameView?.text = IMUIManager.nicknameForSessionMember(user, sessionMember)
        val avatar = IMUIManager.avatarForSessionMember(user, sessionMember)
        ivAvatarView?.let { iv ->
            avatar?.let { avatar ->
                displayAvatar(iv, avatar)
                return
            }
        }
        renderProviderAvatar(user)
    }

    private fun renderProviderAvatar(user: User) {
        val resId = IMUIManager.uiResourceProvider?.avatar(user)
        ivAvatarView?.let {
            if (resId != null) {
                it.setImageResource(resId)
            }
        }
    }

    open fun renderMsgStatus() {
        when (message.sendStatus) {
            MsgSendStatus.SendFailed.value -> {
                pbMsgFailedView?.visibility = View.GONE
                resendView?.visibility = View.VISIBLE
                readStatusView?.visibility = View.GONE
            }

            MsgSendStatus.Success.value -> {
                pbMsgFailedView?.visibility = View.GONE
                resendView?.visibility = View.GONE
                if (message.fUid == IMCoreManager.uId) {
                    queryReadStatus()
                }
            }

            else -> {
                pbMsgFailedView?.visibility = View.VISIBLE
                resendView?.visibility = View.GONE
                readStatusView?.visibility = View.GONE
            }
        }
    }

    private fun queryReadStatus() {
        if (session.type == SessionType.MsgRecord.value || session.type == SessionType.SuperGroup.value) {
            return
        }

        val subscriber = object : BaseSubscriber<Int>() {
            override fun onNext(t: Int?) {
                t?.let {
                    showReadStatus(it)
                }
                disposable.remove(this)
            }

            override fun onError(t: Throwable?) {
                super.onError(t)
                disposable.remove(this)
            }
        }
        Flowable.just(0)
            .flatMap {
                val count = IMCoreManager.db.sessionMemberDao().findSessionMemberCount(session.id)
                return@flatMap Flowable.just(count)
            }.compose(RxTransform.flowableToMain())
            .subscribe(subscriber)
        disposable.add(subscriber)
    }

    private fun showReadStatus(count: Int) {
        val memberCount = max(count-1, 1)
        val readUIds = this.message.getReadUIds()
        val progress = readUIds.count().toFloat()/memberCount.toFloat()
        this.readStatusView?.visibility = View.VISIBLE
        val color = IMUIManager.uiResourceProvider?.tintColor() ?: Color.parseColor("#17a121")
        this.readStatusView?.updateStatus(color, 4f, progress)
    }

    private fun renderReplyMsg() {
        if (message.rMsgId != null && message.referMsg != null) {
            msgReplyContentView.visibility = View.VISIBLE
            val userInfo = msgVHOperator?.msgSender()?.syncGetSessionMemberInfo(message.fUid)
            userInfo?.let { info ->
                val user = info.first
                info.second?.noteName?.let { noteName ->
                    if (noteName.isNotEmpty()) {
                        user.nickname = noteName
                    }
                }
                info.second?.noteAvatar?.let { noteAvatar ->
                    if (noteAvatar.isNotEmpty()) {
                        user.avatar = noteAvatar
                    }
                }
                message.referMsg?.let {
                    msgReplyContentView.setMessage(
                        getPositionType(), user, it, session, msgVHOperator
                    )
                }
                return
            }

            val subscriber = object : BaseSubscriber<User>() {
                override fun onNext(t: User?) {
                    t?.let { user ->
                        message.referMsg?.let {
                            msgReplyContentView.setMessage(
                                getPositionType(), user, it, session, msgVHOperator
                            )
                        }
                    }
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
        msgVHOperator?.let {
            if (it.isSelectMode()) {
                if (canSelect()) {
                    selectView.visibility = View.VISIBLE
                    selectView.isSelected = it.isItemSelected(message)
                }
                cardAvatarContainerView?.visibility = View.GONE
            } else {
                selectView.visibility = View.GONE
                cardAvatarContainerView?.visibility = View.VISIBLE
            }
        }
    }

    protected fun getPositionType(): Int {
        return viewType % 3
    }

    fun highlightFlashing(times: Int) {
        if (times == 0) {
            return
        }
        if (times % 2 == 0) {
            val tintColor =
                IMUIManager.uiResourceProvider?.tintColor() ?: Color.parseColor("#2008AAFF")
            itemView.setBackgroundColor(tintColor)
        } else {
            itemView.setBackgroundColor(Color.TRANSPARENT)
        }
        itemView.postDelayed({
            highlightFlashing(times - 1)
        }, 350)
    }

}