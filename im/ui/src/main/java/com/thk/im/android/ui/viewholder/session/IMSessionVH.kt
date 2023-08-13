package com.thk.im.android.ui.viewholder.session

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.lifecycle.LifecycleOwner
import com.thk.im.android.core.IMManager
import com.thk.im.android.core.api.BaseSubscriber
import com.thk.im.android.core.event.XEventBus
import com.thk.im.android.core.event.XEventType
import com.thk.im.android.core.fileloader.LoadListener
import com.thk.im.android.db.entity.Group
import com.thk.im.android.db.entity.Session
import com.thk.im.android.db.entity.User
import com.thk.im.android.ui.R
import com.thk.im.android.ui.utils.DateUtil
import io.reactivex.disposables.CompositeDisposable
import java.io.File

class IMSessionVH(
    lifecycleOwner: LifecycleOwner,
    parent: ViewGroup,
    resId: Int = R.layout.itemview_session
) : BaseSessionVH(
    lifecycleOwner, LayoutInflater.from(parent.context).inflate(resId, parent, false)
) {
    private lateinit var session:Session
    val nickView: AppCompatTextView = itemView.findViewById(R.id.tv_nickname)
    val avatarView: AppCompatImageView = itemView.findViewById(R.id.iv_avatar)
    private val lastMsgView: AppCompatTextView = itemView.findViewById(R.id.tv_last_message)
    private val lastTimeView: AppCompatTextView = itemView.findViewById(R.id.tv_last_time)
    private val disposable = CompositeDisposable()

    override fun onViewBind(session: Session) {
        super.onViewBind(session)
        this.session = session
        lastMsgView.text = session.lastMsg
        lastTimeView.text = DateUtil.getTimeline(session.mTime)
        if (session.type == 1) {
            showUserInfo(session)
        } else {
            showGroupInfo(session)
        }
    }

    override fun onViewRecycled() {
        disposable.clear()
    }

    override fun onViewResume() {
    }

    override fun onViewPause() {
    }

    override fun onViewDestroy() {
        disposable.clear()
    }

    private fun showUserInfo(session: Session) {
        val subscriber = object : BaseSubscriber<User>() {
            override fun onNext(t: User) {
                nickView.text = t.name
                t.avatar?.let {
                    displayAvatar(avatarView, t.id, it, 1)
                }
            }
        }
        getUserModule().getUserInfo(session.entityId).subscribe(subscriber)
        disposable.add(subscriber)
    }

    private fun showGroupInfo(session: Session) {
        val subscriber = object : BaseSubscriber<Group>() {
            override fun onNext(t: Group) {
                nickView.text = t.name
                t.avatar?.let {
                    displayAvatar(avatarView, t.id, it, 2)
                }
            }
        }
        getGroupModule().getGroupInfo(session.entityId).subscribe(subscriber)
        disposable.add(subscriber)
    }

    fun displayAvatar(imageView: ImageView, id: Long, url: String, type: Int = 1) {
        val path = IMManager.getStorageModule().allocAvatarPath(id, url, type)
        val file = File(path)
        if (file.exists()) {
            com.thk.im.android.common.IMImageLoader.displayImageByPath(imageView, path)
        } else {
            IMManager.getFileLoaderModule().download(url, path, object : LoadListener {
                override fun onProgress(progress: Int, state: Int, url: String, path: String) {
                    if (state == LoadListener.Success) {
                        XEventBus.post(XEventType.SessionUpdate.value, session)
                    }
                }

                override fun notifyOnUiThread(): Boolean {
                    return true
                }
            })
        }
    }
}

