package com.thk.im.android.ui.fragment.adapter

interface ViewHolderSelect {

    fun isSelectMode(): Boolean

    fun isItemSelected(id: Long): Boolean

    fun onSelected(id: Long, selected: Boolean)
}