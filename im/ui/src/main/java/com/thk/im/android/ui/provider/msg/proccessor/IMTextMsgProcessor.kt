package com.thk.im.android.ui.provider.msg.proccessor

import com.thk.im.android.core.IMCoreManager
import com.thk.im.android.core.MsgType
import com.thk.im.android.core.db.entity.Message
import com.thk.im.android.core.processor.IMBaseMsgProcessor


class IMTextMsgProcessor : IMBaseMsgProcessor() {

    override fun messageType(): Int {
        return MsgType.TEXT.value
    }

    override fun getSessionDesc(msg: Message): String {
        return if (msg.data != null) {
            msg.data!!
        } else {
            if (msg.content != null) {
                val regex = "(?<=@)(.+?)(?=\\s)".toRegex()
                val body = regex.replace(msg.content!!) { result ->
                    try {
                        val id = result.value.toLong()
                        val user = IMCoreManager.db.userDao().findById(id)
                        if (user == null) {
                            return@replace ""
                        } else {
                            return@replace user.nickname
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        return@replace ""
                    }
                }
                return body
            } else {
                return ""
            }
        }
    }
}