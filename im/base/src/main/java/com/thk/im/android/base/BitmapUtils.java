package com.thk.im.android.base;

import android.graphics.Bitmap;

public class BitmapUtils {
    //截取长图
    public static Bitmap scaledBitmapByWidth(Bitmap sourceBitmap, int targetWidth, int targetHeight) {
        int bitmapWidth = sourceBitmap.getWidth();
        int bitmapHeight = sourceBitmap.getHeight();
        if (bitmapHeight == 0) {
            return null;
        }

        float widthRatio = targetWidth * 1.0f / bitmapWidth;
        Bitmap scaleBitmap = null;
        try {
            scaleBitmap = Bitmap.createScaledBitmap(sourceBitmap, targetWidth, (int) (bitmapHeight * widthRatio), true); //先根据宽度比，等比缩放，然后截取需要的部分

        } catch (Exception e) {
            return null;
        }
        int sourceWidth = scaleBitmap.getWidth();
        int sourceHeight = scaleBitmap.getHeight();
        int needHeight = Math.min(targetHeight, sourceHeight);//如果需要的高度比图片原图的高度大，则用原图高度，否则使用期望的高度
        Bitmap resultBitmap = null;
        try {
            resultBitmap = Bitmap.createBitmap(scaleBitmap, 0, 0, sourceWidth, needHeight);
            scaleBitmap.recycle();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resultBitmap;
    }
}
