package com.thk.im.android.ui.fragment.viewholder.sessionmember

import android.view.View
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import com.thk.im.android.core.IMCoreManager
import com.thk.im.android.core.base.BaseSubscriber
import com.thk.im.android.core.base.IMImageLoader
import com.thk.im.android.core.base.RxTransform
import com.thk.im.android.core.db.entity.SessionMember
import com.thk.im.android.core.db.entity.User
import com.thk.im.android.ui.R
import com.thk.im.android.ui.fragment.adapter.IMOnSessionMemberClick

class IMSessionMemberVH(itemView: View): RecyclerView.ViewHolder(itemView) {

    private val avatarView: AppCompatImageView
    private val nicknameView: AppCompatTextView
    private var onSessionMemberClick: IMOnSessionMemberClick? = null
    private var user: User? = null
    private var sessionMember: SessionMember? = null
    init {
        avatarView = itemView.findViewById(R.id.iv_avatar)
        nicknameView = itemView.findViewById(R.id.tv_nickname)
    }

    fun bindSessionMember(sessionMember: SessionMember, onSessionMemberClick: IMOnSessionMemberClick?) {
        this.sessionMember = sessionMember
        this.onSessionMemberClick = onSessionMemberClick
        val subscriber = object : BaseSubscriber<User>() {
            override fun onNext(t: User?) {
                t?.let {
                    showUser(it)
                }
            }

        }
        IMCoreManager.userModule.queryUser(sessionMember.userId)
            .compose(RxTransform.flowableToMain())
            .subscribe(subscriber)
        itemView.setOnClickListener {
            onClick()
        }
    }

    private fun showUser(user: User) {
        this.user = user
        user.avatar?.let {
            IMImageLoader.displayImageUrl(avatarView, it)
        }
        nicknameView.text = user.nickname
    }

    private fun onClick() {
        if (this.onSessionMemberClick == null || this.sessionMember == null || this.user == null) {
            return
        }
        this.onSessionMemberClick!!.onSessionMemberClick(sessionMember!!, user!!)
    }
}