package com.thk.im.android.core.bean

import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.thk.im.android.db.entity.Session
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class SessionBean(
    @SerializedName("session_id")
    var id: Long = 0,
    @SerializedName("type")
    var type: Int = 0,
    @SerializedName("entity_id")
    var entityId: Long = 0,
    @SerializedName("top")
    var top: Long = 0,
    @SerializedName("status")
    var status: Int = 0,
    @SerializedName("c_time")
    var cTime: Long = 0,
    @SerializedName("m_time")
    var mTime: Long = 0,
    @SerializedName("ext_data")
    var extData: String = "",
    @SerializedName("last_msg")
    var lastMsg: String = ""
) : Parcelable {
    fun toSession(): Session {
        return Session(
            id, type, entityId, status, top, cTime, mTime, extData, lastMsg
        )
    }

    companion object {
        fun buildSessionBean(session: Session): SessionBean {
            val sessionBean = SessionBean()
            sessionBean.id = session.id
            sessionBean.type = session.type
            sessionBean.entityId = session.entityId
            sessionBean.top = session.topTime
            sessionBean.status = session.status
            sessionBean.cTime = session.cTime
            sessionBean.mTime = session.mTime
            sessionBean.extData = session.ext_data.orEmpty()
            sessionBean.lastMsg = session.lastMsg.orEmpty()
            return sessionBean
        }
    }
}