package com.thk.im.android.ui.provider.msg.proccessor

import com.thk.im.android.core.IMCoreManager
import com.thk.im.android.core.MsgOperateStatus
import com.thk.im.android.core.MsgType
import com.thk.im.android.core.db.entity.Message
import com.thk.im.android.core.db.entity.User
import com.thk.im.android.core.processor.IMBaseMsgProcessor
import com.thk.im.android.ui.R
import com.thk.im.android.ui.utils.AtStringUtils


open class IMTextMsgProcessor : IMBaseMsgProcessor() {

    override fun messageType(): Int {
        return MsgType.Text.value
    }

    override fun atMeDesc(msg: Message): String {
        return IMCoreManager.app.getString(R.string.someone_at_me)
    }

    override fun msgDesc(msg: Message): String {
        if (msg.content != null) {
            var body = msg.content
            if (!msg.atUsers.isNullOrBlank()) {
                body = AtStringUtils.replaceAtUIdsToNickname(msg.content!!, msg.getAtUIds()) { id ->
                    if (id == -1L) {
                        return@replaceAtUIdsToNickname User.all.nickname
                    }
                    val sessionMember =
                        IMCoreManager.db.sessionMemberDao().findSessionMember(msg.sid, id)
                    if (sessionMember?.noteName != null && sessionMember.noteName!!.isNotEmpty()) {
                        return@replaceAtUIdsToNickname sessionMember.noteName!!
                    }
                    val user = IMCoreManager.db.userDao().findById(id)
                    if (user != null) {
                        return@replaceAtUIdsToNickname user.nickname
                    }
                    return@replaceAtUIdsToNickname ""
                }
            }
            var editFlag = ""
            if (msg.oprStatus.and(MsgOperateStatus.Update.value) > 0) {
                editFlag = IMCoreManager.app.getString(R.string.edited)
            }
            return editFlag + body
        }
        return IMCoreManager.app.getString(R.string.im_text_msg)
    }
}