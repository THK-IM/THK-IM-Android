package com.thk.im.android.ui.base

import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.gyf.immersionbar.ImmersionBar
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.core.BasePopupView
import com.thk.im.android.core.IMEvent
import com.thk.im.android.core.SignalStatus
import com.thk.im.android.core.base.utils.ToastUtils
import com.thk.im.android.core.event.XEventBus
import com.thk.im.android.ui.base.loading.PopupLoading
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

open class BaseActivity : AppCompatActivity() {

    private lateinit var loading: BasePopupView
    private lateinit var popupLoading: PopupLoading
    private val compositeDisposable = CompositeDisposable()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        statusBar()
        popupLoading = PopupLoading(this)
        loading = XPopup.Builder(this)
            .isViewMode(true)
            .isDestroyOnDismiss(false)
            .hasShadowBg(false)
            .asCustom(popupLoading)


        XEventBus.observe(this, IMEvent.OnlineStatusUpdate.value, Observer<Int> {
            onConnectStatus(it)
        })
    }

    open fun onConnectStatus(status: Int) {

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
        if (!yes)
            window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        else
            window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
    }

}