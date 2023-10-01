package com.thk.im.android.ui.fragment.panel

import androidx.fragment.app.Fragment
import com.thk.im.android.ui.protocol.IMMsgSender

abstract class BasePanelFragment(var msgSender: IMMsgSender) : Fragment() {
}