package com.paypal.scancode.zxing.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

public class BitmapUtil {

    public static int calculateInSampleSize(BitmapFactory.Options options,
                                            int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        while(reqHeight*reqWidth*4> AppliationUtil.FREE_MEMORY*1048576/4*3){
            reqHeight-=50;
            reqWidth-=50;
        }
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        if(inSampleSize==0) return 1;
        Log.e("hongliang","inSampleSize=" + inSampleSize);
        return inSampleSize;
    }
    public static Bitmap decodeBitmapFromPath(String photo_path, int reqWidth, int reqHeight) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        Bitmap scanBitmap = BitmapFactory.decodeFile(photo_path, options);
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(photo_path, options);
    }
}