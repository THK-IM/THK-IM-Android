package com.thk.im.android.ui.fragment.viewholder.sessionmember

import android.text.TextUtils
import android.view.View
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import com.thk.im.android.core.base.IMImageLoader
import com.thk.im.android.core.db.entity.SessionMember
import com.thk.im.android.core.db.entity.User
import com.thk.im.android.ui.R
import com.thk.im.android.ui.fragment.adapter.IMOnSessionMemberClick
import com.thk.im.android.ui.manager.IMUIManager

class IMSessionMemberVH(itemView: View) : RecyclerView.ViewHolder(itemView) {

    private val avatarView: AppCompatImageView = itemView.findViewById(R.id.iv_avatar)
    private val nicknameView: AppCompatTextView = itemView.findViewById(R.id.tv_nickname)
    private var onSessionMemberClick: IMOnSessionMemberClick? = null
    private lateinit var member: Pair<User, SessionMember?>

    fun bindSessionMember(
        member: Pair<User, SessionMember?>,
        onSessionMemberClick: IMOnSessionMemberClick?
    ) {
        this.member = member
        this.onSessionMemberClick = onSessionMemberClick
        itemView.setOnClickListener {
            onClick()
        }
        showUser(member.first, member.second)
    }

    private fun showUser(user: User, sessionMember: SessionMember?) {
        val avatar = IMUIManager.avatarForSessionMember(user, sessionMember)
        if (avatar != null && !TextUtils.isEmpty(avatar)) {
            IMImageLoader.displayImageUrl(avatarView, avatar)
        } else {
            IMUIManager.uiResourceProvider?.avatar(user)?.let {
                avatarView.setImageResource(it)
            }
        }
        nicknameView.text = IMUIManager.nicknameForSessionMember(user, sessionMember)
    }

    private fun onClick() {
        this.onSessionMemberClick?.onSessionMemberClick(member.first, member.second)
    }
}