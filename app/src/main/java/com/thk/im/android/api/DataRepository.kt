package com.thk.im.android.api;

import android.app.Application
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.thk.im.android.api.contact.ContactApi
import com.thk.im.android.api.user.UserApi
import com.thk.im.android.api.user.vo.User
import com.thk.im.android.constant.Host

object DataRepository {

    lateinit var app: Application

    lateinit var userApi: UserApi

    lateinit var contactApi: ContactApi

    fun init(app: Application) {
        this.app = app
        val token = getUserToken() ?: ""
        ApiFactory.init(app, token)
        userApi = ApiFactory.createApi(UserApi::class.java, Host.UserAPI)
        contactApi = ApiFactory.createApi(ContactApi::class.java, Host.ContactAPI)
    }

    fun getUserToken(): String? {
        val sp = app.getSharedPreferences("UserInfo", AppCompatActivity.MODE_PRIVATE)
        return sp.getString("Token", "")
    }

    fun getUserId(): Long {
        val sp = app.getSharedPreferences("UserInfo", AppCompatActivity.MODE_PRIVATE)
        return sp.getLong("UserId", 0)
    }

    fun getUser(): User? {
        val sp = app.getSharedPreferences("UserInfo", AppCompatActivity.MODE_PRIVATE)
        val userId = sp.getLong("UserId", 0)
        if (userId == 0L) {
            return null
        }
        val userInfoJson = sp.getString("User:$userId", "")
        return Gson().fromJson(userInfoJson, User::class.java)
    }

    fun saveUserInfo(token: String, user: User) {
        val sp = app.getSharedPreferences("UserInfo", AppCompatActivity.MODE_PRIVATE)
        val editor = sp.edit()
        editor.putString("Token", token)
        editor.putLong("UserId", user.id)
        editor.putString("User:${user.id}", Gson().toJson(user))
        editor.apply()
    }

}