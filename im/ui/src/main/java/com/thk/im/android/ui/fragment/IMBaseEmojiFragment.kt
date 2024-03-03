package com.thk.im.android.ui.fragment

import androidx.fragment.app.Fragment
import com.thk.im.android.ui.protocol.internal.IMMsgSender

abstract class IMBaseEmojiFragment : Fragment() {

    fun getMsgSender(): IMMsgSender? {
        return if (parentFragment is IMMsgSender) {
            parentFragment as IMMsgSender
        } else {
            null
        }
    }
}