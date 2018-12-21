package com.example.a2020mkhan.canvaslab2;
import android.graphics.Bitmap;

enum BitmapTransferEnum {
    INSTANCE;

    private Bitmap mBitmap;
    public final static String KEY = "BitmapTransferEnum";


    public static void setData(final Bitmap b) {
        INSTANCE.mBitmap = b;
    }

    public static Bitmap getData() {
        Bitmap retBitmap = INSTANCE.mBitmap;
        Bitmap outBitmap = retBitmap.copy(retBitmap.getConfig(), true);
//        INSTANCE.mBitmap.recycle();
//        INSTANCE.mBitmap = null;
        return outBitmap;
    }
}
