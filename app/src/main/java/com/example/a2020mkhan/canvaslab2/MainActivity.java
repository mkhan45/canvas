package com.example.a2020mkhan.canvaslab2;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.google.gson.Gson;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements settingsFrag.settingsInterface,
        NavigationView.OnNavigationItemSelectedListener {

    canvasFrag cf;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        cf = new canvasFrag();
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.setTransition(android.R.anim.linear_interpolator);
        ft.replace(R.id.frame, cf).addToBackStack(null).commit();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        final Intent saveIntent = new Intent();
        saveIntent.setClass(this, saveService.class);
        startService(saveIntent);

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                if (findViewById(R.id.canvas) != null) {
                    try {
                        Bitmap bmp = ((canvasView) findViewById(R.id.canvas)).getBitmap();
                        if(bmp == null)
                            Log.i("???", "???????????");
                        Uri bmpuri = getImageUri(getApplicationContext(), bmp);
                        saveIntent.putExtra("uri", bmpuri);
                    }catch (Exception e){}
                }
            }
        }, 0, 10000);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.settings) {

            Gson gson = new Gson();
            SharedPreferences sp = getSharedPreferences("canvasPreferences", Context.MODE_PRIVATE);
            Bitmap bmp = ((canvasView) findViewById(R.id.canvas)).getBitmap();

            saveRunnable sr = new saveRunnable();
            sr.setup(bmp, sp);
            Thread saveThread = new Thread(sr);
            saveThread.start();

            settingsFrag sf = new settingsFrag();
            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            ft.setTransition(android.R.anim.linear_interpolator);
            ft.replace(R.id.frame, sf).addToBackStack(null).commit();
            Log.i("TAg", "settubgs");
        } else if (id == R.id.reset) {
            cf.canvasReset();
        } else if (id == R.id.background) {
            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(cameraIntent, 0);
        } else if (id == R.id.colorCycle) {
            cf.colorCycle();
        } else if (id == R.id.blur) {
            cf.blur();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void onAttachFragment(Fragment fragment) {
        if (fragment instanceof settingsFrag) {
            settingsFrag sf = (settingsFrag) fragment;
            sf.setListener(this);
        }
    }


    public void resetImage() {
        cf.canvasReset();
    }

    public void setColor(String c) {
        int color = Color.parseColor("#" + c);
        cf.setColor(color);
    }


    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 0) {
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            cf.setBackground(photo);
        }
    }

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }


}
