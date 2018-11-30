package com.example.a2020mkhan.canvaslab2;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.gson.Gson;

public class canvasFrag extends Fragment{
    public canvasFrag(){}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.canvas_layout, container, false);
        SharedPreferences sp = getActivity().getSharedPreferences("canvasPreferences", Context.MODE_PRIVATE);
        canvasView canvas = (canvasView) rootView.findViewById(R.id.canvas);

        if (sp.contains("bmp")) {

            Gson gson = new Gson();
            String encoded = sp.getString("bmp", null);
            Log.i("tag", encoded);
            byte[] bmpBytes = Base64.decode(encoded.getBytes(), Base64.DEFAULT);
            Bitmap bmp = BitmapFactory.decodeByteArray(bmpBytes, 0, bmpBytes.length);
            canvas.setBitmap(bmp);
        }

        if(sp.contains("color")){
            try {
                canvas.setPaintColor(Color.parseColor(sp.getString("color", "#000000")));
            }catch (Exception e){
                canvas.setPaintColor(Color.BLACK);
            }
        }

        return rootView;
    }
}
