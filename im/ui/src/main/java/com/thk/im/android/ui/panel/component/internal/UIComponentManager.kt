package com.thk.im.android.ui.panel.component.internal

import android.content.Context
import android.view.ViewGroup
import androidx.annotation.ColorInt

/**
 * UI组件管理
 */
class UIComponentManager(val provider: IViewHolderProvider) {

    lateinit var mContext: Context

    private var initialized = false

    val size get() = components.size

    private val components = mutableListOf<BaseUIComponent>()

    /**
     * 面板颜色
     */
    @ColorInt
    var panelColor: Int = 0

    var mSid: Long = 0

    /**
     * 初始化UIComponentManager
     */
    fun init(context: Context, sid: Long) {
        mContext = context
        mSid = sid
        initialized = true
    }

    /**
     * 注册组件
     */
    fun registerComponent(component: BaseUIComponent) {
        checkInit()
        component.context = mContext
        component.componentManager = this
        components.add(component)
        component.onComponentCreate()
    }

    /**
     * 取消注册组件
     */
    fun unregisterComponent(component: BaseUIComponent) {
        if (components.contains(component)) {
            components.remove(component)
            component.onComponentDestroy()
        }
    }

    private fun checkInit() {
        if (!initialized) {
            throw IllegalStateException("invoke init method first")
        }
    }

    /**
     * 根据下标获取组件
     */
    fun getComponent(position: Int): BaseUIComponent? {
        return components.getOrNull(position)
    }


    fun provideViewHolder(parent: ViewGroup): BaseComponentViewHolder {
        return provider.provideViewHolder(parent)
    }


    interface IViewHolderProvider {
        fun provideViewHolder(parent: ViewGroup): BaseComponentViewHolder
    }
}