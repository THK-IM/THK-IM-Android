package com.thk.im.android.ui.provider

import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import com.thk.im.android.db.entity.Session
import com.thk.im.android.ui.viewholder.session.BaseSessionVH

abstract class SessionItemViewProvider {

    /**
     * 视图类型
     */
    open fun viewType(session: Session): Int {
        return session.type
    }

    /**
     * 返回消息视图实例
     */
    abstract fun viewHolder(lifecycleOwner: LifecycleOwner, viewType: Int, parent: ViewGroup): BaseSessionVH
}