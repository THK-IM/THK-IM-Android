package com.thk.im.android.ui.protocol

import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import com.thk.im.android.core.db.entity.Session
import com.thk.im.android.ui.msg.viewholder.BaseSessionVH

abstract class IMBaseSessionIVProvider {

    abstract fun sessionType(): Int

    /**
     * 视图类型
     */
    open fun viewType(session: Session): Int {
        return sessionType()
    }

    /**
     * 返回消息视图实例
     */
    abstract fun viewHolder(lifecycleOwner: LifecycleOwner, viewType: Int, parent: ViewGroup): BaseSessionVH
}