package com.thk.im.android.ui.provider.internal.session.viewholder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.lifecycle.LifecycleOwner
import com.thk.im.android.base.BaseSubscriber
import com.thk.im.android.base.IMImageLoader
import com.thk.im.android.base.utils.DateUtils
import com.thk.im.android.base.utils.StringUtils
import com.thk.im.android.db.entity.Session
import com.thk.im.android.db.entity.User
import com.thk.im.android.ui.R
import com.thk.im.android.ui.fragment.viewholder.BaseSessionVH
import com.thk.im.android.ui.protocol.internal.IMSessionVHOperator
import io.reactivex.disposables.CompositeDisposable

class SingleSessionVH(
    lifecycleOwner: LifecycleOwner,
    parent: ViewGroup,
    resId: Int = R.layout.itemview_session
) : BaseSessionVH(
    lifecycleOwner, LayoutInflater.from(parent.context).inflate(resId, parent, false)
) {
    private val disposable = CompositeDisposable()

    override fun onViewBind(session: Session, sessionVHOperator: IMSessionVHOperator) {
        super.onViewBind(session, sessionVHOperator)
        lastMsgView.text = session.lastMsg
        lastTimeView.text = DateUtils.getTimeline(session.mTime)
        if (session.unRead == 0) {
            unReadCountView.visibility = View.GONE
        } else {
            unReadCountView.visibility = View.VISIBLE
            unReadCountView.text = StringUtils.getMessageCount(session.unRead)
        }
        showUserInfo(session)
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
                    displayAvatar(avatarView, it)
                }
            }
        }
        getUserModule().getUserInfo(session.entityId).subscribe(subscriber)
        disposable.add(subscriber)
    }


    fun displayAvatar(imageView: ImageView, url: String) {
        IMImageLoader.displayImageUrl(imageView, url)
    }
}

