package com.thk.im.android.ui.main.fragment.adapter

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
import com.thk.im.android.core.db.entity.Contact
import com.thk.im.android.core.db.entity.User
import com.thk.im.android.ui.fragment.viewholder.IMBaseVH
import io.reactivex.disposables.CompositeDisposable
import java.lang.StringBuilder


class ContactVH(liftOwner: LifecycleOwner, itemView: View) :
    IMBaseVH(liftOwner, itemView) {

    private val disposable = CompositeDisposable()
    private val avatarView: AppCompatImageView = itemView.findViewById(R.id.iv_avatar)
    private val nickNameView: AppCompatTextView = itemView.findViewById(R.id.tv_nickname)
    private val relationView: AppCompatTextView = itemView.findViewById(R.id.tv_relation)
    private val isSelectedView: AppCompatImageView = itemView.findViewById(R.id.iv_selected)

    fun onBind(contact: Contact, selected: Boolean, operator: ContactItemOperator) {
        showUserInfo(contact)
        contact.noteName?.let {
            nickNameView.text = it
        }
        setRelationText(contact.relation)
        itemView.setOnClickListener {
            operator.onItemClick(contact)
        }
        if (selected) {
            isSelectedView.visibility = View.VISIBLE
        } else {
            isSelectedView.visibility = View.GONE
        }
    }

    private fun showUserInfo(contact: Contact) {
        val subscriber = object : BaseSubscriber<User>() {
            override fun onNext(t: User) {
                t.avatar?.let {
                    displayAvatar(avatarView, it)
                }
                if (contact.noteName == null) {
                    nickNameView.text = t.nickname
                }
            }

            override fun onComplete() {
                super.onComplete()
                disposable.remove(this)
            }
        }
        IMCoreManager.userModule.queryUser(contact.id)
            .compose(RxTransform.flowableToMain())
            .subscribe(subscriber)
        disposable.add(subscriber)
    }

    private fun displayAvatar(imageView: ImageView, url: String) {
        IMImageLoader.displayImageUrl(imageView, url)
    }

    private fun setRelationText(relation: Int) {
        val builder = StringBuilder()
        if (relation.and(8) > 0) {
            builder.append("我关注了他; ")
        }
        if (relation.and(16) > 0) {
            builder.append(" 他关注了我; ")
        }
        if (relation.and(32) > 0) {
            builder.append(" 好友关系; ")
        }
        relationView.text = builder.toString()
    }

    override fun onViewDetached() {
        super.onViewDetached()
        disposable.clear()
    }
}