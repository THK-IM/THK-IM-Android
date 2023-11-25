package com.thk.im.android.core.api.bean

import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.thk.im.android.core.db.entity.Session
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class SessionBean(
    @SerializedName("s_id")
    var id: Long = 0,
    @SerializedName("entity_id")
    var entityId: Long = 0,
    @SerializedName("type")
    var type: Int = 0,
    @SerializedName("name")
    var name: String = "",
    @SerializedName("remark")
    var remark: String = "",
    @SerializedName("top")
    var top: Long = 0,
    @SerializedName("role")
    var role: Int = 0,
    @SerializedName("status")
    var status: Int = 0,
    @SerializedName("mute")
    var mute: Int = 0,
    @SerializedName("ext_data")
    var extData: String? = null,
    @SerializedName("c_time")
    var cTime: Long = 0,
    @SerializedName("m_time")
    var mTime: Long = 0,
) : Parcelable {
    fun toSession(): Session {
        return Session(
            id, type, entityId, name, remark, mute, status, role, top, cTime,
            mTime, 0, null, null, extData
        )
    }

    companion object {
        fun buildSessionBean(session: Session): SessionBean {
            val sessionBean = SessionBean()
            sessionBean.id = session.id
            sessionBean.type = session.type
            sessionBean.entityId = session.entityId
            sessionBean.top = session.topTimestamp
            sessionBean.status = session.status
            sessionBean.cTime = session.cTime
            sessionBean.mTime = session.mTime
            return sessionBean
        }
    }
}