package com.thk.im.android.ui.welcome

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import com.thk.im.android.IMApplication
import com.thk.im.android.api.ApiFactory
import com.thk.im.android.api.DataRepository
import com.thk.im.android.api.user.vo.LoginVo
import com.thk.im.android.api.user.vo.TokenLoginReq
import com.thk.im.android.api.user.vo.UserBasicInfoVo
import com.thk.im.android.api.user.vo.RegisterReq
import com.thk.im.android.api.user.vo.RegisterVo
import com.thk.im.android.api.user.vo.UserVo
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
        val token = DataRepository.getUserToken()
        if (token.isNullOrEmpty()) {
            loginByToken("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJleHBpcmVzQXQiOjE3MDQxOTczNzAsImlkIjoiZDg3OWc2a2NzazB3IiwiaXNzdWVyIjoidXNlcl9zZXJ2ZXIifQ.c3phZZ0o7Hjunv9ceffqq4ghB2Fj0vQw443UFghWMSg")
//            showLogin()
        } else {
            val user = DataRepository.getUser()
            if (user == null) {
                loginByToken(token)
            } else {
                initIM(token, user.id)
            }
        }
    }

    private fun saveUserInfo(token: String, userVo: UserVo) {
        ApiFactory.updateToken(token)
        DataRepository.saveUserInfo(token, userVo)
        initIM(token, userVo.id)
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
        val subscriber = object : BaseSubscriber<RegisterVo>() {
            override fun onNext(t: RegisterVo?) {
                t?.let {
                    saveUserInfo(it.token, it.userVo)
                }
            }

            override fun onComplete() {
                super.onComplete()
                dismissLoading()
                removeDispose(this)
            }

        }
        DataRepository.userApi.register(RegisterReq())
            .compose(RxTransform.flowableToMain())
            .subscribe(subscriber)
    }

    private fun loginByToken(token: String) {
        ApiFactory.updateToken(token)
        showLoading()
        val subscriber = object : BaseSubscriber<LoginVo>() {
            override fun onNext(t: LoginVo?) {
                t?.let {
                    val newToken = it.token ?: token
                    saveUserInfo(newToken, it.userVo)
                }
            }

            override fun onComplete() {
                super.onComplete()
                dismissLoading()
                removeDispose(this)
            }

        }
        DataRepository.userApi.loginByToken(TokenLoginReq(token))
            .compose(RxTransform.flowableToMain())
            .subscribe(subscriber)
    }

    private fun initIM(token: String, uId: Long) {
        (application as IMApplication).initIM(token, uId)
        gotoMainActivity()
//        if (success) {
//        }
    }

    override fun onConnectStatus(status: Int) {
        super.onConnectStatus(status)
//        when (status) {
//            SignalStatus.Connected.value -> {
//                gotoMainActivity()
//            }
//
//            SignalStatus.Connecting.value -> {
//                showToast("连接中")
//            }
//
//            SignalStatus.Disconnected.value -> {
//                showToast("连接失败")
//            }
//        }
    }
}