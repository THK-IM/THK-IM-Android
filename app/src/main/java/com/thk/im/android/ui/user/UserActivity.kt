package com.thk.im.android.ui.user

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import com.thk.im.android.api.DataRepository
import com.thk.im.android.api.contact.vo.ContactSessionCreateVo
import com.thk.im.android.api.user.vo.BasicUserInfo
import com.thk.im.android.core.IMCoreManager
import com.thk.im.android.core.base.BaseSubscriber
import com.thk.im.android.core.base.IMImageLoader
import com.thk.im.android.core.base.RxTransform
import com.thk.im.android.core.base.extension.setShape
import com.thk.im.android.core.db.entity.Session
import com.thk.im.android.databinding.ActivityUserBinding
import com.thk.im.android.ui.base.BaseActivity
import com.thk.im.android.ui.manager.IMUIManager
import io.reactivex.Flowable

class UserActivity : BaseActivity() {

    companion object {
        fun startUserActivity(ctx: Context, basicInfo: BasicUserInfo) {
            val intent = Intent(ctx, UserActivity::class.java)
            intent.putExtra("user", basicInfo)
            ctx.startActivity(intent)
        }
    }

    private lateinit var binding: ActivityUserBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityUserBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val userInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("user", BasicUserInfo::class.java)
        } else {
            intent.getParcelableExtra("user")
        }

        userInfo?.let {
            initView(it)
        }
    }

    private fun initView(userInfo: BasicUserInfo) {
        userInfo.avatar?.let {
            IMImageLoader.displayImageUrl(binding.ivAvatar, it)
        }
        if (userInfo.nickname.isNullOrEmpty()) {
            binding.tvNickname.text = "无名"
        } else {
            binding.tvNickname.text = userInfo.nickname
        }
        binding.tvId.text = userInfo.displayId

        binding.tvAddFriend.visibility = View.VISIBLE
        binding.tvAddFriend.setShape(
            Color.parseColor("#3282F6"),
            Color.parseColor("#3282F6"),
            1,
            floatArrayOf(10f, 10f, 10f, 10f)
        )

        binding.tvAddFriend.setOnClickListener {
        }

        binding.tvSendMsg.visibility = View.VISIBLE
        binding.tvSendMsg.setShape(
            Color.parseColor("#3282F6"),
            Color.parseColor("#3282F6"),
            1,
            floatArrayOf(10f, 10f, 10f, 10f)
        )

        binding.tvSendMsg.setOnClickListener {
            startMessage(userInfo)
        }
    }

    private fun startMessage(userInfo: BasicUserInfo) {
        val uId = DataRepository.getUserId()
        val req = ContactSessionCreateVo(uId, userInfo.id)
        val subscriber = object : BaseSubscriber<Session>() {
            override fun onNext(t: Session) {
                IMUIManager.sessionOperator?.openSession(this@UserActivity, t)
            }

            override fun onComplete() {
                super.onComplete()
                removeDispose(this)
            }
        }
        DataRepository.contactApi.createContactSession(req)
            .flatMap { vo ->
                val session = vo.toSession()
                IMCoreManager.getImDataBase().sessionDao()
                    .insertOrUpdateSessions(session)
                return@flatMap Flowable.just(session)
            }
            .compose(RxTransform.flowableToMain())
            .subscribe(subscriber)
        addDispose(subscriber)
    }
}