package com.thk.im.android.ui.viewholder

import android.view.View
import android.view.View.OnAttachStateChangeListener
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.thk.im.android.base.LLog

abstract class BaseVH(private val liftOwner: LifecycleOwner, itemView: View) :
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
        itemView.addOnAttachStateChangeListener(object : OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(p0: View) {
                onViewAttached()
            }

            override fun onViewDetachedFromWindow(p0: View) {
                onViewDetached()
            }

        })
    }

    open fun onViewAttached() {

    }

    /**
     * ViewHolder离开屏幕触发，此处可以释放一些和界面渲染无关的资源
     */
    open fun onViewDetached() {
    }

    /**
     * recyclerView adapter的lifeOwner resume时触发
     */
    open fun onLifeOwnerResume() {}

    /**
     * recyclerView adapter的lifeOwner pause时触发
     */
    open fun onLifeOwnerPause() {}

    /**
     * recyclerView adapter的lifeOwner销毁时触发, 此处应该彻底断开ViewHolder被其他对象的引用
     */
    open fun onLifeOwnerDestroy() {
        onViewDetached()
    }
}