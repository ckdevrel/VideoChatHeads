package com.takeoffandroid.videochatheads.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.takeoffandroid.videochatheads.R;
import com.takeoffandroid.videochatheads.services.VideoChatHeadService;

public class MainActivity extends AppCompatActivity {

    // UI
    private Toolbar toolbar;
    private FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = (Toolbar) findViewById(R.id.toolbar_activity_main);

        setUiSettings();
    }

    @Override
    protected void onResume() {
        super.onResume();

        initListeners();

    }

    private void initListeners() {

        if(Build.VERSION.SDK_INT >= 23) {
            if (!Settings.canDrawOverlays(MainActivity.this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, 1234);
            }else{
                Intent i = VideoChatHeadService.getIntent(MainActivity.this);
                startService(i);
                MainActivity.this.finish();
            }
        }
        else
        {
            Intent i = VideoChatHeadService.getIntent(MainActivity.this);
            startService(i);
            MainActivity.this.finish();
        }

    }

    @Override
    public void onActivityReenter(int resultCode, Intent data) {

        if(resultCode == 1234){

            Intent i = VideoChatHeadService.getIntent(MainActivity.this);
            startService(i);
            MainActivity.this.finish();
        }
        super.onActivityReenter(resultCode, data);
    }

    private void setUiSettings() {
        setSupportActionBar(toolbar);
    }
}
