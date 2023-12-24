package com.thk.im.android.ui.welcome

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import com.thk.im.android.IMApplication
import com.thk.im.android.api.ApiFactory
import com.thk.im.android.api.UserRepository
import com.thk.im.android.api.user.vo.LoginResp
import com.thk.im.android.api.user.vo.TokenLoginReq
import com.thk.im.android.api.user.vo.User
import com.thk.im.android.api.user.vo.UserRegisterReq
import com.thk.im.android.api.user.vo.UserRegisterResp
import com.thk.im.android.core.SignalStatus
import com.thk.im.android.core.base.BaseSubscriber
import com.thk.im.android.core.base.RxTransform
import com.thk.im.android.core.base.extension.setShape
import com.thk.im.android.databinding.ActivityWelcomeBinding
import com.thk.im.android.ui.base.BaseActivity
import com.thk.im.android.ui.main.MainActivity

class WelcomeActivity : BaseActivity() {

    private lateinit var binding: ActivityWelcomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityWelcomeBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        initUserInfo()
    }

    private fun gotoMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun initUserInfo() {
        val token = UserRepository.getUserToken()
        if (token.isNullOrEmpty()) {
            showLogin()
        } else {
            val user = UserRepository.getUser()
            if (user == null) {
                loginByToken(token)
            } else {
                initIM(token, user.id)
            }
        }
    }

    private fun saveUserInfo(token: String, user: User) {
        ApiFactory.updateToken(token)
        UserRepository.saveUserInfo(token, user)
        initIM(token, user.id)
    }

    private fun showLogin() {
        binding.btnLogin.setShape(Color.CYAN, Color.CYAN, 1, floatArrayOf(10f, 10f, 10f, 10f))
        binding.btnRegister.setShape(Color.BLACK, Color.BLACK, 1, floatArrayOf(10f, 10f, 10f, 10f))
        binding.btnQuickRegister.setShape(Color.RED, Color.RED, 1, floatArrayOf(10f, 10f, 10f, 10f))
        binding.btnLogin.setOnClickListener {

        }
        binding.btnRegister.setOnClickListener {
        }

        binding.btnQuickRegister.setOnClickListener {
            quickRegister()
        }
        binding.btnLogin.visibility = View.VISIBLE
        binding.btnRegister.visibility = View.VISIBLE
        binding.btnQuickRegister.visibility = View.VISIBLE
    }

    private fun quickRegister() {
        showLoading()
        val subscriber = object : BaseSubscriber<UserRegisterResp>() {
            override fun onNext(t: UserRegisterResp?) {
                t?.let {
                    saveUserInfo(it.token, it.user)
                }
            }

            override fun onComplete() {
                super.onComplete()
                dismissLoading()
                removeDispose(this)
            }

        }
        UserRepository.userApi.register(UserRegisterReq())
            .compose(RxTransform.flowableToMain())
            .subscribe(subscriber)
    }

    private fun loginByToken(token: String) {
        ApiFactory.updateToken(token)
        showLoading()
        val subscriber = object : BaseSubscriber<LoginResp>() {
            override fun onNext(t: LoginResp?) {
                t?.let {
                    val newToken = it.token ?: token
                    saveUserInfo(newToken, it.user)
                }
            }

            override fun onComplete() {
                super.onComplete()
                dismissLoading()
                removeDispose(this)
            }

        }
        UserRepository.userApi.loginByToken(TokenLoginReq(token))
            .compose(RxTransform.flowableToMain())
            .subscribe(subscriber)
    }

    private fun initIM(token: String, uId: Long) {
        val success = (application as IMApplication).initIM(token, uId)
        if (success) {
            gotoMainActivity()
        }
    }

    override fun onConnectStatus(status: Int) {
        super.onConnectStatus(status)
        when (status) {
            SignalStatus.Connected.value -> {
                gotoMainActivity()
            }

            SignalStatus.Connecting.value -> {
                showToast("连接中")
            }

            SignalStatus.Disconnected.value -> {
                showToast("连接失败")
            }
        }
    }
}