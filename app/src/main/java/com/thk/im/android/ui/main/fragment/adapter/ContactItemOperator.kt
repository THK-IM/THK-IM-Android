package com.thk.im.android.ui.main.fragment.adapter

import com.thk.im.android.core.db.entity.Contact

interface ContactItemOperator {
    fun onItemClick(contact: Contact)
}