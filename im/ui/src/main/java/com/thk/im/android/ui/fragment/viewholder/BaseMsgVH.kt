package com.thk.im.android.ui.fragment.viewholder

import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.annotation.LayoutRes
import androidx.lifecycle.LifecycleOwner
import com.thk.im.android.base.BaseSubscriber
import com.thk.im.android.base.IMImageLoader
import com.thk.im.android.core.IMCoreManager
import com.thk.im.android.core.IMEvent
import com.thk.im.android.core.event.XEventBus
import com.thk.im.android.core.fileloader.LoadListener
import com.thk.im.android.db.MsgSendStatus
import com.thk.im.android.db.SessionType
import com.thk.im.android.db.entity.Message
import com.thk.im.android.db.entity.Session
import com.thk.im.android.db.entity.User
import com.thk.im.android.ui.R
import com.thk.im.android.ui.protocol.IMMsgVHOperator
import io.reactivex.disposables.CompositeDisposable
import java.io.File

abstract class BaseMsgVH(liftOwner: LifecycleOwner, itemView: View, open val viewType: Int) :
    BaseVH(liftOwner, itemView), View.OnClickListener, View.OnLongClickListener {

    open lateinit var message: Message
    open lateinit var session: Session
    private var msgVHOperator: IMMsgVHOperator? = null
    private var pos: Int = 0

    open val disposable = CompositeDisposable()
    open val ivAvatarView: ImageView? = itemView.findViewById(R.id.iv_avatar)
    open val tvNicknameView: TextView? = itemView.findViewById(R.id.tv_nickname)
    open val ivMsgFailedView: ImageView? = itemView.findViewById(R.id.iv_msg_fail)
    open val pbMsgFailedView: ProgressBar? = itemView.findViewById(R.id.pb_sending)

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

    override fun onViewAttached() {
        super.onViewAttached()
        renderUserInfo()
        renderMsgStatus()
    }

    fun onCreate() {
        // 内容视图layout
        val flContent: LinearLayout = itemView.findViewById(R.id.fl_content)
        val contentContainer = LayoutInflater.from(itemView.context).inflate(getContentId(), null)
        flContent.addView(contentContainer)
        contentContainer.setOnClickListener(this)
        contentContainer.setOnLongClickListener(this)
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
                    tvNicknameView?.text = t.name
                    ivAvatarView?.let { iv ->
                        t.avatar?.let {
                            displayAvatar(iv, t.id, it)
                        }
                    }
                }
            }
            IMCoreManager.getUserModule().getUserInfo(message.fUid).subscribe(subscriber)
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

        ivMsgFailedView?.setOnClickListener {
            resend()
        }
    }

    open fun resend() {
        msgVHOperator?.onMsgResendClick(message)
    }

    fun getType(): Int {
        return viewType % 3
    }

    override fun onViewDetached() {
        super.onViewDetached()
        disposable.clear()
        avatarTaskId?.let {
            IMCoreManager.fileLoadModule.cancelDownloadListener(it)
        }
    }

    open fun displayAvatar(imageView: ImageView, id: Long, url: String) {
        val path = IMCoreManager.storageModule.allocAvatarPath(id, url)
        val file = File(path)
        if (file.exists()) {
            IMImageLoader.displayImageByPath(imageView, path)
        } else {
            avatarTaskId = IMCoreManager.fileLoadModule.download(url, path, object : LoadListener {
                override fun onProgress(progress: Int, state: Int, url: String, path: String) {
                    if (state == LoadListener.Success) {
                        XEventBus.post(IMEvent.MsgUpdate.value, message)
                    }
                }

                override fun notifyOnUiThread(): Boolean {
                    return true
                }
            })
        }
    }

    override fun onClick(p0: View?) {
        p0?.let {
            msgVHOperator?.onMsgCellClick(message, pos, it)
        }
    }

    override fun onLongClick(p0: View?): Boolean {
        if (p0 != null && msgVHOperator != null) {
            msgVHOperator!!.onMsgCellLongClick(message, pos, p0)
            return true
        }
        return false
    }

    override fun onViewRecycled() {
        super.onViewRecycled()
        msgVHOperator = null
    }
}