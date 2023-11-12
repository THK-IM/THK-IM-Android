package com.thk.im.android.ui.provider.session.provider

import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import com.thk.im.android.db.SessionType
import com.thk.im.android.ui.fragment.viewholder.BaseSessionVH
import com.thk.im.android.ui.protocol.IMBaseSessionIVProvider
import com.thk.im.android.ui.provider.session.viewholder.SingleSessionVH

class SingleSessionIVProvider : IMBaseSessionIVProvider() {
    override fun sessionType(): Int {
        return SessionType.Single.value
    }

    override fun viewHolder(
        lifecycleOwner: LifecycleOwner, viewType: Int, parent: ViewGroup
    ): BaseSessionVH {
        return SingleSessionVH(lifecycleOwner, parent)
    }
}