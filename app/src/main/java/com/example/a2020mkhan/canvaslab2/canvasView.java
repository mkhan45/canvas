package com.example.a2020mkhan.canvaslab2;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.os.Process;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.logging.Handler;

public class canvasView extends View {

    private Paint paint;
    private android.graphics.Path path;
    private Canvas canvas;
    private Bitmap bitmap;
    private Paint bitmapPaint;
    private int width;
    private int height;
    private boolean blur;

    public canvasView(Context context, AttributeSet attributes) {
        super(context, attributes);
        setFocusable(true);
        setFocusableInTouchMode(true);
        path = new Path();
        bitmapPaint = new Paint(Paint.DITHER_FLAG);
        SharedPreferences sp = getContext().getSharedPreferences("canvasPreferences", 0);
        if (paint == null)
            setPaint();


        //saveService ss = new saveService();
        //ss.startService(new Intent());
    }


    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        width = w;
        height = h;
        if (bitmap == null)
            bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bitmap);
    }

    public boolean onTouchEvent(MotionEvent event) {
        float pointX = event.getX();
        float pointY = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                path.reset();
                path.moveTo(pointX, pointY);
                break;
            case MotionEvent.ACTION_MOVE:
                path.lineTo(pointX, pointY);
                break;
            case MotionEvent.ACTION_UP:
                path.lineTo(pointX, pointY);
                canvas.drawPath(path, paint);
                path.reset();
            default:
                return false;
        }
        postInvalidate();
        return true;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bmp) {
        bitmap = bmp.copy(Bitmap.Config.ARGB_8888, true);
    }

    public void changeBitmap(Bitmap bmp) {
        int w = getWidth();
        int h = getHeight();
        Rect src = new Rect(0, 0, bmp.getWidth() - 1, bmp.getHeight() - 1);
        Rect dest = new Rect(0, 0, width - 1, height - 1);
        canvas.drawBitmap(bmp, src, dest, bitmapPaint);
    }


    protected void onDraw(Canvas canvas) {
        canvas.drawBitmap(bitmap, 0, 0, bitmapPaint);
        canvas.drawPath(path, paint);
    }

    private void setPaint() {
        Log.i("setting paint", "");


        SharedPreferences sp = getContext().getSharedPreferences("canvasPreferences", 0);
        paint = new Paint();
        setPaintColor(Color.parseColor("#" + sp.getString("color", "FFFFFF")));
        paint.setAntiAlias(true);
        paint.setStrokeWidth(5);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStyle(Paint.Style.STROKE);
    }

    void setPaintColor(int c) {
        paint.setColor(c);
    }

    public void reset() {
        canvas.drawColor(Color.WHITE);
    }

    final Runnable r = new Runnable() {
        android.os.Handler h = new android.os.Handler();
        int c = Color.BLACK;

        @Override
        public void run() {
            canvas.drawColor(c);
            c += 1000;
            h.postDelayed(this, 1);
        }
    };

    public void startColorCycle() {
        Thread runColor = new Thread(r);
        runColor.start();
    }

    public void blur() {
        Thread blurThread = new Thread(blurRunnable);
        blurThread.start();
        draw(canvas);
    }


    final Runnable blurRunnable = new Runnable() {
        @Override
        public void run() {
            android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();

            Bitmap in = Bitmap.createScaledBitmap(bitmap, width, height, false);
            Bitmap out = Bitmap.createBitmap(bitmap);

            RenderScript rs = RenderScript.create(getContext());
            ScriptIntrinsicBlur blur = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));
            Allocation tmpIn = Allocation.createFromBitmap(rs, in);
            Allocation tmpOut = Allocation.createFromBitmap(rs, out);
            blur.setRadius(25);
            blur.setInput(tmpIn);
            blur.forEach(tmpOut);
            tmpOut.copyTo(out);

            changeBitmap(out);
        }
    };



}
