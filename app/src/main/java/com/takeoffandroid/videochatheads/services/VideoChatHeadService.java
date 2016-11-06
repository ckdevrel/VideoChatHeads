package com.takeoffandroid.videochatheads.services;

import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.takeoffandroid.videochatheads.R;
import com.takeoffandroid.videochatheads.utils.Utils;
import com.takeoffandroid.videochatheads.views.VideoSurfaceView;

import java.io.IOException;


public class VideoChatHeadService extends Service implements SurfaceHolder.Callback {

    // constants
    public static final String BASIC_TAG = VideoChatHeadService.class.getName();

    // variables
    private WindowManager mWindowManager;
    private Vibrator mVibrator;
    private WindowManager.LayoutParams mPaperParams;
    private WindowManager.LayoutParams mRecycleBinParams;
    private int windowHeight;
    private int windowWidth;

    private View closeView, chatHeadsView;
    private LayoutInflater liClose, liChatHeads;

    // UI
//    private VideoSurfaceView mVideoSurfaceView;
    private ImageView ivRecycleBin, imgPlayPause;

//    MediaPlayer player;

    private VideoSurfaceView[] mVideoSurfaceView = new VideoSurfaceView[1];
    private MediaPlayer mediaPlayer;
    private boolean isMediaPrepared, isPlaying;

//    final String dataSources = "http://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4";
//    private View myview;

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
        showChatHeades();

        return START_STICKY;
    }

    private void showChatHeades() {
        mWindowManager = (WindowManager) getSystemService(Service.WINDOW_SERVICE);
        mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        liChatHeads = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);

        if (chatHeadsView != null) {
            mWindowManager.removeView(chatHeadsView);
            mVideoSurfaceView[0] = null;
        }

        mPaperParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
       /*| WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE*/,

                PixelFormat.TRANSLUCENT);


        DisplayMetrics displaymetrics = new DisplayMetrics();
        mWindowManager.getDefaultDisplay().getMetrics(displaymetrics);
        windowHeight = displaymetrics.heightPixels;
        windowWidth = displaymetrics.widthPixels;

        mPaperParams.gravity = Gravity.TOP | Gravity.RIGHT;
        mPaperParams.height = 200;
        mPaperParams.width = 200;

//        mVideoSurfaceView.setImageResource(R.drawable.ic_crumpled_paper);
//        mVideoSurfaceView[0].setLayoutParams(new LinearLayout.LayoutParams(50,50));
//        myview = li.inflate(R.layout.view_layout_chat_heads, null);

//        String uriPath = "android.resource://com.dision.android.hudrecyclebin/"+R.raw.k;
//        Uri uri = Uri.parse(uriPath);
//        mVideoSurfaceView.setVideoURI(uri);
//        mVideoSurfaceView.requestFocus();
//        mVideoSurfaceView.start();

        final int radius = getResources()
                .getDimensionPixelOffset(R.dimen.corner_radius_video);

        final String[] dataSources = new String[]{
                "http://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4",
                "http://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4",
                "http://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4"
        };


        chatHeadsView = liChatHeads.inflate(R.layout.view_layout_chat_heads, null);

//        mVideoSurfaceView[0] = (VideoSurfaceView) findViewById(R.id.video_surface_view1);
        mVideoSurfaceView[0] = (VideoSurfaceView) chatHeadsView.findViewById(R.id.video_surface_view);

        imgPlayPause = (ImageView) chatHeadsView.findViewById(R.id.img_play_pause);


//        mVideoSurfaceView[0] = new VideoSurfaceView(this);
//        mVideoSurfaceView[0].setCornerRadius(radius);
        mVideoSurfaceView[0].setCornerRadius(radius);
//        mVideoSurfaceView[2].setCornerRadius(radius);


        mPaperParams.x = 0;
        mPaperParams.y = 50;

        mWindowManager.addView(chatHeadsView, mPaperParams);

        for (int i = 0; i < mVideoSurfaceView.length; i++) {
            mediaPlayer = new MediaPlayer();
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
                        Utils.setBackground(VideoChatHeadService.this, imgPlayPause, R.drawable.ic_play);

                        Handler handler = new Handler();

                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {

                                imgPlayPause.setVisibility(View.GONE);
                                isMediaPrepared = true;

                                isPlaying = true;
                                mediaPlayer.start();
                                surfaceView.setVideoAspectRatio((float) mediaPlayer.getVideoWidth() /
                                        (float) mediaPlayer.getVideoHeight());


                            }
                        }, 1000);


                    }



                });

                surfaceView.setMediaPlayer(mediaPlayer);
            } catch (IOException e) {
                e.printStackTrace();
                mediaPlayer.release();
            }
        }
        addCrumpledPaperOnTouchListener();
    }


    private void addCrumpledPaperOnTouchListener() {


        chatHeadsView.setOnTouchListener(new View.OnTouchListener() {
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
                        addCloseView();

                        return true;
                    case MotionEvent.ACTION_UP:


//                        if(!isMoved){
//                            Toast.makeText(VideoChatHeadService.this, "clicked chat heads", Toast.LENGTH_SHORT).show();
//                        }

                        if ((Math.abs(initialTouchX - event.getRawX()) < 5) && (Math.abs(initialTouchY - event.getRawY()) < 5)) {

                            Toast.makeText(VideoChatHeadService.this, "clicked chat heads", Toast.LENGTH_SHORT).show();

                            if (isMediaPrepared) {
                                if (isPlaying) {

                                    isPlaying = false;
                                    imgPlayPause.setVisibility(View.VISIBLE);
//                                    Utils.setBackground(VideoChatHeadService.this, imgPlayPause, R.drawable.ic_pause);

                                    imgPlayPause.setImageResource(R.drawable.ic_pause);
                                    mediaPlayer.pause();

                                } else {

                                    isPlaying = true;

//                                    Utils.setBackground(VideoChatHeadService.this, imgPlayPause, R.drawable.ic_play);

                                    imgPlayPause.setImageResource(R.drawable.ic_play);

                                    new Handler().postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            imgPlayPause.setVisibility(View.GONE);
                                            mediaPlayer.start();

                                        }
                                    }, 1000);


                                }
                            }
                        }

                        int centerOfScreenByX = windowWidth / 2;

                        // remove crumpled paper when the it is in the recycle bin's area
                        if ((mPaperParams.y > windowHeight - closeView.getHeight() - chatHeadsView.getHeight()) &&
                                ((mPaperParams.x > centerOfScreenByX - ivRecycleBin.getWidth() - chatHeadsView.getWidth() / 2) && (mPaperParams.x < centerOfScreenByX + ivRecycleBin.getWidth() / 2))) {
                            mVibrator.vibrate(100);


                            if (isMediaPrepared) {
                                mediaPlayer.stop();

                            }

                            stopSelf();
                        }


                        // always remove recycle bin ImageView when paper is dropped
                        mWindowManager.removeView(closeView);
                        ivRecycleBin = null;

                        return true;
                    case MotionEvent.ACTION_MOVE:
                        // move paper ImageView

                        mPaperParams.x = initialX + (int) (initialTouchX - event.getRawX());
                        mPaperParams.y = initialY + (int) (event.getRawY() - initialTouchY);
                        mWindowManager.updateViewLayout(chatHeadsView, mPaperParams);
                        return true;
                }
                return false;
            }
        });
    }

    private void addCloseView() {

        liClose = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);

        // add recycle bin ImageView centered on the bottom of the screen
        mRecycleBinParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        mRecycleBinParams.gravity = Gravity.BOTTOM | Gravity.CENTER;
        mRecycleBinParams.height = 400;
        mRecycleBinParams.width = WindowManager.LayoutParams.MATCH_PARENT;

        closeView = liClose.inflate(R.layout.view_layout_close, null);

        ivRecycleBin = (ImageView) closeView.findViewById(R.id.img_close);

//        ivRecycleBin = new ImageView(this);
//        ivRecycleBin.setImageResource(R.drawable.ic_close);

        mRecycleBinParams.x = 0;
        mRecycleBinParams.y = 0;

        mWindowManager.addView(closeView, mRecycleBinParams);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {

        // remove views on destroy!
        if (chatHeadsView != null) {
            mWindowManager.removeView(chatHeadsView);
            mVideoSurfaceView[0] = null;

            imgPlayPause = null;
        }

        if (ivRecycleBin != null) {
            mWindowManager.removeView(closeView);
            ivRecycleBin = null;
        }

        if (isMediaPrepared) {
            mediaPlayer.stop();
        }

        super.onDestroy();

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
//        player.setDisplay(holder);
//        player.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

        Log.i("VideoChatHeads", "surfaceChanged");
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

        Log.i("VideoChatHeads", "surfaceDestroyed");

    }


}
