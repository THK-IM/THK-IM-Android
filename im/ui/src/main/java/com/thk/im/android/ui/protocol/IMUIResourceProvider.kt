package com.thk.im.android.ui.protocol

import android.graphics.drawable.Drawable
import com.thk.im.android.core.db.entity.Session
import com.thk.im.android.core.db.entity.User
import com.thk.im.android.ui.manager.IMMsgPosType

interface IMUIResourceProvider {

    /**
     * @return resId
     */
    fun avatar(user: User): Int?

    fun unicodeEmojis(): List<String>?

    fun msgContainer(posType: IMMsgPosType): Int?

    fun msgBubble(fromUId: Long, session: Session?): Drawable?

    fun tintColor(): Int?

    fun inputBgColor(): Int?

    fun inputLayoutBgColor(): Int?

    fun messageSelectImageResource(): Int?

    fun supportFunction(session: Session, functionFlag: Long): Boolean
}