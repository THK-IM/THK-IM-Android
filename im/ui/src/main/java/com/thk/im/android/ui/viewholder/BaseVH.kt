package com.thk.im.android.ui.viewholder

import android.view.View
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView

abstract class BaseVH(private val liftOwner: LifecycleOwner, itemView: View) : RecyclerView.ViewHolder(itemView) {

    private val lifecycleObserver = object : DefaultLifecycleObserver {
        override fun onDestroy(owner: LifecycleOwner) {
            super.onDestroy(owner)
            onViewRecycled()
            onViewDestroy()
        }

        override fun onPause(owner: LifecycleOwner) {
            super.onPause(owner)
            onViewPause()
        }

        override fun onResume(owner: LifecycleOwner) {
            super.onResume(owner)
            onViewResume()
        }
    }

    open fun onViewCreated() {
        liftOwner.lifecycle.addObserver(this.lifecycleObserver)
    }

    /**
     * ViewHolder离开屏幕触发，此处可以释放一些和界面渲染无关的资源
     */
    open fun onViewRecycled() {
        liftOwner.lifecycle.removeObserver(this.lifecycleObserver)
    }

    /**
     * recyclerView adapter的lifeOwner resume时触发
     */
    abstract fun onViewResume()

    /**
     * recyclerView adapter的lifeOwner pause时触发
     */
    abstract fun onViewPause()

    /**
     * recyclerView adapter的lifeOwner销毁时触发, 此处应该彻底断开ViewHolder被其他对象的引用
     */
    abstract fun onViewDestroy()
}