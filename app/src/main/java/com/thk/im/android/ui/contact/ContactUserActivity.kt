package com.thk.im.android.ui.contact

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.Toolbar
import com.thk.im.android.api.DataRepository
import com.thk.im.android.api.contact.vo.ApplyFriendVo
import com.thk.im.android.api.contact.vo.ContactSessionCreateVo
import com.thk.im.android.api.contact.vo.FollowVo
import com.thk.im.android.core.IMCoreManager
import com.thk.im.android.core.base.BaseSubscriber
import com.thk.im.android.core.base.IMImageLoader
import com.thk.im.android.core.base.LLog
import com.thk.im.android.core.base.RxTransform
import com.thk.im.android.core.base.extension.setShape
import com.thk.im.android.core.db.entity.Session
import com.thk.im.android.core.db.entity.User
import com.thk.im.android.databinding.ActivityUserBinding
import com.thk.im.android.ui.base.BaseActivity
import com.thk.im.android.ui.manager.IMUIManager
import io.reactivex.Flowable

class ContactUserActivity : BaseActivity() {

    companion object {
        fun startContactUserActivity(ctx: Context, user: User) {
            val intent = Intent(ctx, ContactUserActivity::class.java)
            intent.putExtra("user", user)
            ctx.startActivity(intent)
        }
    }

    private lateinit var binding: ActivityUserBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityUserBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val user = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("user", User::class.java)
        } else {
            intent.getParcelableExtra("user")
        }

        user?.let {
            initUserInfoView(it)
        }
    }

    override fun getToolbar(): Toolbar {
        return binding.tbTop.toolbar
    }

    override fun needBackIcon(): Boolean {
        return true
    }

    override fun menuMoreVisibility(id: Int): Int {
        return View.GONE
    }

    private fun initUserInfoView(user: User) {
        user.avatar?.let {
            IMImageLoader.displayImageUrl(binding.ivAvatar, it)
        }
        binding.tvNickname.text = user.nickname
        binding.tvId.text = user.displayId

        binding.tvFollow.visibility = View.VISIBLE
        binding.tvFollow.setShape(
            Color.parseColor("#3282F6"), floatArrayOf(10f, 10f, 10f, 10f)
        )

        binding.tvFollow.setOnClickListener {
            followUser(user)
        }


        binding.tvAddFriend.visibility = View.VISIBLE
        binding.tvAddFriend.setShape(
            Color.parseColor("#3282F6"), floatArrayOf(10f, 10f, 10f, 10f)
        )

        binding.tvAddFriend.setOnClickListener {
            applyFriend(user)
        }

        binding.tvSendMsg.visibility = View.VISIBLE
        binding.tvSendMsg.setShape(
            Color.parseColor("#3282F6"), floatArrayOf(10f, 10f, 10f, 10f)
        )

        binding.tvSendMsg.setOnClickListener {
            startMessage(user)
        }
    }

    private fun followUser(user: User) {
        val subscriber = object : BaseSubscriber<Void>() {
            override fun onError(t: Throwable?) {
                t?.message?.let {
                    LLog.e(it)
                }
                removeDispose(this)
            }

            override fun onComplete() {
                super.onComplete()
                removeDispose(this)
                dismissLoading()
                showToast("关注成功")
            }

            override fun onNext(t: Void?) {}
        }

        showLoading()
        val vo = FollowVo(IMCoreManager.uId, user.id)
        DataRepository.contactApi.follow(vo)
            .compose(RxTransform.flowableToMain())
            .subscribe(subscriber)
        addDispose(subscriber)
    }

    private fun applyFriend(user: User) {
        val subscriber = object : BaseSubscriber<Void>() {
            override fun onNext(t: Void?) {
            }

            override fun onError(t: Throwable?) {
                t?.message?.let {
                    LLog.e(it)
                }
                removeDispose(this)
            }

            override fun onComplete() {
                super.onComplete()
                removeDispose(this)
                dismissLoading()
                showToast("申请成功")
            }
        }
        addDispose(subscriber)

        showLoading()
        val vo = ApplyFriendVo(IMCoreManager.uId, user.id, 1, "i am vizoss")
        DataRepository.contactApi.applyFriend(vo)
            .compose(RxTransform.flowableToMain())
            .subscribe(subscriber)
    }

    private fun startMessage(user: User) {
        val uId = DataRepository.getUserId() ?: return
        val req = ContactSessionCreateVo(uId, user.id)
        val subscriber = object : BaseSubscriber<Session>() {
            override fun onNext(t: Session) {
                IMUIManager.pageRouter?.openSession(this@ContactUserActivity, t)
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
                    .insertOrReplace(listOf(session))
                return@flatMap Flowable.just(session)
            }
            .compose(RxTransform.flowableToMain())
            .subscribe(subscriber)
        addDispose(subscriber)
    }
}