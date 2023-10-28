package com.thk.im.preview.viewholder

import android.view.View
import androidx.lifecycle.LifecycleOwner
import com.thk.im.android.db.entity.Message
import com.thk.im.android.ui.fragment.viewholder.BaseVH

open class PreviewVH(liftOwner: LifecycleOwner, itemView: View) :
    BaseVH(liftOwner, itemView) {

    protected var message: Message? = null
    open fun bindMessage(message: Message) {
        this.message = message
    }

    open fun startPreview() {

    }

    open fun stopPreview() {

    }

    open fun hide() {
    }

    open fun show() {

    }

}