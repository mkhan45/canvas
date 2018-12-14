package com.example.a2020mkhan.canvaslab2;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.util.Timer;
import java.util.TimerTask;

public class saveService extends Service{
    private Looper mServiceLooper;
    private ServiceHandler mServiceHandler;
    Bitmap bitmap;


    private final class ServiceHandler extends Handler{
        public ServiceHandler(Looper looper){
            super(looper);
        }
    }

    public void onCreate(){
        HandlerThread thread = new HandlerThread("ServiceStartArguments", Process.THREAD_PRIORITY_BACKGROUND);

        thread.start();

        mServiceLooper = thread.getLooper();
        mServiceHandler = new ServiceHandler(mServiceLooper);
    }

    public void handleMessage(Message msg){
        try {
            Thread.sleep(5000);
        }catch (InterruptedException e){
            Thread.currentThread().interrupt();
        }

        stopSelf(msg.arg1);
    }

    public int onStartCommand(final Intent intent, int flags, int startId){
        Toast.makeText(this, "autosaving", Toast.LENGTH_SHORT).show();
        Log.i("Service", "Saving");

        Message msg = mServiceHandler.obtainMessage();
        msg.arg1 = startId;
        mServiceHandler.sendMessage(msg);

        Handler handler = new Handler();

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                onHandleIntent(intent);
            }
        }, 0, 10000);

        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    protected void onHandleIntent(Intent intentIn){
        try {
            SharedPreferences sp = getSharedPreferences("canvasPreferences", Context.MODE_PRIVATE);
            Log.i("Service", "Saving");
            Intent intentOut = new Intent();
            intentIn.setAction("save");


            Uri uri = (Uri) intentIn.getParcelableExtra("uri");
            bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
            byte[] b = baos.toByteArray();
            String encoded = Base64.encodeToString(b, Base64.DEFAULT);
            sp.edit().putString("bmp", encoded).commit();
        }catch (Exception e){}
    }

    public void onDestroy(){
        Toast.makeText(this, "service dead", Toast.LENGTH_SHORT).show();
    }
}
