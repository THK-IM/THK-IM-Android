package com.thk.im.android.ui.main

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.core.view.get
import com.thk.im.android.R
import com.thk.im.android.api.UserRepository
import com.thk.im.android.core.IMCoreManager.getImDataBase
import com.thk.im.android.core.IMCoreManager.messageModule
import com.thk.im.android.core.base.BaseSubscriber
import com.thk.im.android.core.base.RxTransform.flowableToMain
import com.thk.im.android.core.db.entity.Session
import com.thk.im.android.databinding.ActivityMainBinding
import com.thk.im.android.ui.base.BaseActivity
import com.thk.im.android.ui.main.adpater.MainFragmentAdapter
import io.reactivex.Flowable

class MainActivity : BaseActivity() {


    private lateinit var binding: ActivityMainBinding

    private var chooseMenuIndex = 0
    private var bottomMenuTitles = setOf("message", "contact", "daily", "mine")

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityMainBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val adapter = MainFragmentAdapter(this)
        binding.vpContainer.adapter = adapter
        binding.vpContainer.isUserInputEnabled = false

        binding.bnvBottom.setOnItemSelectedListener {
            onBottomItemSelected(it)
        }
        binding.bnvBottom.selectedItemId = binding.bnvBottom[chooseMenuIndex].id
        setTitle(bottomMenuTitles.elementAt(chooseMenuIndex))
    }

    private fun onBottomItemSelected(it: MenuItem): Boolean {
        val pos = bottomMenuTitles.indexOf(it.title)
        binding.vpContainer.setCurrentItem(pos, false)
        setTitle(bottomMenuTitles.elementAt(pos))
        if (pos == 3) {
            binding.tbTop.toolbar.visibility = View.GONE
        } else {
            binding.tbTop.toolbar.visibility = View.VISIBLE
        }
        return false
    }

    override fun getToolbar(): Toolbar {
        return binding.tbTop.toolbar
    }

    override fun onToolBarMenuClick(view: View) {
        if (view.id == R.id.tb_more) {
            createSession(4L)
        }
    }

    private fun createSession(contactId: Long) {
        val uId = UserRepository.getUserId()
        if (uId <= 0L) {
            return
        }
        val subscriber: BaseSubscriber<Session> = object : BaseSubscriber<Session>() {
            override fun onNext(t: Session?) {

            }

            override fun onComplete() {
                super.onComplete()
                removeDispose(this)
            }
        }
        messageModule
            .createSingleSession(contactId)
            .flatMap { session ->
                getImDataBase().sessionDao()
                    .insertOrUpdateSessions(session)
                Flowable.just(session)
            }
            .compose(flowableToMain<Session>())
            .subscribe(subscriber)
        addDispose(subscriber)
    }


}