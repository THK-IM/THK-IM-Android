package com.thk.im.android.ui.search

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import com.thk.im.android.api.DataRepository
import com.thk.im.android.api.group.vo.GroupVo
import com.thk.im.android.api.user.vo.UserBasicInfoVo
import com.thk.im.android.core.base.BaseSubscriber
import com.thk.im.android.core.base.RxTransform
import com.thk.im.android.databinding.ActivitySearchBinding
import com.thk.im.android.ui.base.BaseActivity
import com.thk.im.android.ui.group.GroupActivity
import com.thk.im.android.ui.contact.ContactUserActivity

class SearchActivity : BaseActivity() {

    companion object {
        fun startSearchActivity(ctx: Context, searchType: Int) {
            val intent = Intent(ctx, SearchActivity::class.java)
            intent.putExtra("searchType", searchType)
            ctx.startActivity(intent)
        }
    }

    private lateinit var binding: ActivitySearchBinding
    private var searchType = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivitySearchBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        searchType = intent.getIntExtra("searchType", 0)

        binding.etKeywords.setText("d86s4l3scyn5")
        binding.etKeywords.requestFocus()

        binding.etKeywords.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                search(v.text.toString())
            }
            false
        }
    }

    private fun search(keywords: String) {
        if (searchType == 1) {
            searchUser(keywords)
        } else if (searchType == 2) {
            searchGroup(keywords)
        }
    }

    private fun searchGroup(keywords: String) {
        if (keywords.isNotEmpty()) {
            val subscriber = object : BaseSubscriber<GroupVo>() {
                override fun onNext(t: GroupVo?) {
                    t?.let {
                        GroupActivity.startGroupActivity(this@SearchActivity, it.toGroup())
                    }
                }

                override fun onComplete() {
                    super.onComplete()
                    dismissLoading()
                }
            }
            showLoading(true)
            DataRepository.groupApi.searchGroup(keywords)
                .compose(RxTransform.flowableToMain())
                .subscribe(subscriber)
            addDispose(subscriber)
        }
    }

    private fun searchUser(keywords: String) {
        if (keywords.isNotEmpty()) {
            val subscriber = object : BaseSubscriber<UserBasicInfoVo>() {
                override fun onNext(t: UserBasicInfoVo?) {
                    t?.let {
                        ContactUserActivity.startContactUserActivity(this@SearchActivity, it.toUser())
                    }
                }

                override fun onComplete() {
                    super.onComplete()
                    dismissLoading()
                }
            }
            showLoading(true)
            DataRepository.userApi.searchUserByDisplayId(keywords)
                .compose(RxTransform.flowableToMain())
                .subscribe(subscriber)
            addDispose(subscriber)
        }
    }
}