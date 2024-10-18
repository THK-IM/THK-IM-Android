package com.thk.im.android.ui.base

import androidx.fragment.app.Fragment
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

abstract class BaseFragment : Fragment() {
    private val compositeDisposable = CompositeDisposable()


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

    override fun onDestroyView() {
        super.onDestroyView()
        clearDispose()
    }

}