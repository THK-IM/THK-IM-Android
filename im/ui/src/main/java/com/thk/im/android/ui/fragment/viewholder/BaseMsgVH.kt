package com.thk.im.android.ui.fragment.viewholder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.annotation.LayoutRes
import androidx.lifecycle.LifecycleOwner
import com.thk.im.android.base.AppUtils
import com.thk.im.android.base.BaseSubscriber
import com.thk.im.android.base.IMImageLoader
import com.thk.im.android.base.extension.dp2px
import com.thk.im.android.core.IMCoreManager
import com.thk.im.android.core.IMEvent
import com.thk.im.android.core.event.XEventBus
import com.thk.im.android.core.fileloader.LoadListener
import com.thk.im.android.db.MsgSendStatus
import com.thk.im.android.db.entity.Message
import com.thk.im.android.db.entity.Session
import com.thk.im.android.db.entity.User
import com.thk.im.android.ui.R
import com.thk.im.android.ui.provider.internal.viewholder.msg.MsgPosType
import io.reactivex.disposables.CompositeDisposable
import java.io.File

abstract class BaseMsgVH(liftOwner: LifecycleOwner, itemView: View, open val viewType: Int) :
    BaseVH(liftOwner, itemView), View.OnClickListener, View.OnLongClickListener {

    open lateinit var message: Message
    open lateinit var session: Session
    lateinit var contentContainer: View

    open val disposable = CompositeDisposable()
    open val ivAvatarView: ImageView? = itemView.findViewById(R.id.iv_avatar)
    open val tvNicknameView: TextView? = itemView.findViewById(R.id.tv_nickname)
    open val ivMsgFailedView: ImageView? = itemView.findViewById(R.id.iv_msg_fail)
    open val pbMsgFailedView: ProgressBar? = itemView.findViewById(R.id.pb_sending)

    private var avatarTaskId: String? = null

    @LayoutRes
    abstract fun getContentId(): Int

    override fun onViewAttached() {
        super.onViewAttached()
    }

    /**
     * ViewHolder 绑定数据触发设置界面ui
     */
    open fun onViewBind(message: Message, session: Session) {
        this.message = message
        this.session = session

        renderUserInfo()
        renderMsgStatus()
    }

    fun resetLayout() {
        // 包裹视图layout
        val flContainer: LinearLayout?
        if (viewType % 3 == MsgPosType.Mid.value) {
            flContainer = itemView.findViewById(R.id.fl_container_mid)
            val lp = flContainer.layoutParams
            lp.width = AppUtils.instance().screenWidth
            lp.height = ViewGroup.LayoutParams.WRAP_CONTENT
            flContainer.layoutParams = lp
        } else if (viewType % 3 == MsgPosType.Left.value) {
            flContainer = itemView.findViewById(R.id.fl_container_left)
            val lp = flContainer.layoutParams
            lp.width = 300.dp2px()
            lp.height = ViewGroup.LayoutParams.WRAP_CONTENT
            flContainer.layoutParams = lp
        } else {
            flContainer = itemView.findViewById(R.id.fl_container_right)
            val lp = flContainer.layoutParams
            lp.width = 300.dp2px()
            lp.height = ViewGroup.LayoutParams.WRAP_CONTENT
            flContainer.layoutParams = lp
        }
        // 内容视图layout
        val flContent: LinearLayout = itemView.findViewById(R.id.fl_content)
        contentContainer = LayoutInflater.from(itemView.context).inflate(getContentId(), null)
        flContent.addView(contentContainer)
        contentContainer.setOnClickListener(this)
        contentContainer.setOnLongClickListener(this)
    }

    open fun renderUserInfo() {
        tvNicknameView?.let {
//            if (session.type != SessionType.Single.value) {
//                it.visibility = View.VISIBLE
//            } else {
//                it.visibility = View.GONE
//            }
            it.visibility = View.VISIBLE
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
        val msgProcessor = IMCoreManager.getMessageModule().getMsgProcessor(message.type)
        msgProcessor.resend(message)
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

    }

    override fun onLongClick(p0: View?): Boolean {
        return false
    }
}