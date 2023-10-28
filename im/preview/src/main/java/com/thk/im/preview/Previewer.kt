package com.thk.im.preview

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.graphics.Rect
import android.os.Build
import android.view.View
import com.thk.im.android.ui.manager.MediaItem
import com.thk.im.android.ui.protocol.IMPreviewer

class Previewer(app: Application, token: String) : IMPreviewer {

    init {
        VideoCache.init(app, token)
    }
    override fun previewMessage(
        activity: Activity,
        items: ArrayList<MediaItem>,
        view: View,
        position: Int
    ) {
        val intent = Intent(activity, MediaPreviewActivity::class.java)
        val locations = IntArray(2)
        view.getLocationOnScreen(locations)
        intent.putParcelableArrayListExtra("media_items", items)
        val rect = Rect(
            locations[0],
            locations[1],
            locations[0] + view.measuredWidth,
            locations[1] + view.measuredHeight,
        )
        intent.putExtra("origin_rect", rect)
        intent.putExtra("position", position)
        activity.startActivity(intent)
        if (Build.VERSION.SDK_INT >= 34) {
            activity.overrideActivityTransition(Activity.OVERRIDE_TRANSITION_OPEN, 0, 0)
        } else {
            activity.overridePendingTransition(0, 0)
        }
    }
}