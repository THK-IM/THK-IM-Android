package com.thk.im.android.ui.fragment.viewholder

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.annotation.LayoutRes
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

    private var avatarTaskId: String? = null

    @LayoutRes
    abstract fun getContentId(): Int

    /**
     * ViewHolder 绑定数据触发设置界面ui
     */
    open fun onViewBind(
        position: Int,
        messages: List<Message>,
        session: Session,
        msgVHOperator: IMMsgVHOperator
    ) {
        onViewDetached()
        this.pos = position
        this.message = messages[position]
        this.session = session
        this.msgVHOperator = msgVHOperator
        onViewAttached()
    }

    fun updateSelectMode() {
        if (!canSelect()) {
            selectView.visibility = View.GONE
            return
        }
        msgVHOperator?.let {
            LLog.v("updateSelectMode ${it.isSelectMode()}")
            if (it.isSelectMode()) {
                selectView.visibility = View.VISIBLE
                selectView.isSelected = it.isItemSelected(message)
            } else {
                selectView.visibility = View.GONE
            }
        }
    }

    open fun canSelect(): Boolean {
        return true
    }

    override fun onViewAttached() {
        super.onViewAttached()
        renderUserInfo()
        renderMsgStatus()
        updateSelectMode()
        readMessage()
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

    fun onCreate() {
        // 内容视图layout
        val flContent: LinearLayout = itemView.findViewById(R.id.fl_content)
        val contentContainer = LayoutInflater.from(itemView.context).inflate(getContentId(), null)
        if (hasBubble()) {
            when (getPositionType()) {
                IMMsgPosType.Left.value -> {
                    contentContainer.setShape(
                        Color.parseColor("#ffffff"),
                        Color.parseColor("#ffffff"),
                        1,
                        floatArrayOf(0f, 10f, 0f, 10f)
                    )
                }

                IMMsgPosType.Right.value -> {
                    contentContainer.setShape(
                        Color.parseColor("#d1e3fe"),
                        Color.parseColor("#d1e3fe"),
                        1,
                        floatArrayOf(10f, 0f, 10f, 0f)
                    )
                }

                else -> {
                    contentContainer.setShape(
                        Color.parseColor("#20000000"),
                        Color.parseColor("#20000000"),
                        0,
                        floatArrayOf(10f, 10f, 10f, 10f)
                    )
                }
            }
        }

        flContent.addView(contentContainer)

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

        contentContainer.setOnClickListener {
            msgVHOperator?.onMsgCellClick(message, pos, it)
        }
        contentContainer.setOnLongClickListener {
            if (canSelect()) {
                msgVHOperator?.onMsgCellLongClick(message, pos, it)
                true
            } else {
                false
            }
        }
    }

    open fun hasBubble(): Boolean {
        return true
    }

    open fun renderUserInfo() {
        tvNicknameView?.let {
            if (session.type != SessionType.Single.value) {
                it.visibility = View.VISIBLE
            } else {
                it.visibility = View.INVISIBLE
            }
        }

        if (message.fUid != 0L) {
            val subscriber = object : BaseSubscriber<User>() {
                override fun onNext(t: User) {
                    tvNicknameView?.text = t.nickname
                    ivAvatarView?.let { iv ->
                        t.avatar?.let {
                            displayAvatar(iv, it)
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

    open fun resend() {
        msgVHOperator?.onMsgResendClick(message)
    }

    fun getPositionType(): Int {
        return viewType % 3
    }

    override fun onViewDetached() {
        super.onViewDetached()
        disposable.clear()
        avatarTaskId?.let {
            IMCoreManager.fileLoadModule.cancelDownloadListener(it)
        }
    }

    open fun displayAvatar(imageView: ImageView, url: String) {
        IMImageLoader.displayImageUrl(imageView, url)
    }


    override fun onViewRecycled() {
        super.onViewRecycled()
        msgVHOperator = null
    }

}