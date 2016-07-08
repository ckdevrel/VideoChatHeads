package com.takeoffandroid.videochatheads.services;

import android.annotation.TargetApi;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.VideoView;

import com.takeoffandroid.videochatheads.R;
import com.takeoffandroid.videochatheads.views.VideoSurfaceView;

import java.io.IOException;


public class VideoChatHeadService extends Service implements SurfaceHolder.Callback{

    // constants
    public static final String BASIC_TAG = VideoChatHeadService.class.getName();

    // variables
    private WindowManager mWindowManager;
    private Vibrator mVibrator;
    private WindowManager.LayoutParams mPaperParams;
    private WindowManager.LayoutParams mRecycleBinParams;
    private int windowHeight;
    private int windowWidth;

    // UI
//    private VideoSurfaceView mVideoSurfaceView;
    private ImageView ivRecycleBin;

//    MediaPlayer player;

    private VideoSurfaceView[] mVideoSurfaceView = new VideoSurfaceView[1] ;

//    final String dataSources = "http://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4";
    private LayoutInflater li;
    private View myview;

    // get intent methods
    public static Intent getIntent(Context context) {


        Intent intent = new Intent(context, VideoChatHeadService.class);
        return intent;
    }

    // methods
    @Override
    public void onCreate() {
        super.onCreate();


    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        showHud();

        return START_STICKY;
    }

    private void showHud() {
        mWindowManager = (WindowManager) getSystemService(Service.WINDOW_SERVICE);
        mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        if (mVideoSurfaceView[0] != null) {
            mWindowManager.removeView(mVideoSurfaceView[0]);
            mVideoSurfaceView[0] = null;
        }
        li = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);

        mPaperParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
       /*| WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE*/,

        PixelFormat.TRANSLUCENT);

        DisplayMetrics displaymetrics = new DisplayMetrics();
        mWindowManager.getDefaultDisplay().getMetrics(displaymetrics);
        windowHeight = displaymetrics.heightPixels;
        windowWidth = displaymetrics.widthPixels;

        mPaperParams.gravity = Gravity.TOP | Gravity.RIGHT;

//        mVideoSurfaceView.setImageResource(R.drawable.ic_crumpled_paper);
//        mVideoSurfaceView[0].setLayoutParams(new LinearLayout.LayoutParams(50,50));
        myview = li.inflate(R.layout.view_video_chat_heads, null);

//        String uriPath = "android.resource://com.dision.android.hudrecyclebin/"+R.raw.k;
//        Uri uri = Uri.parse(uriPath);
//        mVideoSurfaceView.setVideoURI(uri);
//        mVideoSurfaceView.requestFocus();
//        mVideoSurfaceView.start();

        final int radius = getResources()
                .getDimensionPixelOffset(R.dimen.corner_radius_video);

        final String[] dataSources = new String[] {
                "http://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4",
                "http://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4",
                "http://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4"
        };


//        mVideoSurfaceView[0] = (VideoSurfaceView) findViewById(R.id.video_surface_view1);
        mVideoSurfaceView[0] = (VideoSurfaceView) myview.findViewById(R.id.video_surface_view);
//        mVideoSurfaceView[2] = (VideoSurfaceView) findViewById(R.id.video_surface_view3);

//        mVideoSurfaceView[0].setCornerRadius(radius);
        mVideoSurfaceView[0].setCornerRadius(radius);
//        mVideoSurfaceView[2].setCornerRadius(radius);

        for (int i = 0; i < mVideoSurfaceView.length; i++) {
            final MediaPlayer mediaPlayer = new MediaPlayer();
            final VideoSurfaceView surfaceView = mVideoSurfaceView[i];
            final String dataSource = dataSources[i];
            try {
                mediaPlayer.setDataSource(dataSource);
                // the video view will take care of calling prepare and attaching the surface once
                // it becomes available
                mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        mediaPlayer.start();
                        surfaceView.setVideoAspectRatio((float) mediaPlayer.getVideoWidth() /
                                (float) mediaPlayer.getVideoHeight());
                    }
                });
                surfaceView.setMediaPlayer(mediaPlayer);
            } catch (IOException e) {
                e.printStackTrace();
                mediaPlayer.release();
            }
        }

        mPaperParams.x = 0;
        mPaperParams.y = 50;

        mWindowManager.addView(myview, mPaperParams);
        addCrumpledPaperOnTouchListener();
    }

    private void addCrumpledPaperOnTouchListener() {
        mVideoSurfaceView[0].setOnTouchListener(new View.OnTouchListener() {
            private int initialX;
            private int initialY;
            private float initialTouchX;
            private float initialTouchY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = mPaperParams.x;
                        initialY = mPaperParams.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        // add recycle bin when moving crumpled paper
                        addRecycleBinView();

                        return true;
                    case MotionEvent.ACTION_UP:

                        int centerOfScreenByX = windowWidth / 2;

                        // remove crumpled paper when the it is in the recycle bin's area
                        if ((mPaperParams.y > windowHeight - ivRecycleBin.getHeight() - mVideoSurfaceView[0].getHeight()) &&
                                ((mPaperParams.x > centerOfScreenByX - ivRecycleBin.getWidth() - mVideoSurfaceView[0].getWidth() / 2) && (mPaperParams.x < centerOfScreenByX + ivRecycleBin.getWidth() / 2))) {
                            mVibrator.vibrate(100);
                            stopSelf();
                        }

                        // always remove recycle bin ImageView when paper is dropped
                        mWindowManager.removeView(ivRecycleBin);
                        ivRecycleBin = null;

                        return true;
                    case MotionEvent.ACTION_MOVE:
                        // move paper ImageView
                        mPaperParams.x = initialX + (int) (initialTouchX - event.getRawX());
                        mPaperParams.y = initialY + (int) (event.getRawY() - initialTouchY);
                        mWindowManager.updateViewLayout(mVideoSurfaceView[0], mPaperParams);
                        return true;
                }
                return false;
            }
        });
    }

    private void addRecycleBinView() {
        // add recycle bin ImageView centered on the bottom of the screen
        mRecycleBinParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        mRecycleBinParams.gravity = Gravity.BOTTOM | Gravity.CENTER;

        ivRecycleBin = new ImageView(this);
        ivRecycleBin.setImageResource(R.drawable.ic_recycle_bin);

        mRecycleBinParams.x = 0;
        mRecycleBinParams.y = 0;

        mWindowManager.addView(ivRecycleBin, mRecycleBinParams);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // remove views on destroy!
        if (mVideoSurfaceView[0] != null) {
            mWindowManager.removeView(mVideoSurfaceView[0]);
            mVideoSurfaceView[0] = null;
        }

        if (ivRecycleBin != null) {
            mWindowManager.removeView(ivRecycleBin);
            ivRecycleBin = null;
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
//        player.setDisplay(holder);
//        player.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }
}
