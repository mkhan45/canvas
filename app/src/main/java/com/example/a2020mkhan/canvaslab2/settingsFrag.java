package com.example.a2020mkhan.canvaslab2;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;

public class settingsFrag extends Fragment{

    SeekBar red, green, blue;

    public settingsFrag(){}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.settings, container, false);

        red = (SeekBar) rootView.findViewById(R.id.redSlider);
        green = (SeekBar) rootView.findViewById(R.id.greenSlider);
        blue = (SeekBar) rootView.findViewById(R.id.blueSlider);

        red.setMax(255);
        green.setMax(255);
        blue.setMax(255);

        SharedPreferences sp = getActivity().getSharedPreferences("canvasPreferences", Context.MODE_PRIVATE);
        if(sp.contains("color")){
            String colorString = sp.getString("color", "#FFFFFF").substring(1);
            Log.i("Tag", colorString);

            red.setProgress(Integer.parseInt(colorString.substring(0,2), 16));
            green.setProgress(Integer.parseInt(colorString.substring(2,4), 16));
            blue.setProgress(Integer.parseInt(colorString.substring(4,6), 16));
        }


        Button save = (Button) rootView.findViewById(R.id.save);
        save.setOnClickListener(saveSettings);
        return rootView;
    }

    public View.OnClickListener saveSettings = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            SharedPreferences sp = getActivity().getSharedPreferences("canvasPreferences", Context.MODE_PRIVATE);
            String colorString = "";
            String redVal, greenVal, blueVal;

            if(red.getProgress() != 0)
                redVal = Integer.toHexString(red.getProgress());
            else
                redVal = "00";
            if(green.getProgress() != 0)
                greenVal = Integer.toHexString(green.getProgress());
            else greenVal = "00";
            if(blue.getProgress() != 0)
                blueVal = Integer.toHexString(blue.getProgress());
            else blueVal = "00";

            sp.edit().putString("color", "" + redVal + greenVal + blueVal).apply();
            Log.i("Color", redVal + " " + greenVal + " " + " " + blueVal);
        }
    };


}
