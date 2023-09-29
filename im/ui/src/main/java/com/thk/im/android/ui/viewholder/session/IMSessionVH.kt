package com.thk.im.android.ui.viewholder.session

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.lifecycle.LifecycleOwner
import com.thk.im.android.base.BaseSubscriber
import com.thk.im.android.base.IMImageLoader
import com.thk.im.android.core.IMCoreManager
import com.thk.im.android.core.IMEvent
import com.thk.im.android.core.event.XEventBus
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
    val nickView: AppCompatTextView = itemView.findViewById(R.id.tv_nickname)
    val avatarView: AppCompatImageView = itemView.findViewById(R.id.iv_avatar)
    private val lastMsgView: AppCompatTextView = itemView.findViewById(R.id.tv_last_message)
    private val lastTimeView: AppCompatTextView = itemView.findViewById(R.id.tv_last_time)
    private val disposable = CompositeDisposable()

    override fun onViewBind(session: Session) {
        super.onViewBind(session)
        lastMsgView.text = session.lastMsg
        lastTimeView.text = DateUtil.getTimeline(session.mTime)
        if (session.type == 1) {
            showUserInfo(session)
        } else {
            showGroupInfo(session)
        }
    }

    override fun onViewDetached() {
        disposable.clear()
    }

    override fun onLifeOwnerResume() {
    }

    override fun onLifeOwnerPause() {
    }

    private fun showUserInfo(session: Session) {
        val subscriber = object : BaseSubscriber<User>() {
            override fun onNext(t: User) {
                nickView.text = t.name
                t.avatar?.let {
                    displayAvatar(avatarView, t.id, it)
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
                    displayAvatar(avatarView, t.id, it)
                }
            }
        }
        getGroupModule().getGroupInfo(session.entityId).subscribe(subscriber)
        disposable.add(subscriber)
    }

    fun displayAvatar(imageView: ImageView, id: Long, url: String) {
        val path = IMCoreManager.storageModule.allocAvatarPath(id, url)
        val file = File(path)
        if (file.exists()) {
            IMImageLoader.displayImageByPath(imageView, path)
        } else {
            IMCoreManager.fileLoaderModule.download(url, path, object : LoadListener {
                override fun onProgress(progress: Int, state: Int, url: String, path: String) {
                    if (state == LoadListener.Success) {
                        XEventBus.post(IMEvent.SessionUpdate.value, session)
                    }
                }

                override fun notifyOnUiThread(): Boolean {
                    return true
                }
            })
        }
    }
}

