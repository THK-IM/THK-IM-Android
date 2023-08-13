package com.thk.im.android.ui.panel.component.internal

import android.content.Context
import android.view.View
import androidx.annotation.DrawableRes
import com.hjq.permissions.XXPermissions

/**
 * 组件基类
 */
abstract class BaseUIComponent(
    val name: String,
    @DrawableRes val icon: Int,
    ) : Component {

    lateinit var context: Context
    lateinit var attachView: View
    lateinit var componentManager: UIComponentManager

    override fun show() {
        attachView.visibility = View.VISIBLE
    }

    override fun hide() {
        attachView.visibility = View.GONE
    }

    fun isPermissionsGranted(context: Context, vararg permission: String): Boolean {
        return XXPermissions.isGranted(
            context,
            permission
        )
    }

    fun requestPermissions(vararg permission: String) {
        XXPermissions.with(context)
            .permission(permission)
            .request { permissions, all ->
                if (all) {
                    onPermissionGranted(permissions)
                } else {
                    onPermissionDenied(permissions)
                }
            }
    }

    open fun onPermissionGranted(permissions: List<String>) {

    }

    open fun onPermissionDenied(permissions: List<String>) {

    }


}