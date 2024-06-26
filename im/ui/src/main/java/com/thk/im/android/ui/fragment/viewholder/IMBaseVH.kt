package com.thk.im.android.ui.fragment.viewholder

import android.view.View
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView

abstract class IMBaseVH(private val liftOwner: LifecycleOwner, itemView: View) :
    RecyclerView.ViewHolder(itemView) {

    private val lifecycleObserver = object : DefaultLifecycleObserver {
        override fun onDestroy(owner: LifecycleOwner) {
            super.onDestroy(owner)
            liftOwner.lifecycle.removeObserver(this)
            onLifeOwnerDestroy()
        }

        override fun onPause(owner: LifecycleOwner) {
            super.onPause(owner)
            onLifeOwnerPause()
        }

        override fun onResume(owner: LifecycleOwner) {
            super.onResume(owner)
            onLifeOwnerResume()
        }
    }

    init {
        liftOwner.lifecycle.addObserver(this.lifecycleObserver)
    }

    /**
     * ViewHolder上屏触发，此处可以初始化一些和界面渲染无关的资源
     */
    open fun onViewAttached() {}

    /**
     * ViewHolder离屏触发，此处可以释放一些和界面渲染无关的资源
     */
    open fun onViewDetached() {}

    /**
     * recyclerView adapter的lifeOwner resume时触发
     */
    open fun onLifeOwnerResume() {}

    /**
     * recyclerView adapter的lifeOwner pause时触发
     */
    open fun onLifeOwnerPause() {}

    open fun onViewRecycled() {
        onViewDetached()
    }

    /**
     * recyclerView adapter的lifeOwner销毁时触发, 此处应该彻底断开ViewHolder被其他对象的引用
     */
    open fun onLifeOwnerDestroy() {
        onViewRecycled()
    }
}