package com.thk.im.android.ui.group.adapter

import android.view.View
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.lifecycle.LifecycleOwner
import com.thk.im.android.R
import com.thk.im.android.core.IMCoreManager
import com.thk.im.android.core.base.BaseSubscriber
import com.thk.im.android.core.base.IMImageLoader
import com.thk.im.android.core.base.RxTransform
import com.thk.im.android.core.db.entity.User
import com.thk.im.android.ui.fragment.viewholder.BaseVH
import io.reactivex.disposables.CompositeDisposable

class GroupMemberVH(liftOwner: LifecycleOwner, itemView: View) :
    BaseVH(liftOwner, itemView) {

    private val disposable = CompositeDisposable()
    private val avatarView: AppCompatImageView = itemView.findViewById(R.id.iv_avatar)
    private val nickNameView: AppCompatTextView = itemView.findViewById(R.id.tv_nickname)

    fun onBind(id: Long) {
        showUserInfo(id)
    }

    private fun showUserInfo(id: Long) {
        val subscriber = object : BaseSubscriber<User>() {
            override fun onNext(t: User) {
                t.avatar?.let {
                    displayAvatar(avatarView, it)
                }
                nickNameView.text = t.nickname
            }

            override fun onComplete() {
                super.onComplete()
                disposable.remove(this)
            }
        }
        IMCoreManager.userModule.queryUser(id)
            .compose(RxTransform.flowableToMain())
            .subscribe(subscriber)
        disposable.add(subscriber)
    }


    private fun displayAvatar(imageView: ImageView, url: String) {
        IMImageLoader.displayImageUrl(imageView, url)
    }
}