package com.example.a2020mkhan.canvaslab2;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

public class settingsFrag extends Fragment{

    EditText color;

    public settingsFrag(){}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.settings, container, false);
        color = (EditText) rootView.findViewById(R.id.colorChanger);
        SharedPreferences sp = getActivity().getSharedPreferences("canvasPreferences", Context.MODE_PRIVATE);

        if(sp.contains("color")){
            color.setText(sp.getString("color", null));
        }



        Button save = (Button) rootView.findViewById(R.id.save);
        save.setOnClickListener(saveSettings);
        return rootView;
    }

    public View.OnClickListener saveSettings = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            SharedPreferences sp = getActivity().getSharedPreferences("canvasPreferences", Context.MODE_PRIVATE);
            sp.edit().putString("color", color.getText().toString());
        }
    };


}
