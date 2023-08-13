package com.thk.im.android.ui.panel.component.internal

import android.view.View

/**
 * 功能面板上的组件
 */
interface Component {
    fun onComponentCreate() {}
    fun show()
    fun hide()
    fun onComponentDestroy() {}
    fun onComponentClick(view: View?)


    interface Factory {
        fun create(name: String, resId: Int, url: String)
    }
}