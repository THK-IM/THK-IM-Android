package com.thk.im.preview

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.graphics.Rect
import android.os.Build
import android.view.View
import androidx.annotation.Keep
import com.google.gson.Gson
import com.thk.im.android.core.IMCoreManager
import com.thk.im.android.core.SessionType
import com.thk.im.android.core.base.BaseSubscriber
import com.thk.im.android.core.base.RxTransform
import com.thk.im.android.core.db.entity.Message
import com.thk.im.android.core.db.entity.Session
import com.thk.im.android.ui.manager.IMRecordMsgBody
import com.thk.im.android.ui.protocol.IMPreviewer
import io.reactivex.Flowable

@Keep
class Previewer(app: Application) : IMPreviewer {

    init {
        VideoCache.init(app)
    }

    override fun previewMediaMessage(
        activity: Activity,
        items: ArrayList<Message>,
        view: View,
        defaultId: Long
    ) {
        val intent = Intent(activity, IMMediaPreviewActivity::class.java)
        val locations = IntArray(2)
        view.getLocationOnScreen(locations)
        intent.putParcelableArrayListExtra("messages", items)
        val rect = Rect(
            locations[0],
            locations[1],
            locations[0] + view.measuredWidth,
            locations[1] + view.measuredHeight,
        )
        intent.putExtra("origin_rect", rect)
        intent.putExtra("defaultId", defaultId)
        activity.startActivity(intent)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            activity.overrideActivityTransition(Activity.OVERRIDE_TRANSITION_OPEN, 0, 0)
        } else {
            activity.overridePendingTransition(0, 0)
        }
    }

    override fun previewRecordMessage(
        activity: Activity,
        originSession: Session,
        message: Message
    ) {
        if (message.content != null) {
            val recordBody =
                Gson().fromJson(message.content!!, IMRecordMsgBody::class.java) ?: return

            val subscriber = object : BaseSubscriber<List<Message>>() {
                override fun onNext(t: List<Message>) {
                    val intent = Intent(activity, IMRecordPreviewActivity::class.java)
                    val arrayList = arrayListOf<Message>()
                    arrayList.addAll(t)
                    intent.putParcelableArrayListExtra("recordMessages", arrayList)
                    val session = Session()
                    session.type = SessionType.MsgRecord.value
                    intent.putExtra("session", session)
                    intent.putExtra("originSession", originSession)
                    intent.putExtra("title", recordBody.title)
                    activity.startActivity(intent)
                }
            }

            Flowable.just(recordBody.messages).flatMap {
                val dbMessages = mutableListOf<Message>()
                for (m in it) {
                    val dbMsg = IMCoreManager.getImDataBase().messageDao().findByMsgId(
                        m.msgId,
                        m.sid
                    )
                    if (dbMsg == null) {
                        IMCoreManager.getImDataBase().messageDao().insertOrIgnore(listOf(m))
                        dbMessages.add(m)
                    } else {
                        dbMessages.add(dbMsg)
                    }
                }
                dbMessages.sortBy { msg ->
                    msg.cTime
                }
                return@flatMap Flowable.just(dbMessages)
            }.compose(RxTransform.flowableToMain())
                .subscribe(subscriber)
        }

    }

    override fun setTokenForEndpoint(endPoint: String, token: String) {
        VideoCache.addEndpointToken(endPoint, token)
    }
}