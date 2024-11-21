package com.thk.im.android.module.msg.call

import com.google.gson.Gson
import com.thk.im.android.constant.DemoMsgType
import com.thk.im.android.core.db.entity.Message
import com.thk.im.android.core.processor.IMBaseMsgProcessor
import com.thk.im.android.live.RoomMode

class IMCallMsgProcessor : IMBaseMsgProcessor() {
    override fun msgDesc(msg: Message): String {
        val callMsg = Gson().fromJson(msg.content, IMCallMsg::class.java) ?: return "[语音通话]"
        return if (callMsg.roomMode == RoomMode.Audio.value) {
            "[语音通话]"
        } else {
            "[视频通话]"
        }
    }

    override fun messageType(): Int {
        return DemoMsgType.Call.value
    }
}