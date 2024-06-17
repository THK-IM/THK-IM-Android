package com.thk.im.android.ui.fragment.viewholder.session

import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.emoji2.widget.EmojiTextView
import androidx.lifecycle.LifecycleOwner
import com.thk.im.android.core.IMCoreManager
import com.thk.im.android.core.SessionStatus
import com.thk.im.android.core.db.entity.Session
import com.thk.im.android.core.module.GroupModule
import com.thk.im.android.core.module.UserModule
import com.thk.im.android.ui.R
import com.thk.im.android.ui.fragment.viewholder.IMBaseVH
import com.thk.im.android.ui.protocol.internal.IMSessionVHOperator


abstract class IMBaseSessionVH(liftOwner: LifecycleOwner, itemView: View) :
    IMBaseVH(liftOwner, itemView) {

    val nickView: AppCompatTextView = itemView.findViewById(R.id.tv_nickname)
    val avatarView: AppCompatImageView = itemView.findViewById(R.id.iv_avatar)
    val lastMsgView: EmojiTextView = itemView.findViewById(R.id.tv_last_message)
    val lastTimeView: AppCompatTextView = itemView.findViewById(R.id.tv_last_time)
    val unReadCountView: AppCompatTextView = itemView.findViewById(R.id.tv_unread_count)

    val statusView: AppCompatImageView = itemView.findViewById(R.id.iv_session_status)
    val deleteView = itemView.findViewById<TextView>(R.id.tv_session_delete)
    val topView = itemView.findViewById<TextView>(R.id.tv_session_top)
    val muteView = itemView.findViewById<TextView>(R.id.tv_session_mute)
    val container = itemView.findViewById<ConstraintLayout>(R.id.cl_container)

    lateinit var session: Session
    private var sessionVHOperator: IMSessionVHOperator? = null

    /**
     * ViewHolder 绑定数据触发设置界面ui
     */
    open fun onViewBind(session: Session, sessionVHOperator: IMSessionVHOperator) {
        this.session = session
        this.sessionVHOperator = sessionVHOperator
        if (session.status.and(SessionStatus.Silence.value) > 0) {
            statusView.visibility = View.VISIBLE
            muteView.text = itemView.context.getString(R.string.cancel_silence)
        } else {
            statusView.visibility = View.GONE
            muteView.text = itemView.context.getString(R.string.silence)
        }
        if (session.topTimestamp > 0) {
            topView.text = itemView.context.getString(R.string.cancel_top)
        } else {
            topView.text = itemView.context.getString(R.string.top)
        }
        deleteView.setOnClickListener {
            this.sessionVHOperator?.deleteSession(this.session)
        }
        muteView.setOnClickListener {
            this.session.status = this.session.status.xor(SessionStatus.Silence.value)
            this.sessionVHOperator?.updateSession(this.session)
        }
        topView.setOnClickListener {
            if (this.session.topTimestamp > 0) {
                this.session.topTimestamp = 0
            } else {
                this.session.topTimestamp = IMCoreManager.severTime
            }
            this.sessionVHOperator?.updateSession(this.session)
        }
        container.setOnClickListener {
            this.sessionVHOperator?.openSession(this.session)
        }
    }

    fun getUserModule(): UserModule {
        return IMCoreManager.userModule
    }

    fun getGroupModule(): GroupModule {
        return IMCoreManager.groupModule
    }

    override fun onViewRecycled() {
        super.onViewRecycled()
        sessionVHOperator = null
    }


}