package com.example.a2020mkhan.canvaslab2;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class canvasView extends View {

    private Paint paint;
    private int paintColor = Color.BLACK;
    private android.graphics.Path path = new android.graphics.Path();
    public canvasView(Context context, AttributeSet attributes){
        super(context, attributes);
        setPaint();
    }

    public boolean onTouchEvent(MotionEvent event){
        float pointX = event.getX();
        float pointY = event.getY();

        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                path.moveTo(pointX, pointY);
                break;
            case MotionEvent.ACTION_MOVE:
                path.lineTo(pointX, pointY);
                break;
             default:
                    return false;
        }

        postInvalidate();
        return true;
    }

    protected void onDraw(Canvas canvas){
        canvas.drawPath(path, paint);
    }

    private void setPaint(){
        Log.i("setting paint", "");
        paint = new Paint();
        paint.setColor(paintColor);
        paint.setAntiAlias(true);
        paint.setStrokeWidth(5);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStyle(Paint.Style.STROKE);
    }
}
