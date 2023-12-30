package com.thk.im.android.ui.base

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.gyf.immersionbar.ImmersionBar
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.core.BasePopupView
import com.thk.im.android.R
import com.thk.im.android.core.IMEvent
import com.thk.im.android.core.base.utils.ToastUtils
import com.thk.im.android.core.event.XEventBus
import com.thk.im.android.ui.base.loading.PopupLoading
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

abstract class BaseActivity : AppCompatActivity() {

    private lateinit var loading: BasePopupView
    private lateinit var popupLoading: PopupLoading
    private val compositeDisposable = CompositeDisposable()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        statusBar()
        popupLoading = PopupLoading(this)
        loading = XPopup.Builder(this).isViewMode(true).isDestroyOnDismiss(false).hasShadowBg(false)
            .asCustom(popupLoading)

        XEventBus.observe(this, IMEvent.OnlineStatusUpdate.value, Observer<Int> {
            onConnectStatus(it)
        })

        resetToolbar()
    }

    fun resetToolbar() {
        val tb = getToolbar()
        tb?.let {
            val backView: ImageView = tb.findViewById(R.id.tb_iv_back)
            if (needBackIcon()) {
                backView.visibility = View.VISIBLE
                backView.setOnClickListener {
                    finish()
                }
            } else {
                backView.visibility = View.GONE
            }

            val oprView: ImageView = tb.findViewById(R.id.tb_menu1)
            oprView.setOnClickListener {
                onToolBarMenuClick(it)
            }
            oprView.visibility = menuMoreVisibility(R.id.tb_menu1)
            oprView.setImageDrawable(menuIcon(R.id.tb_menu1))

            val moveView: ImageView = tb.findViewById(R.id.tb_menu2)
            moveView.setOnClickListener {
                onToolBarMenuClick(it)
            }
            moveView.visibility = menuMoreVisibility(R.id.tb_menu2)
            moveView.setImageDrawable(menuIcon(R.id.tb_menu2))
        }
    }

    open fun menuMoreVisibility(id: Int): Int {
        return View.INVISIBLE
    }

    open fun menuIcon(id: Int): Drawable? {
        return if (id == R.id.tb_menu2) {
            ContextCompat.getDrawable(this, R.drawable.ic_add)
        } else  {
            ContextCompat.getDrawable(this, R.drawable.ic_search)
        }
    }

    open fun onToolBarMenuClick(view: View) {

    }

    open fun onConnectStatus(status: Int) {

    }

    open fun getToolbar(): Toolbar? {
        return null
    }

    open fun setTitle(title: String) {
        val tb = getToolbar()
        tb?.let {
            val titleView: TextView = it.findViewById(R.id.tb_title)
            titleView.text = title
        }
    }

    open fun needBackIcon(): Boolean {
        return false
    }

    fun addDispose(disposable: Disposable) {
        compositeDisposable.add(disposable)
    }

    fun removeDispose(disposable: Disposable) {
        compositeDisposable.remove(disposable)
    }

    fun clearDispose() {
        compositeDisposable.clear()
    }

    override fun onDestroy() {
        super.onDestroy()
        loading.destroy()
        compositeDisposable.clear()
    }

    open fun showLoading(cancelAble: Boolean = true) {
        if (!loading.isShow) {
            loading.show()
        }
        popupLoading.setIsDismissOnBackPressed(cancelAble)
        popupLoading.setIsDismissOnTouchOutside(cancelAble)
    }

    open fun dismissLoading() {
        if (loading.isShow) {
            loading.dismiss()
        }
    }

    private fun statusBar() {
        ImmersionBar.with(this).transparentStatusBar().statusBarDarkFont(isDarkFont()).init()
    }

    open fun isDarkFont(): Boolean {
        return true
    }

    open fun showToast(text: String) {
        ToastUtils.showShort(text)
    }

    /**
     * 是否允许截屏
     */
    fun screenshotSafe(yes: Boolean) {
        if (!yes) window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        else window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
    }

}