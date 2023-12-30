package com.thk.im.android.ui.provider.session.provider

import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import com.thk.im.android.core.SessionType
import com.thk.im.android.ui.fragment.viewholder.BaseSessionVH
import com.thk.im.android.ui.protocol.IMBaseSessionIVProvider
import com.thk.im.android.ui.provider.session.viewholder.GroupSessionVH

class SuperGroupSessionIVProvider : IMBaseSessionIVProvider() {
    override fun sessionType(): Int {
        return SessionType.SuperGroup.value
    }

    override fun viewHolder(
        lifecycleOwner: LifecycleOwner, viewType: Int, parent: ViewGroup
    ): BaseSessionVH {
        return GroupSessionVH(lifecycleOwner, parent)
    }
}