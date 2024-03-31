package com.thk.im.android.ui.protocol

import com.thk.im.android.core.db.entity.User

interface IMUIResourceProvider {

    /**
     * @return resId
     */
    fun avatar(user: User): Int?

    fun unicodeEmojis(): List<String>?
}