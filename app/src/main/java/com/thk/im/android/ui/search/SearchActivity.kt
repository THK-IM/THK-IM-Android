package com.thk.im.android.ui.search

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import com.thk.android.im.live.base.LLog
import com.thk.im.android.api.DataRepository
import com.thk.im.android.api.user.vo.BasicUserInfo
import com.thk.im.android.core.base.BaseSubscriber
import com.thk.im.android.core.base.RxTransform
import com.thk.im.android.core.exception.HttpStatusCodeException
import com.thk.im.android.databinding.ActivitySearchBinding
import com.thk.im.android.ui.base.BaseActivity
import com.thk.im.android.ui.user.UserActivity

class SearchActivity : BaseActivity() {

    companion object {
        fun startSearchActivity(ctx: Context) {
            val intent = Intent(ctx, SearchActivity::class.java)
            ctx.startActivity(intent)
        }
    }

    private lateinit var binding: ActivitySearchBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivitySearchBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.etKeywords.requestFocus()

        binding.etKeywords.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                search(v.text.toString())
            }
            false
        }
    }

    private fun search(keywords: String) {
        if (keywords.isNotEmpty()) {
            val subscriber = object : BaseSubscriber<BasicUserInfo>() {
                override fun onNext(t: BasicUserInfo?) {
                    t?.let {
                        UserActivity.startUserActivity(this@SearchActivity, it)
                    }
                }

                override fun onComplete() {
                    super.onComplete()
                    dismissLoading()
                }
            }
            showLoading(true)
            DataRepository.userApi.searchUser(keywords)
                .compose(RxTransform.flowableToMain())
                .subscribe(subscriber)
            addDispose(subscriber)
        }
    }
}