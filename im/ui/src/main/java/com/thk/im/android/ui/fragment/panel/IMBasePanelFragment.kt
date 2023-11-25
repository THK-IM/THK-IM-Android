package com.thk.im.android.ui.fragment.panel

import androidx.fragment.app.Fragment
import com.thk.im.android.ui.protocol.internal.IMMsgSender

abstract class IMBasePanelFragment : Fragment() {

    fun getMsgSender(): IMMsgSender? {
        return if (parentFragment is IMMsgSender) {
            parentFragment as IMMsgSender
        } else {
            null
        }
    }
}