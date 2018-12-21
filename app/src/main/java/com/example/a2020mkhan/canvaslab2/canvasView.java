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
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.os.Process;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Pair;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.Arrays;
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
        canvas.drawBitmap(bitmap, 0, 0, bitmapPaint);
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

    final Runnable blur2 = new Runnable() {
        @Override
        public void run() {
            fftBlur(getBitmap());
        }
    };

    public void blurImage2(Bitmap bmp, int rad) {
        int width = bmp.getWidth();
        int height = bmp.getHeight();
        Bitmap bmp2 = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        for (int y = 0; y < height; y++) {
            int minY = (y - rad < 0) ? 0 : (y - rad);
            int maxY = (y + rad > height - 1) ? height - 1 : (y + rad);

            for (int x = 0; x < width; x++) {
                int minX = (x - rad < 0) ? 0 : (x - rad);
                int maxX = (x + rad > height - 1) ? (x + rad) : (width - 1);
                int r = 0;
                int g = 0;
                int b = 0;

                for (int yi = minY; yi < maxY; yi++) {
                    for (int xi = minX; xi < maxX; xi++) {
                        int pixel = bmp.getPixel(xi, yi);
                        r += Color.red(pixel);
                        g += Color.green(pixel);
                        b += Color.red(pixel);
                    }
                }

                r = (int) Math.floor((r / Math.pow(2, rad)));
                g = (int) Math.floor((g / Math.pow(2, rad)));
                b = (int) Math.floor((b / Math.pow(2, rad)));

                if (Color.rgb(r, g, b) != bmp.getPixel(x, y))
                    bmp2.setPixel(x, y, Color.rgb(r, g, b));
                Log.i("blur", "blurring" + x + ", " + y);
            }
        }

        changeBitmap(bmp2);
    }

    public void blur3(Bitmap bmp, int fac) {
        Bitmap bmp2 = Bitmap.createScaledBitmap(bmp, bmp.getWidth() / fac, bmp.getHeight() / fac, false);
        changeBitmap(Bitmap.createScaledBitmap(bmp2, bmp.getWidth(), bmp.getHeight(), false));
    }

    public void fftBlur(Bitmap bmp) {
        Bitmap bmp2 = transformBitmap(bmp);
        changeBitmap(bmp2);
    }

    public Pair<Bitmap, int[]> getInside(Path path) {
        RectF rectF = new RectF();
        path.computeBounds(rectF, true);
        Bitmap bmpOut = Bitmap.createBitmap((int) rectF.width(), (int) rectF.height(), Bitmap.Config.ARGB_8888);
        int[] coords = {(int) rectF.left, (int) rectF.top};

        int x = 0, y = 0;
        for (int r = coords[1]; r < rectF.bottom - 1; r++) {
            for (int c = coords[0]; r < rectF.right - 1; c++) {
                bmpOut.setPixel(x, y, bitmap.getPixel(r, c));
            }
        }


        return new Pair<>(bmpOut, coords);
    }

    private Bitmap transformBitmap(Bitmap bmp) {

        String TAG = "tag";

        // --------------------------------------------------------
        //      INITIALIZATION

        // INITIALIZE HELPER VARIABLES USED AT VARIOUS POINTS IN THIS METHOD
        int i = 0;
        int j = 0;
        int pixelNo;


        // DEFINE A NEW DIMENSION (TO BE USED AS HEIGHT AND WIDTH) OF THE BITMAP
        //  THEN, RESIZE THE BITMAP
        int new_dimension = 40;
        bmp.reconfigure(new_dimension, new_dimension, bmp.getConfig());

        // EXTRACT PIXEL VALUES FROM THE OLD BITMAP.
        //   -> -> THESE PIXELS ARE NOT USED IN THIS EXAMPLE. JUST FYI.
        int[] argb_in = new int[new_dimension * new_dimension];
        bmp.getPixels(argb_in, 0, new_dimension, 0, 0, new_dimension, new_dimension);

        Log.i(TAG, "Bitmap Width: " + String.valueOf(bmp.getWidth()));
        Log.i(TAG, "Bitmap Height:" + String.valueOf(bmp.getHeight()));


        // DEFINE THE NUMBER OF POINTS IN THE FFT.
        //  FIRST CREATE AN FFT OBJECT
        //  THEN CREATE COMPLEX OBJECTS (THAT WILL HOLD fft'd VALUES)
        //  THE FFT OPERATION USED IN THIS METHOD TRANSFORMS THE OBJECTS IN PLACE
        int N = 128;
        FFT fft = new FFT(N, N);
        Complex2D KERNEL = new Complex2D(N, N);
        Complex2D COLOR = new Complex2D(N, N);

        // DEFINE THE CONVOLUTION KERNEL
        //  KERNEL IS A 7x& GAUUSIAN
        double[][] kernel = {{0.000036, 0.000363, 0.001446, 0.002291, 0.001446, 0.000363, 0.000036},
                {0.000363, 0.003676, 0.014662, 0.023226, 0.014662, 0.003676, 0.000363},
                {0.001446, 0.014662, 0.058488, 0.092651, 0.058488, 0.014662, 0.001446},
                {0.002291, 0.023226, 0.092651, 0.146768, 0.092651, 0.023226, 0.002291},
                {0.001446, 0.014662, 0.058488, 0.092651, 0.058488, 0.014662, 0.001446},
                {0.000363, 0.003676, 0.014662, 0.023226, 0.014662, 0.003676, 0.000363},
                {0.000036, 0.000363, 0.001446, 0.002291, 0.001446, 0.000363, 0.000036}};

        // ASSIGN THE KERNEL VALUES TO AN OBJECT THAT CAN BE FFT'd
        //  THEN TAKE THE FFT OF THE KERNEL
        KERNEL.assign(kernel);
        fft.fft2(KERNEL.real, KERNEL.imag);

        // THE CONVOLVED IMAGE WILL BE INSET kernel.length/2 PIXELS FROM THE EDGE
        int x_inset = 3;
        int y_inset = 3;


        // DEFINE A 40x40 CHECKER PATTERN
        //   (THIS IS THE TEST IMAGE)
        //  THEN, CREATE AN ARRAY TO HOLD THE PIXElS THAT WILL
        //   PUSHED BE THE OUTPUT BITMAP
        double[] color = {255., 255., 255., 255., 255., 255., 255., 255., 255., 255., 0., 0., 0., 0., 0., 0., 0., 0., 0., 0., 255., 255., 255., 255., 255., 255., 255., 255., 255., 255., 0., 0., 0., 0., 0., 0., 0., 0., 0., 0., 255., 255., 255., 255., 255., 255., 255., 255., 255., 255., 0., 0., 0., 0., 0., 0., 0., 0., 0., 0., 255., 255., 255., 255., 255., 255., 255., 255., 255., 255., 0., 0., 0., 0., 0., 0., 0., 0., 0., 0., 255., 255., 255., 255., 255., 255., 255., 255., 255., 255., 0., 0., 0., 0., 0., 0., 0., 0., 0., 0., 255., 255., 255., 255., 255., 255., 255., 255., 255., 255., 0., 0., 0., 0., 0., 0., 0., 0., 0., 0., 255., 255., 255., 255., 255., 255., 255., 255., 255., 255., 0., 0., 0., 0., 0., 0., 0., 0., 0., 0., 255., 255., 255., 255., 255., 255., 255., 255., 255., 255., 0., 0., 0., 0., 0., 0., 0., 0., 0., 0., 255., 255., 255., 255., 255., 255., 255., 255., 255., 255., 0., 0., 0., 0., 0., 0., 0., 0., 0., 0., 255., 255., 255., 255., 255., 255., 255., 255., 255., 255., 0., 0., 0., 0., 0., 0., 0., 0., 0., 0., 255., 255., 255., 255., 255., 255., 255., 255., 255., 255., 0., 0., 0., 0., 0., 0., 0., 0., 0., 0., 255., 255., 255., 255., 255., 255., 255., 255., 255., 255., 0., 0., 0., 0., 0., 0., 0., 0., 0., 0., 255., 255., 255., 255., 255., 255., 255., 255., 255., 255., 0., 0., 0., 0., 0., 0., 0., 0., 0., 0., 255., 255., 255., 255., 255., 255., 255., 255., 255., 255., 0., 0., 0., 0., 0., 0., 0., 0., 0., 0., 255., 255., 255., 255., 255., 255., 255., 255., 255., 255., 0., 0., 0., 0., 0., 0., 0., 0., 0., 0., 255., 255., 255., 255., 255., 255., 255., 255., 255., 255., 0., 0., 0., 0., 0., 0., 0., 0., 0., 0., 255., 255., 255., 255., 255., 255., 255., 255., 255., 255., 0., 0., 0., 0., 0., 0., 0., 0., 0., 0., 255., 255., 255., 255., 255., 255., 255., 255., 255., 255., 0., 0., 0., 0., 0., 0., 0., 0., 0., 0., 255., 255., 255., 255., 255., 255., 255., 255., 255., 255., 0., 0., 0., 0., 0., 0., 0., 0., 0., 0., 255., 255., 255., 255., 255., 255., 255., 255., 255., 255., 0., 0., 0., 0., 0., 0., 0., 0., 0., 0., 0., 0., 0., 0., 0., 0., 0., 0., 0., 0., 255., 255., 255., 255., 255., 255., 255., 255., 255., 255., 0., 0., 0., 0., 0., 0., 0., 0., 0., 0., 255., 255., 255., 255., 255., 255., 255., 255., 255., 255., 0., 0., 0., 0., 0., 0., 0., 0., 0., 0., 255., 255., 255., 255., 255., 255., 255., 255., 255., 255., 0., 0., 0., 0., 0., 0., 0., 0., 0., 0., 255., 255., 255., 255., 255., 255., 255., 255., 255., 255., 0., 0., 0., 0., 0., 0., 0., 0., 0., 0., 255., 255., 255., 255., 255., 255., 255., 255., 255., 255., 0., 0., 0., 0., 0., 0., 0., 0., 0., 0., 255., 255., 255., 255., 255., 255., 255., 255., 255., 255., 0., 0., 0., 0., 0., 0., 0., 0., 0., 0., 255., 255., 255., 255., 255., 255., 255., 255., 255., 255., 0., 0., 0., 0., 0., 0., 0., 0., 0., 0., 255., 255., 255., 255., 255., 255., 255., 255., 255., 255., 0., 0., 0., 0., 0., 0., 0., 0., 0., 0., 255., 255., 255., 255., 255., 255., 255., 255., 255., 255., 0., 0., 0., 0., 0., 0., 0., 0., 0., 0., 255., 255., 255., 255., 255., 255., 255., 255., 255., 255., 0., 0., 0., 0., 0., 0., 0., 0., 0., 0., 255., 255., 255., 255., 255., 255., 255., 255., 255., 255., 0., 0., 0., 0., 0., 0., 0., 0., 0., 0., 255., 255., 255., 255., 255., 255., 255., 255., 255., 255., 0., 0., 0., 0., 0., 0., 0., 0., 0., 0., 255., 255., 255., 255., 255., 255., 255., 255., 255., 255., 0., 0., 0., 0., 0., 0., 0., 0., 0., 0., 255., 255., 255., 255., 255., 255., 255., 255., 255., 255., 0., 0., 0., 0., 0., 0., 0., 0., 0., 0., 255., 255., 255., 255., 255., 255., 255., 255., 255., 255., 0., 0., 0., 0., 0., 0., 0., 0., 0., 0., 255., 255., 255., 255., 255., 255., 255., 255., 255., 255., 0., 0., 0., 0., 0., 0., 0., 0., 0., 0., 255., 255., 255., 255., 255., 255., 255., 255., 255., 255., 0., 0., 0., 0., 0., 0., 0., 0., 0., 0., 255., 255., 255., 255., 255., 255., 255., 255., 255., 255., 0., 0., 0., 0., 0., 0., 0., 0., 0., 0., 255., 255., 255., 255., 255., 255., 255., 255., 255., 255., 0., 0., 0., 0., 0., 0., 0., 0., 0., 0., 255., 255., 255., 255., 255., 255., 255., 255., 255., 255., 255., 255., 255., 255., 255., 255., 255., 255., 255., 255., 0., 0., 0., 0., 0., 0., 0., 0., 0., 0., 255., 255., 255., 255., 255., 255., 255., 255., 255., 255., 0., 0., 0., 0., 0., 0., 0., 0., 0., 0., 255., 255., 255., 255., 255., 255., 255., 255., 255., 255., 0., 0., 0., 0., 0., 0., 0., 0., 0., 0., 255., 255., 255., 255., 255., 255., 255., 255., 255., 255., 0., 0., 0., 0., 0., 0., 0., 0., 0., 0., 255., 255., 255., 255., 255., 255., 255., 255., 255., 255., 0., 0., 0., 0., 0., 0., 0., 0., 0., 0., 255., 255., 255., 255., 255., 255., 255., 255., 255., 255., 0., 0., 0., 0., 0., 0., 0., 0., 0., 0., 255., 255., 255., 255., 255., 255., 255., 255., 255., 255., 0., 0., 0., 0., 0., 0., 0., 0., 0., 0., 255., 255., 255., 255., 255., 255., 255., 255., 255., 255., 0., 0., 0., 0., 0., 0., 0., 0., 0., 0., 255., 255., 255., 255., 255., 255., 255., 255., 255., 255., 0., 0., 0., 0., 0., 0., 0., 0., 0., 0., 255., 255., 255., 255., 255., 255., 255., 255., 255., 255., 0., 0., 0., 0., 0., 0., 0., 0., 0., 0., 255., 255., 255., 255., 255., 255., 255., 255., 255., 255., 0., 0., 0., 0., 0., 0., 0., 0., 0., 0., 255., 255., 255., 255., 255., 255., 255., 255., 255., 255., 0., 0., 0., 0., 0., 0., 0., 0., 0., 0., 255., 255., 255., 255., 255., 255., 255., 255., 255., 255., 0., 0., 0., 0., 0., 0., 0., 0., 0., 0., 255., 255., 255., 255., 255., 255., 255., 255., 255., 255., 0., 0., 0., 0., 0., 0., 0., 0., 0., 0., 255., 255., 255., 255., 255., 255., 255., 255., 255., 255., 0., 0., 0., 0., 0., 0., 0., 0., 0., 0., 255., 255., 255., 255., 255., 255., 255., 255., 255., 255., 0., 0., 0., 0., 0., 0., 0., 0., 0., 0., 255., 255., 255., 255., 255., 255., 255., 255., 255., 255., 0., 0., 0., 0., 0., 0., 0., 0., 0., 0., 255., 255., 255., 255., 255., 255., 255., 255., 255., 255., 0., 0., 0., 0., 0., 0., 0., 0., 0., 0., 255., 255., 255., 255., 255., 255., 255., 255., 255., 255., 0., 0., 0., 0., 0., 0., 0., 0., 0., 0., 255., 255., 255., 255., 255., 255., 255., 255., 255., 255., 0., 0., 0., 0., 0., 0., 0., 0., 0., 0., 0., 0., 0., 0., 0., 0., 0., 0., 0., 0., 255., 255., 255., 255., 255., 255., 255., 255., 255., 255., 0., 0., 0., 0., 0., 0., 0., 0., 0., 0., 255., 255., 255., 255., 255., 255., 255., 255., 255., 255., 0., 0., 0., 0., 0., 0., 0., 0., 0., 0., 255., 255., 255., 255., 255., 255., 255., 255., 255., 255., 0., 0., 0., 0., 0., 0., 0., 0., 0., 0., 255., 255., 255., 255., 255., 255., 255., 255., 255., 255., 0., 0., 0., 0., 0., 0., 0., 0., 0., 0., 255., 255., 255., 255., 255., 255., 255., 255., 255., 255., 0., 0., 0., 0., 0., 0., 0., 0., 0., 0., 255., 255., 255., 255., 255., 255., 255., 255., 255., 255., 0., 0., 0., 0., 0., 0., 0., 0., 0., 0., 255., 255., 255., 255., 255., 255., 255., 255., 255., 255., 0., 0., 0., 0., 0., 0., 0., 0., 0., 0., 255., 255., 255., 255., 255., 255., 255., 255., 255., 255., 0., 0., 0., 0., 0., 0., 0., 0., 0., 0., 255., 255., 255., 255., 255., 255., 255., 255., 255., 255., 0., 0., 0., 0., 0., 0., 0., 0., 0., 0., 255., 255., 255., 255., 255., 255., 255., 255., 255., 255., 0., 0., 0., 0., 0., 0., 0., 0., 0., 0., 255., 255., 255., 255., 255., 255., 255., 255., 255., 255., 0., 0., 0., 0., 0., 0., 0., 0., 0., 0., 255., 255., 255., 255., 255., 255., 255., 255., 255., 255., 0., 0., 0., 0., 0., 0., 0., 0., 0., 0., 255., 255., 255., 255., 255., 255., 255., 255., 255., 255., 0., 0., 0., 0., 0., 0., 0., 0., 0., 0., 255., 255., 255., 255., 255., 255., 255., 255., 255., 255., 0., 0., 0., 0., 0., 0., 0., 0., 0., 0., 255., 255., 255., 255., 255., 255., 255., 255., 255., 255., 0., 0., 0., 0., 0., 0., 0., 0., 0., 0., 255., 255., 255., 255., 255., 255., 255., 255., 255., 255., 0., 0., 0., 0., 0., 0., 0., 0., 0., 0., 255., 255., 255., 255., 255., 255., 255., 255., 255., 255., 0., 0., 0., 0., 0., 0., 0., 0., 0., 0., 255., 255., 255., 255., 255., 255., 255., 255., 255., 255., 0., 0., 0., 0., 0., 0., 0., 0., 0., 0., 255., 255., 255., 255., 255., 255., 255., 255., 255., 255., 0., 0., 0., 0., 0., 0., 0., 0., 0., 0., 255., 255., 255., 255., 255., 255., 255., 255., 255., 255.};

        int[] argb_out = new int[new_dimension * new_dimension];

        // SET THE OUTPUT TRANSPARENCY TO ZERO
        Arrays.fill(argb_out, 255 << 24);

        // DEFINE A VALUE THAT WILL BE USED TO FILL ALL PIXELS THAT ARE NOT EXPLICITLY SET
        double all_others = 255.;


        // --------------------------------------------------------
        //      FAST CONVOLUTION


        //  -------------- RED --------------

        // FILL THE COLOR FFT OBJECT WITH THE PIXELS FROM OUR INPUT IMAGE
        COLOR.assignFrom(color, new_dimension, all_others);

        // TAKE THE 2D FFT OF THE RED LAYER OF THE IMAGE
        fft.fft2(COLOR.real, COLOR.imag);
        Log.i(TAG, "RED FFT");

        // MULTIPLY THE FFT OF THE RED LAYER BY THE FFT OF THE KERNEL
        COLOR.complexMultiply(KERNEL.real, KERNEL.imag);

        // INVERSE 2D FFT THE MULTIPLICATION RESULT
        fft.ifft2(COLOR.real, COLOR.imag);
        Log.i(TAG, "RED iFFT");

        // ASSIGN THE CONVOLVED VALUES TO THE RED CHANNEL OF THE OUTPUT BUFFER
        pixelNo = 0;
        for (i = x_inset; i < x_inset + new_dimension; i++) {
            for (j = y_inset; j < y_inset + new_dimension; j++) {

                // CAST AS AN INT AND SHIFT TO THE RED CHANNEL
                argb_out[pixelNo++] |= ((int) COLOR.real[i][j]) << 16;
            }
        }

        //  -------------- GREEN --------------

        COLOR.assignFrom(color, new_dimension, all_others);

        fft.fft2(COLOR.real, COLOR.imag);
        Log.i(TAG, "GREEN FFT");

        COLOR.complexMultiply(KERNEL.real, KERNEL.imag);
        fft.ifft2(COLOR.real, COLOR.imag);
        Log.i(TAG, "GREEN iFFT");
        pixelNo = 0;

        for (i = x_inset; i < x_inset + new_dimension; i++) {
            for (j = y_inset; j < y_inset + new_dimension; j++) {
                argb_out[pixelNo++] |= ((int) COLOR.real[i][j]) << 8;
            }
        }

        //  -------------- BLUE --------------

        COLOR.assignFrom(color, new_dimension, all_others);

        fft.fft2(COLOR.real, COLOR.imag);
        Log.i(TAG, "BLUE FFT");

        COLOR.complexMultiply(KERNEL.real, KERNEL.imag);
        fft.ifft2(COLOR.real, COLOR.imag);
        Log.i(TAG, "BLUE iFFT");

        pixelNo = 0;
        for (i = x_inset; i < x_inset + new_dimension; i++) {
            for (j = y_inset; j < y_inset + new_dimension; j++) {
                argb_out[pixelNo++] |= ((int) COLOR.real[i][j]);
            }
        }

        // ASSIGN THE NEW PIXELS TO THE BITMAP
        bmp.setPixels(argb_out, 0, new_dimension, 0, 0, new_dimension, new_dimension);
        return bmp;
    }
}



