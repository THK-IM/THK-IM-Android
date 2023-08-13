package com.thk.im.android.ui.viewholder.session

import android.view.View
import androidx.lifecycle.LifecycleOwner
import com.thk.im.android.core.IMManager
import com.thk.im.android.core.module.GroupModule
import com.thk.im.android.core.module.UserModule
import com.thk.im.android.core.signal.SignalType
import com.thk.im.android.db.entity.Session
import com.thk.im.android.ui.viewholder.BaseVH


abstract class BaseSessionVH(liftOwner: LifecycleOwner, itemView: View) :
    BaseVH(liftOwner, itemView) {

    /**
     * ViewHolder 绑定数据触发设置界面ui
     */
    open fun onViewBind(session: Session) {
        super.onViewCreated()
    }

    fun getUserModule(): UserModule {
        return IMManager.getModule(SignalType.User.value) as UserModule
    }

    fun getGroupModule(): GroupModule {
        return IMManager.getModule(SignalType.Group.value) as GroupModule
    }



}