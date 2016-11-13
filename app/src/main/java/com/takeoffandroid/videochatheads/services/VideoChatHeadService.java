package com.takeoffandroid.videochatheads.services;

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
import android.widget.ImageView;
import android.widget.Toast;

import com.takeoffandroid.videochatheads.R;
import com.takeoffandroid.videochatheads.utils.Constants;
import com.takeoffandroid.videochatheads.utils.Utils;
import com.takeoffandroid.videochatheads.views.VideoSurfaceView;

import java.io.IOException;


public class VideoChatHeadService extends Service implements SurfaceHolder.Callback {

    // constants
    public static final String BASIC_TAG = VideoChatHeadService.class.getName();

    // variables
    private WindowManager mWindowManager;
    private Vibrator mVibrator;
    private WindowManager.LayoutParams mVideoViewParams;
    private WindowManager.LayoutParams mCloseViewParams;
    private int windowHeight;
    private int windowWidth;

    private View closeView, chatHeadsView;
    private LayoutInflater liClose, liChatHeads;

    private ImageView ivCloseView, imgPlayPause;


    private VideoSurfaceView[] mVideoSurfaceView = new VideoSurfaceView[1];
    private MediaPlayer mediaPlayer;
    private boolean isMediaPrepared, isPlaying;


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

        mVideoViewParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,

                PixelFormat.TRANSLUCENT);


        DisplayMetrics displaymetrics = new DisplayMetrics();
        mWindowManager.getDefaultDisplay().getMetrics(displaymetrics);
        windowHeight = displaymetrics.heightPixels;
        windowWidth = displaymetrics.widthPixels;

        mVideoViewParams.gravity = Gravity.TOP | Gravity.RIGHT;
        mVideoViewParams.height = Constants.VIDEO_VIEW_CIRCLE_SIZE;
        mVideoViewParams.width = Constants.VIDEO_VIEW_CIRCLE_SIZE;


//        final int radius = getResources()
//                .getDimensionPixelOffset(R.dimen.corner_radius_video);



        chatHeadsView = liChatHeads.inflate(R.layout.view_layout_chat_heads, null);

        mVideoSurfaceView[0] = (VideoSurfaceView) chatHeadsView.findViewById(R.id.video_surface_view);

        imgPlayPause = (ImageView) chatHeadsView.findViewById(R.id.img_play_pause);


        mVideoSurfaceView[0].setCornerRadius(Constants.VIDEO_VIEW_CORNER_RADIUS);

        mVideoViewParams.x = 0;
        mVideoViewParams.y = 50;

        mWindowManager.addView(chatHeadsView, mVideoViewParams);

            mediaPlayer = new MediaPlayer();
            final VideoSurfaceView surfaceView = mVideoSurfaceView[0];
            final String dataSource = "http://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4";
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
        addViewOnTouchListener();
    }


    private void addViewOnTouchListener() {


        chatHeadsView.setOnTouchListener(new View.OnTouchListener() {
            private int initialX;
            private int initialY;
            private float initialTouchX;
            private float initialTouchY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:


                        initialX = mVideoViewParams.x;
                        initialY = mVideoViewParams.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        // add closeview when moving video view
                        addCloseView();

                        return true;
                    case MotionEvent.ACTION_UP:

                        if ((Math.abs(initialTouchX - event.getRawX()) < 5) && (Math.abs(initialTouchY - event.getRawY()) < 5)) {


                            if (isMediaPrepared) {
                                if (isPlaying) {

                                    Toast.makeText(VideoChatHeadService.this, "Pause Video", Toast.LENGTH_SHORT).show();

                                    isPlaying = false;
                                    imgPlayPause.setVisibility(View.VISIBLE);
//                                    Utils.setBackground(VideoChatHeadService.this, imgPlayPause, R.drawable.ic_pause);

                                    imgPlayPause.setImageResource(R.drawable.ic_pause);
                                    mediaPlayer.pause();

                                } else {

                                    isPlaying = true;

                                    Toast.makeText(VideoChatHeadService.this, "Play Video", Toast.LENGTH_SHORT).show();

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

                        // remove video view when the it is in the close view area
                        if ((mVideoViewParams.y > windowHeight - closeView.getHeight() - chatHeadsView.getHeight()) &&
                                ((mVideoViewParams.x > centerOfScreenByX - ivCloseView.getWidth() - chatHeadsView.getWidth() / 2) && (mVideoViewParams.x < centerOfScreenByX + ivCloseView.getWidth() / 2))) {
                            mVibrator.vibrate(100);


                            if (isMediaPrepared) {
                                mediaPlayer.stop();

                            }

                            stopSelf();
                        }


                        // always remove close view ImageView when video view is dropped
                        mWindowManager.removeView(closeView);
                        ivCloseView = null;

                        return true;
                    case MotionEvent.ACTION_MOVE:
                        // move videoview ImageView

                        mVideoViewParams.x = initialX + (int) (initialTouchX - event.getRawX());
                        mVideoViewParams.y = initialY + (int) (event.getRawY() - initialTouchY);
                        mWindowManager.updateViewLayout(chatHeadsView, mVideoViewParams);
                        return true;
                }
                return false;
            }
        });
    }

    private void addCloseView() {

        liClose = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);

        // add close view ImageView centered on the bottom of the screen
        mCloseViewParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        mCloseViewParams.gravity = Gravity.BOTTOM | Gravity.CENTER;
        mCloseViewParams.height = 400;
        mCloseViewParams.width = WindowManager.LayoutParams.MATCH_PARENT;

        closeView = liClose.inflate(R.layout.view_layout_close, null);

        ivCloseView = (ImageView) closeView.findViewById(R.id.img_close);

        mCloseViewParams.x = 0;
        mCloseViewParams.y = 0;

        mWindowManager.addView(closeView, mCloseViewParams);
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

        if (ivCloseView != null) {
            mWindowManager.removeView(closeView);
            ivCloseView = null;
        }

        if (isMediaPrepared) {
            mediaPlayer.stop();
        }

        super.onDestroy();

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {


    }


}
