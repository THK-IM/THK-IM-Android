package com.thk.im.android.ui.fragment.adapter

import com.thk.im.android.db.entity.Message

interface ViewHolderSelect {

    fun isSelectMode(): Boolean

    fun isItemSelected(message: Message): Boolean

    fun onSelected(message: Message, selected: Boolean)
}