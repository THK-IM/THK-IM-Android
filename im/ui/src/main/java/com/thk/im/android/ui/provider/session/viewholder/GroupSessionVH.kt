package com.thk.im.android.ui.provider.session.viewholder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.lifecycle.LifecycleOwner
import com.thk.im.android.core.IMCoreManager
import com.thk.im.android.core.base.BaseSubscriber
import com.thk.im.android.core.base.IMImageLoader
import com.thk.im.android.core.base.RxTransform
import com.thk.im.android.core.base.utils.DateUtils
import com.thk.im.android.core.base.utils.StringUtils
import com.thk.im.android.core.db.entity.Group
import com.thk.im.android.core.db.entity.Session
import com.thk.im.android.ui.R
import com.thk.im.android.ui.msg.viewholder.BaseSessionVH
import com.thk.im.android.ui.protocol.internal.IMSessionVHOperator
import io.reactivex.disposables.CompositeDisposable

class GroupSessionVH(
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
        lastTimeView.text = DateUtils.timeToMsgTime(session.mTime, IMCoreManager.commonModule.getSeverTime())
        if (session.unReadCount == 0) {
            unReadCountView.visibility = View.GONE
        } else {
            unReadCountView.visibility = View.VISIBLE
            unReadCountView.text = StringUtils.getMessageCount(session.unReadCount)
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
        val subscribe = object : BaseSubscriber<Group>() {
            override fun onNext(t: Group?) {
                t?.let {
                    displayAvatar(avatarView, it.avatar)
                    nickView.text = it.name
                }
            }
        }
        IMCoreManager.groupModule.findById(session.entityId)
            .compose(RxTransform.flowableToMain()).subscribe(subscribe)
        disposable.add(subscribe)
    }


    fun displayAvatar(imageView: ImageView, url: String) {
        IMImageLoader.displayImageUrl(imageView, url)
    }
}