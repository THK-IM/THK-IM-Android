package com.thk.im.android.ui.viewholder.msg

import android.view.LayoutInflater
import android.view.View
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
import com.thk.im.android.db.SessionType
import com.thk.im.android.db.entity.Message
import com.thk.im.android.db.entity.Session
import com.thk.im.android.db.entity.User
import com.thk.im.android.ui.R
import com.thk.im.android.ui.viewholder.BaseVH
import io.reactivex.disposables.CompositeDisposable
import java.io.File

abstract class BaseMsgVH(liftOwner: LifecycleOwner, itemView: View, open val viewType: Int) :
    BaseVH(liftOwner, itemView) {

    open lateinit var messsage: Message
    open lateinit var session: Session
    lateinit var contentContainer: View

    protected open val disposable = CompositeDisposable()
    protected open val ivAvatarView: ImageView? = itemView.findViewById(R.id.iv_avatar)
    protected open val tvNicknameView: TextView? = itemView.findViewById(R.id.tv_nickname)
    protected open val ivMsgFailedView: ImageView? = itemView.findViewById(R.id.iv_msg_fail)
    protected open val pbMsgFailedView: ProgressBar? = itemView.findViewById(R.id.pb_sending)

    @LayoutRes
    abstract fun getContentId(): Int

    override fun onViewCreated() {
        super.onViewCreated()
//        val flContent: LinearLayout = when (viewType % 3) {
//            1 -> {
//                itemView.findViewById(R.id.fl_content_left)
//            }
//            2 -> {
//                itemView.findViewById(R.id.fl_content_right)
//            }
//
//            else -> {
//                itemView.findViewById(R.id.fl_content_mid)
//            }
//        }

        val flContent: LinearLayout = itemView.findViewById(R.id.fl_content)
        contentContainer = LayoutInflater.from(itemView.context).inflate(getContentId(), null)
        flContent.addView(contentContainer)

    }

    /**
     * ViewHolder 绑定数据触发设置界面ui
     */
    open fun onViewBind(msg: Message, ses: Session) {
//        onViewCreated()
        var flContainer: LinearLayout? = null
        if (viewType % 3 == 0) {
            flContainer = itemView.findViewById(R.id.fl_container_mid)
            val lp = flContainer.layoutParams
            lp.width = AppUtils.instance().screenWidth
            flContainer.layoutParams = lp
        } else if (viewType % 3 == 1) {
            flContainer = itemView.findViewById(R.id.fl_container_left)
            val lp = flContainer.layoutParams
            lp.width = 300.dp2px()
            flContainer.layoutParams = lp
        } else {
            flContainer = itemView.findViewById(R.id.fl_container_right)
            val lp = flContainer.layoutParams
            lp.width = 300.dp2px()
            flContainer.layoutParams = lp
        }

        messsage = msg
        session = ses

        tvNicknameView?.let {
            if (ses.type != SessionType.Single.value) {
                it.visibility = View.VISIBLE
            } else {
                it.visibility = View.GONE
            }
        }

        if (msg.fUid != 0L) {
            val subscriber = object : BaseSubscriber<User>() {
                override fun onNext(t: User) {
                    tvNicknameView?.text = t.name
                    ivAvatarView?.let { iv ->
                        t.avatar?.let { it ->
                            displayAvatar(iv, t.id, it, 1)
                        }
                    }
                }
            }

            IMCoreManager.getUserModule().getUserInfo(msg.fUid).subscribe(subscriber)
            disposable.add(subscriber)
        }

        when (msg.sendStatus) {
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
        val msgProcessor = IMCoreManager.getMessageModule().getMsgProcessor(messsage.type)
        msgProcessor.resend(messsage)
    }

    fun getType(): Int {
        return viewType % 3
    }

    override fun onViewResume() {
    }

    override fun onViewPause() {
    }

    override fun onViewRecycled() {
        super.onViewRecycled()
        disposable.clear()
    }

    override fun onViewDestroy() {
        super.onViewRecycled()
        disposable.clear()
    }


    open fun displayAvatar(imageView: ImageView, id: Long, url: String, type: Int = 1) {
        val path = IMCoreManager.getStorageModule().allocAvatarPath(id, url, type)
        val file = File(path)
        if (file.exists()) {
            IMImageLoader.displayImageByPath(imageView, path)
        } else {
            IMCoreManager.fileLoaderModule.download(url, path, object : LoadListener {
                override fun onProgress(progress: Int, state: Int, url: String, path: String) {
                    if (state == LoadListener.Success) {
                        XEventBus.post(IMEvent.MsgUpdate.value, messsage)
                    }
                }

                override fun notifyOnUiThread(): Boolean {
                    return true
                }
            })
        }
    }
}