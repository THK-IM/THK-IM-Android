package com.thk.im.android.core.base

import android.app.Application
import android.content.Context
import android.content.res.Resources
import android.os.Build
import android.os.LocaleList
import android.text.TextUtils
import android.util.DisplayMetrics
import java.util.Locale


object LanguageUtils {

    private val spLanguage = "Language"
    private val keyLanguage = "language"
    private val keyCountry = "country"

    private lateinit var mApp: Application

    fun init(application: Application) {
        this.mApp = application
    }

    private fun getSpLanguage(): String {
        val sp = mApp.getSharedPreferences(spLanguage, Context.MODE_PRIVATE)
        return sp.getString(keyLanguage, "") ?: ""
    }

    private fun getSpLanguage(ctx: Context): String {
        val sp = ctx.getSharedPreferences(spLanguage, Context.MODE_PRIVATE)
        return sp.getString(keyLanguage, "") ?: ""
    }


    private fun getSpCountry(): String {
        val sp = mApp.getSharedPreferences(spLanguage, Context.MODE_PRIVATE)
        return sp.getString(keyCountry, "") ?: ""
    }

    private fun getSpCountry(ctx: Context): String {
        val sp = ctx.getSharedPreferences(spLanguage, Context.MODE_PRIVATE)
        return sp.getString(keyCountry, "") ?: ""
    }


    /**
     * 修改应用内语言设置
     * @param language  语言
     * @param area      地区
     */
    fun changeLanguage(context: Context, language: String, area: String): Boolean {
        val sp = context.getSharedPreferences(spLanguage, Context.MODE_PRIVATE)
        val editor = sp.edit()
        editor.putString(keyLanguage, language)
        editor.putString(keyCountry, area)
        return editor.commit()
    }

    /**
     * 更新应用语言（核心）
     * @param context
     * @param locale
     */
    private fun setAppLanguage(context: Context, locale: Locale): Context {
        val resources: Resources = context.resources
        val metrics: DisplayMetrics = resources.displayMetrics
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            context.resources.configuration.setLocale(locale)
            context.resources.configuration.setLocales(LocaleList(locale))
            val newContext = context.createConfigurationContext(resources.configuration)
            newContext.resources.updateConfiguration(resources.configuration, metrics)
            return newContext
        } else {
            context.resources.configuration.setLocale(locale)
            context.resources.updateConfiguration(resources.configuration, metrics)
            return context
        }
    }

    /**
     * 跟随系统语言
     */
    fun attachBaseContext(context: Context?): Context? {
        context?.let {
            val spLanguage = getSpLanguage(it)
            val spCountry = getSpCountry(it)
            if (!TextUtils.isEmpty(spLanguage)) {
                val locale = Locale(spLanguage, spCountry)
                return setAppLanguage(it, locale)
            } else {
                val locale = Locale.getDefault()
                return setAppLanguage(it, locale)
            }
        }
        return null
    }

    fun getAppLocale(): Locale {
        val language = getSpLanguage()
        val country = getSpCountry()
        if (language == "") {
            return Locale.getDefault()
        }
        return Locale(language, country)
    }

    fun getSpLocale(): Locale {
        val language = getSpLanguage()
        val country = getSpCountry()
        return Locale(language, country)
    }


}

