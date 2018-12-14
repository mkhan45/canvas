package com.example.a2020mkhan.canvaslab2;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.util.Base64;

import java.io.ByteArrayOutputStream;

public class saveRunnable implements Runnable {
    Bitmap bitmap;
    SharedPreferences sp;

    @Override
    public void run(){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] b = baos.toByteArray();
        String encoded = Base64.encodeToString(b, Base64.DEFAULT);
        sp.edit().putString("bmp", encoded).commit();
    }

    public void setup(Bitmap bmp, SharedPreferences sharedPrefs){
       bitmap = bmp;
       sp = sharedPrefs;
    }
}
