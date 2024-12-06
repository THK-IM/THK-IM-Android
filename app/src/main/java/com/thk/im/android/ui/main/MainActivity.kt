package com.thk.im.android.ui.main

import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.core.view.get
import com.thk.im.android.R
import com.thk.im.android.databinding.ActivityMainBinding
import com.thk.im.android.live.engine.LiveRTCEngine
import com.thk.im.android.ui.base.BaseActivity
import com.thk.im.android.ui.group.GroupActivity
import com.thk.im.android.ui.main.adpater.MainFragmentAdapter
import com.thk.im.android.ui.search.SearchActivity

class MainActivity : BaseActivity() {

    private var chooseMenuIndex = 0
    private var bottomMenuTitles = setOf("message", "contact", "group", "mine")
    private var currentNavigationPos = 0

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityMainBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val adapter = MainFragmentAdapter(this)
        binding.vpContainer.adapter = adapter
        binding.vpContainer.isUserInputEnabled = false

        binding.bnvBottom.setOnItemSelectedListener {
            return@setOnItemSelectedListener onBottomItemSelected(it)
        }
        binding.bnvBottom.selectedItemId = binding.bnvBottom[chooseMenuIndex].id
        setTitle(bottomMenuTitles.elementAt(chooseMenuIndex))

        try {
            val afd = assets.openFd("sample-111s.mp3")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                LiveRTCEngine.shared().mediaPlayer?.setMediaItem(afd)
                LiveRTCEngine.shared().mediaPlayer?.play()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun onBottomItemSelected(it: MenuItem): Boolean {
        val pos = bottomMenuTitles.indexOf(it.title)
        setNavigation(pos)
        return true
    }

    private fun setNavigation(pos: Int) {
        binding.vpContainer.setCurrentItem(pos, false)
        setTitle(bottomMenuTitles.elementAt(pos))
        currentNavigationPos = pos
        if (pos == 3) {
            binding.tbTop.toolbar.visibility = View.GONE
        } else {
            binding.tbTop.toolbar.visibility = View.VISIBLE
            resetToolbar()
        }
    }

    override fun getToolbar(): Toolbar {
        return binding.tbTop.toolbar
    }

    override fun menuMoreVisibility(id: Int): Int {
        if (id == R.id.tb_menu2) {
            return if (currentNavigationPos != 2) {
                View.GONE
            } else {
                View.VISIBLE
            }
        } else if (id == R.id.tb_menu1) {
            return View.VISIBLE
        }
        return View.VISIBLE
    }

    override fun onToolBarMenuClick(view: View) {
        if (view.id == R.id.tb_menu2) {
            if (currentNavigationPos == 2) {
                createGroup()
            }
        } else if (view.id == R.id.tb_menu1) {
            SearchActivity.startSearchActivity(this, currentNavigationPos)
        }
    }

    private fun createGroup() {
        GroupActivity.startCreateGroupActivity(this)
    }

}