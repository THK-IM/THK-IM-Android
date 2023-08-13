package com.thk.im.android.ui.emoji.span;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.text.style.ImageSpan;

public class CenterImageSpan extends ImageSpan {

    public CenterImageSpan(Drawable drawable) {
        super(drawable);
    }

    @Override
    public void draw(Canvas canvas, CharSequence text, int start, int end,
                     float x, int top, int y, int bottom, Paint paint) {
        Drawable b = getDrawable();
        canvas.save();
        int transY;
        //要显示的文本高度-图片高度除2等居中位置+top(换行情况)
        transY = ((bottom - top) - b.getBounds().bottom) / 2 + top;
        //偏移画布后开始绘制
        canvas.translate(x, transY);
        b.draw(canvas);
        canvas.restore();
    }
}
