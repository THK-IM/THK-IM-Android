package com.thk.im.android.ui.welcome

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import com.google.gson.Gson
import com.thk.im.android.IMApplication
import com.thk.im.android.api.ApiFactory
import com.thk.im.android.core.IMCoreManager
import com.thk.im.android.core.SignalStatus
import com.thk.im.android.core.base.BaseSubscriber
import com.thk.im.android.core.base.RxTransform
import com.thk.im.android.core.base.extension.setShape
import com.thk.im.android.databinding.ActivityWelcomeBinding
import com.thk.im.android.ui.base.BaseActivity
import com.thk.im.android.ui.main.MainActivity
import com.thk.im.android.ui.welcome.api.WelcomeApi
import com.thk.im.android.ui.welcome.api.vo.LoginResp
import com.thk.im.android.ui.welcome.api.vo.TokenLoginReq
import com.thk.im.android.ui.welcome.api.vo.User
import com.thk.im.android.ui.welcome.api.vo.UserRegisterReq
import com.thk.im.android.ui.welcome.api.vo.UserRegisterResp

class WelcomeActivity : BaseActivity() {

    private lateinit var binding: ActivityWelcomeBinding
    private val welcomeApi =
        ApiFactory.createApi(WelcomeApi::class.java, "http://user-api.thkim.com")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWelcomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initUserInfo()
    }

    private fun gotoMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun initUserInfo() {
        val sp = getSharedPreferences("UserInfo", MODE_PRIVATE)
        val token = sp.getString("Token", "")
        if (token.isNullOrEmpty()) {
            showLogin()
        } else {
            val userId = sp.getLong("UserId", 0)
            if (userId == 0L) {
                loginByToken(token)
            } else {
                val userInfoJson = sp.getString("User:$userId", "")
                if (userInfoJson.isNullOrEmpty()) {
                    loginByToken(token)
                } else {
                    val user = Gson().fromJson(userInfoJson, User::class.java)
                    initIM(token, user.id)
                }
            }
        }
    }

    private fun saveUserInfo(token: String, user: User) {
        ApiFactory.updateToken(token)
        val sp = getSharedPreferences("UserInfo", MODE_PRIVATE)
        val editor = sp.edit()
        editor.putString("Token", token)
        editor.putLong("UserId", user.id)
        editor.putString("User:${user.id}", Gson().toJson(user))
        editor.apply()
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
        welcomeApi.register(UserRegisterReq())
            .compose(RxTransform.flowableToIo())
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
        welcomeApi.loginByToken(TokenLoginReq(token))
            .compose(RxTransform.flowableToIo())
            .subscribe(subscriber)
    }

    private fun initIM(token: String, uId: Long) {
        if (IMCoreManager.inited) {
            gotoMainActivity()
        } else {
            (application as IMApplication).initIM(token, uId)
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