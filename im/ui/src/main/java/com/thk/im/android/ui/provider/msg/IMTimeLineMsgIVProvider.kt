package com.thk.im.android.ui.provider.msg

import android.content.Context
import android.view.View
import androidx.lifecycle.LifecycleOwner
import com.thk.im.android.core.MsgType
import com.thk.im.android.ui.fragment.view.IMsgBodyView
import com.thk.im.android.ui.fragment.viewholder.msg.IMBaseMsgVH
import com.thk.im.android.ui.manager.IMMsgPosType
import com.thk.im.android.ui.protocol.IMBaseMessageIVProvider
import com.thk.im.android.ui.provider.msg.view.IMTextMsgView
import com.thk.im.android.ui.provider.msg.view.IMTimeLineMsgView

open class IMTimeLineMsgIVProvider : IMBaseMessageIVProvider() {

    override fun messageType(): Int {
        return MsgType.TimeLine.value
    }

    override fun hasBubble(): Boolean {
        return false
    }

    override fun canSelect(): Boolean {
        return false
    }

    override fun msgBodyView(context: Context, position: IMMsgPosType): IMsgBodyView {
        val v = IMTimeLineMsgView(context)
        v.setPosition(position)
        return v
    }

}