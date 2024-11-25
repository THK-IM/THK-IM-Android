package com.thk.im.android.ui.provider.session.viewholder

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.lifecycle.LifecycleOwner
import com.thk.im.android.core.IMCoreManager
import com.thk.im.android.core.base.BaseSubscriber
import com.thk.im.android.core.base.IMImageLoader
import com.thk.im.android.core.base.RxTransform
import com.thk.im.android.core.db.entity.Group
import com.thk.im.android.ui.R
import com.thk.im.android.ui.fragment.viewholder.session.IMBaseSessionVH

class GroupSessionVH(
    lifecycleOwner: LifecycleOwner,
    parent: ViewGroup,
    resId: Int = R.layout.itemview_session
) : IMBaseSessionVH(
    lifecycleOwner, LayoutInflater.from(parent.context).inflate(resId, parent, false)
) {

    override fun renderSessionEntityInfo() {
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