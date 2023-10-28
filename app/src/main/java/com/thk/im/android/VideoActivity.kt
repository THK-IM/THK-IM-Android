package com.thk.im.android

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.util.UnstableApi
import com.danikula.videocache.CacheListener
import com.thk.im.android.base.LLog
import com.thk.im.android.databinding.ActivityVideoBinding
import java.io.File

@UnstableApi
class VideoActivity : AppCompatActivity(), CacheListener {
    private lateinit var binding: ActivityVideoBinding
    private val videoUrl =
        "https://smilemiao.oss-cn-beijing.aliyuncs.com/im/session_1670948901844160512/50778/1689652409151_A846F817-773E-4E20-A3E2-8BB6CF72E23A_EeL7oR0z.mov"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVideoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initVideoView()
        com.thk.im.preview.VideoCache.getProxy().registerCacheListener(this, videoUrl)
    }

    private fun initVideoView() {
        binding.pvVideo.initPlay(videoUrl)
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.pvVideo.releasePlay()
        com.thk.im.preview.VideoCache.getProxy().unregisterCacheListener(
            this, videoUrl
        )
    }

    override fun onCacheAvailable(cacheFile: File?, url: String?, percentsAvailable: Int) {
        LLog.v("onCacheAvailable $cacheFile, $percentsAvailable, $url")
    }
}