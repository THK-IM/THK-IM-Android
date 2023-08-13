package com.thk.im.android.ui.provider.internal

import android.view.ViewGroup
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.thk.im.android.ui.R
import com.thk.im.android.ui.provider.SessionItemViewProvider
import com.thk.im.android.ui.viewholder.session.BaseSessionVH
import com.thk.im.android.ui.viewholder.session.IMSessionVH

class DefaultSsIVProvider : SessionItemViewProvider() {

    override fun viewHolder(lifecycleOwner: LifecycleOwner, viewType: Int, parent: ViewGroup): BaseSessionVH {
        return IMSessionVH(lifecycleOwner, parent)
    }
}