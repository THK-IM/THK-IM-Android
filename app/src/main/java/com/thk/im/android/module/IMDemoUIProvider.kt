package com.thk.im.android.module

import android.app.Application
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.Drawable
import com.thk.im.android.core.IMCoreManager
import com.thk.im.android.core.MsgType
import com.thk.im.android.core.base.utils.ShapeUtils
import com.thk.im.android.core.db.entity.Message
import com.thk.im.android.core.db.entity.Session
import com.thk.im.android.core.db.entity.User
import com.thk.im.android.ui.manager.IMMsgPosType
import com.thk.im.android.ui.protocol.IMUIResourceProvider

class IMDemoUIProvider(private val app: Application) : IMUIResourceProvider {

    private var emojis = listOf(
        "😀", "😃", "😄", "😁", "😆", "😅", "🤣", "😂", "🙂", "🙃", "🫠", "😉", "😊", "😇",
        "🥰", "😍", "🤩", "😘", "😗", "😚", "😙", "🥲", "😋", "😛", "😜", "🤪", "😝", "🤑",
        "🤗", "🤭", "🫢", "🫣", "🤫", "🤔", "🫡", "😌", "😔", "😪", "🤤", "😴", "😭", "😱",
        "😖", "😣", "😞", "😓", "😩", "😫", "🥱", "😤", "😡", "🤡", "🤖", "😺", "😸", "😹",
        "😻", "😼", "😽", "🙀", "😿", "😾", "💔", "🩷", "💢", "💥", "💫", "💦", "💋", "💤",
        "✅️", "❎️", "👋", "🤚", "🖐️", "✋️", "🖖", "🫱", "🫲", "🫳", "🫴", "🫷", "🫸", "👌",
        "🤌", "🤏", "✌️", "🤞", "🫰", "🤟", "🤘", "🤙", "👈️", "👉️", "👆️", "🖕", "👇️", "☝️",
        "🫵", "👍️", "👎️", "✊️", "👊", "🤛", "🤜", "👏", "🙌", "🫶", "👐", "🤲", "🤝", "🙏",
        "👄", "🫦", "🐵", "🐒", "🦍", "🦧", "🐶", "🐕️", "🦮", "🐕‍🦺", "🐩", "🐺", "🦊", "🦝",
        "🐱", "🐈️", "🐈‍⬛", "🦁", "🐯", "🐅", "🐆", "🐴", "🫎", "🫏", "🐎", "🦄", "🦓", "🦌",
        "🦬", "🐮", "🐂", "🐃", "🐄", "🐷", "🐖", "🐗", "🐽", "🐏", "🐑", "🐐", "🐪", "🐫",
        "🦙", "🦒", "🐘", "🦣", "🦏", "🦛", "🐭", "🐁", "🐀", "🐹", "🐰", "🐇", "🐿️", "🦫",
        "🦔", "🦇", "🐻‍“, ❄️", "🐨", "🐼", "🦥", "🦦", "🦨", "🦘", "🦡", "🐾", "🦃", "🐔",
        "🐓", "🐣", "🐤", "🐥", "🐦️", "🐧", "🕊️", "🦅", "🦆", "🦢", "🦉", "🦤", "🪶", "🦩",
        "🦚", "🦜", "🍆", "🌶️"
    )

    override fun avatar(user: User): Int? {
        return null
    }

    override fun unicodeEmojis(): List<String> {
        return emojis
    }

    override fun msgContainer(posType: IMMsgPosType): Int? {
        return null
    }

    override fun msgBubble(message: Message, session: Session?): Drawable {
        val corner = 12f * Resources.getSystem().displayMetrics.density
//        if (session?.functionFlag == 0L) {
//            return ShapeUtils.createRectangleDrawable(
//                Color.WHITE, Color.WHITE, 1, floatArrayOf(corner, corner, corner, corner)
//            )
//        }

        if (message.type == MsgType.Revoke.value) {
            return ShapeUtils.createRectangleDrawable(
                Color.WHITE, Color.WHITE, 0, floatArrayOf(corner, corner, corner, corner)
            )
        }
        when (message.fUid) {
            IMCoreManager.uId -> {
                return ShapeUtils.createRectangleDrawable(
                    Color.WHITE, Color.WHITE, 1, floatArrayOf(corner, 0f, corner, corner)
                )
            }

            0L -> {
                return ShapeUtils.createRectangleDrawable(
                    Color.parseColor("#45000000"),
                    Color.parseColor("#45000000"),
                    0,
                    floatArrayOf(corner, corner, corner, corner)
                )
            }

            else -> {
                return ShapeUtils.createRectangleDrawable(
                    Color.WHITE, Color.WHITE, 0, floatArrayOf(0f, corner, corner, corner)
                )
            }
        }
    }

    override fun tintColor(): Int {
        return Color.parseColor("#00FF00")
    }

    override fun inputBgColor(): Int {
        return Color.parseColor("#F4F4F4")
    }

    override fun inputLayoutBgColor(): Int {
        return Color.WHITE
    }

    override fun messageSelectImageResource(): Int? {
        return null
    }

    override fun supportFunction(session: Session, functionFlag: Long): Boolean {
        return session.functionFlag.and(functionFlag) != 0L
    }
}