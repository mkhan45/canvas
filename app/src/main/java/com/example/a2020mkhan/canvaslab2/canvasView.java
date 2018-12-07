package com.example.a2020mkhan.canvaslab2;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
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

    public canvasView(Context context, AttributeSet attributes){
        super(context, attributes);
        setFocusable(true);
        setFocusableInTouchMode(true);
        path = new Path();
        bitmapPaint = new Paint(Paint.DITHER_FLAG);
        SharedPreferences sp = getContext().getSharedPreferences("canvasPreferences", 0);
        if(paint == null)
            setPaint();
        Log.i("Color on instantiate", sp.getString("color", "null"));
    }


    protected void onSizeChanged(int w, int h, int oldw, int oldh){
        super.onSizeChanged(w, h, oldw, oldh);
        width = w;
        height = h;
        if(bitmap == null)
            bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bitmap);
    }

    public boolean onTouchEvent(MotionEvent event){
        float pointX = event.getX();
        float pointY = event.getY();

        switch (event.getAction()){
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

    public void setBitmap(Bitmap bmp){
        bitmap = bmp.copy(Bitmap.Config.ARGB_8888, true);
    }

    public void changeBitmap(Bitmap bmp){
        int w = getWidth();
        int h = getHeight();
        Rect src = new Rect(0, 0, bmp.getWidth()-1, bmp.getHeight()-1);
        Rect dest = new Rect(0, 0, width-1, height-1);
        canvas.drawBitmap(bmp, src, dest, bitmapPaint);
    }


    protected void onDraw(Canvas canvas){
        canvas.drawBitmap(bitmap, 0, 0, bitmapPaint);
        Log.i("paint color", paint.getColor() + "");
        canvas.drawPath(path, paint);
    }

    private void setPaint(){
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

    void setPaintColor(int c){
        Log.i("changing color" ,c + "");
        paint.setColor(c);
        Log.i("color changed", paint.getColor() + "");
    }

    public void reset(){
        canvas.drawColor(Color.WHITE);
    }

    final Runnable r = new Runnable() {
        android.os.Handler h = new android.os.Handler();
        int c = Color.BLACK;

        @Override
        public void run() {
            canvas.drawColor(c);
            Log.i("color changing", c + "");
            c += 1000;
            h.postDelayed(this, 1);
        }
    };

    public void startColorCycle(){
        r.run();
    }


}
