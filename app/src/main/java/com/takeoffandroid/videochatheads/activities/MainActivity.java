package com.takeoffandroid.videochatheads.activities;

import android.content.Intent;
import android.os.Bundle;
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
        fab = (FloatingActionButton) findViewById(R.id.fab_activity_main);

        initListeners();
        setUiSettings();
    }

    private void initListeners() {
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = VideoChatHeadService.getIntent(MainActivity.this);
                startService(i);
                MainActivity.this.finish();
            }
        });
    }

    private void setUiSettings() {
        setSupportActionBar(toolbar);
    }
}
