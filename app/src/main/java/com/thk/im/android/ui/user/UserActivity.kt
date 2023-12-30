package com.thk.im.android.ui.user

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import com.thk.im.android.api.DataRepository
import com.thk.im.android.api.contact.vo.ApplyFriendVo
import com.thk.im.android.api.contact.vo.ContactSessionCreateVo
import com.thk.im.android.api.contact.vo.FollowVo
import com.thk.im.android.api.user.vo.BasicUserVo
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
        fun startUserActivity(ctx: Context, basicInfo: BasicUserVo) {
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
            intent.getParcelableExtra("user", BasicUserVo::class.java)
        } else {
            intent.getParcelableExtra("user")
        }

        userInfo?.let {
            initView(it)
        }
    }

    private fun initView(userInfo: BasicUserVo) {
        userInfo.avatar?.let {
            IMImageLoader.displayImageUrl(binding.ivAvatar, it)
        }
        if (userInfo.nickname.isNullOrEmpty()) {
            binding.tvNickname.text = "无名"
        } else {
            binding.tvNickname.text = userInfo.nickname
        }
        binding.tvId.text = userInfo.displayId

        binding.tvFollow.visibility = View.VISIBLE
        binding.tvFollow.setShape(
            Color.parseColor("#3282F6"),
            Color.parseColor("#3282F6"),
            1,
            floatArrayOf(10f, 10f, 10f, 10f)
        )

        binding.tvFollow.setOnClickListener {
            followUser(userInfo)
        }


        binding.tvAddFriend.visibility = View.VISIBLE
        binding.tvAddFriend.setShape(
            Color.parseColor("#3282F6"),
            Color.parseColor("#3282F6"),
            1,
            floatArrayOf(10f, 10f, 10f, 10f)
        )

        binding.tvAddFriend.setOnClickListener {
            applyFriend(userInfo)
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

    private fun followUser(userInfo: BasicUserVo) {
        val subscriber = object: BaseSubscriber<Void>() {
            override fun onNext(t: Void?) {
                showToast("关注成功")
            }

            override fun onComplete() {
                super.onComplete()
                dismissLoading()
            }
        }

        showLoading()
        val vo = FollowVo(IMCoreManager.uId, userInfo.id)
        DataRepository.contactApi.follow(vo)
            .compose(RxTransform.flowableToMain())
            .subscribe(subscriber)
    }

    private fun applyFriend(userInfo: BasicUserVo) {
        val subscriber = object: BaseSubscriber<Void>() {
            override fun onNext(t: Void?) {
                showToast("申请成功")
            }

            override fun onComplete() {
                super.onComplete()
                dismissLoading()
            }
        }

        showLoading()
        val vo = ApplyFriendVo(IMCoreManager.uId, userInfo.id, 1, "i am vizoss")
        DataRepository.contactApi.applyFriend(vo)
            .compose(RxTransform.flowableToMain())
            .subscribe(subscriber)
    }

    private fun startMessage(userInfo: BasicUserVo) {
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