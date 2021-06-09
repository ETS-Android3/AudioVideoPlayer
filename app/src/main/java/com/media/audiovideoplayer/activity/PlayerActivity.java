package com.media.audiovideoplayer.activity;

import static com.media.audiovideoplayer.service.PlayerService.exoPlayer;
import static com.media.audiovideoplayer.service.PlayerService.mediaControllerCompat;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.GranularRoundedCorners;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.PlayerControlView;
import com.google.android.exoplayer2.ui.PlayerView;
import com.media.audiovideoplayer.R;
import com.media.audiovideoplayer.constants.AudioVideoEnum;
import com.media.audiovideoplayer.sharedpreferences.Preferences;

public class PlayerActivity extends AppCompatActivity {
    public PlayerView playerView;
    public ImageView audioImageView;
    public PlayerControlView playerControlView;
    public static Activity playerActivity;
    public SharedPreferences sharedPreferences;
    private String audioFilePath;
    private ImageButton nextButton, prevButton, playPauseButton;
    private ImageButton fullScreenButton;
    private LinearLayout playerControl;
    private Handler handler;
    private boolean fullscreen = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        playerView = findViewById(R.id.video_player);
        audioImageView = findViewById(R.id.audio_player_image);
        playerControl = findViewById(R.id.showProgress);
        playPauseButton = findViewById(R.id.playPause);
        nextButton = findViewById(R.id.next);
        prevButton = findViewById(R.id.prev);
        playerControlView = findViewById(R.id.player_control);
        fullScreenButton = findViewById(R.id.exo_fullscreen_button);
        sharedPreferences = Preferences.getSharedPreferences(getApplicationContext());
        playerActivity = this;
        handler = new Handler();
        initiatePlayerUI();
        handleFullScreenIcon();
        handleNextPrevButtonClick();
        showOrHideUIControl();
        handleTouchEvent();
    }

    public void initiatePlayerUI() {
        if (null != exoPlayer) {
            if (exoPlayer.getPlayWhenReady()) {
                playerControlView.setPlayer(exoPlayer);
                playPauseButton.setImageResource(R.drawable.pause);
            } else {
                playPauseButton.setImageResource(R.drawable.play);
            }
        }
        switch (AudioVideoEnum.valueOf(sharedPreferences.getString("source", "def"))) {
            case AUDIO:
                playerView.setVisibility(View.INVISIBLE);
                audioImageView.setVisibility(View.VISIBLE);
                audioFilePath = sharedPreferences.getString("filePath", "def");
                Bitmap audioIcon = getBitmapImage(audioFilePath);
                Glide.with(getApplicationContext()).asBitmap().load(audioIcon).transform(new GranularRoundedCorners(15, 15, 15, 15)).into(audioImageView);
                break;
            case VIDEO:
                playerView.setVisibility(View.VISIBLE);
                audioImageView.setVisibility(View.INVISIBLE);
                playerView.setPlayer(exoPlayer);
                addFunctionalityFullScreen();
                break;
        }
    }

    public void handleTouchEvent() {
        playerView.setOnTouchListener((v, event) -> {
            playerControl.setVisibility(View.VISIBLE);
            if (event.getAction() == MotionEvent.ACTION_UP) {
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        playerControl.setVisibility(View.INVISIBLE);
                    }
                }, 5000);
                playerControlView.show();
                playerControlView.setShowTimeoutMs(5000);
            }
            return true;
        });
        audioImageView.setOnTouchListener((v, event) -> {
            playerControl.setVisibility(View.VISIBLE);
            if (event.getAction() == MotionEvent.ACTION_UP) {
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        playerControl.setVisibility(View.INVISIBLE);
                    }
                }, 5000);
                playerControlView.show();
                playerControlView.setShowTimeoutMs(5000);
            }
            return true;
        });
    }

    public void handleNextPrevButtonClick() {
        playPauseButton.setOnClickListener(v -> {
            if (exoPlayer.getPlayWhenReady()) {
                mediaControllerCompat.getTransportControls().pause();
                playPauseButton.setImageResource(R.drawable.play);
            } else {
                mediaControllerCompat.getTransportControls().play();
                playPauseButton.setImageResource(R.drawable.pause);
            }
        });
        nextButton.setOnClickListener(v -> {
            mediaControllerCompat.getTransportControls().skipToNext();
        });
        prevButton.setOnClickListener(v -> {
            mediaControllerCompat.getTransportControls().skipToPrevious();
        });

    }

    public void handleFullScreenIcon() {
        if (fullscreen)
            fullScreenButton.setImageResource(R.drawable.fullscreen_exit);
        else
            fullScreenButton.setImageResource(R.drawable.fullscreen_enter);
    }

    public Bitmap getBitmapImage(String fileUrl) {
        MediaMetadataRetriever retriever;
        Bitmap icon;
        try {
            retriever = new MediaMetadataRetriever();
            retriever.setDataSource(fileUrl);
            byte[] data = retriever.getEmbeddedPicture();
            if (data != null) {
                icon = BitmapFactory.decodeByteArray(data, 0, data.length);
            } else {
                icon = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.music_image);
            }
        } catch (Exception e) {
            icon = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.music_image);
        }
        return icon;
    }

    public void addFunctionalityFullScreen() {
        fullScreenButton.setOnClickListener(v -> {
            if (fullscreen) {
                fullScreenButton.setImageResource(R.drawable.fullscreen_enter);
                getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
                if (getSupportActionBar() != null) {
                    getSupportActionBar().show();
                }
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);
                fullscreen = false;
            } else {
                fullScreenButton.setImageResource(R.drawable.fullscreen_exit);
                getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
                if (getSupportActionBar() != null) {
                    getSupportActionBar().hide();
                }
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FILL);
                fullscreen = true;
            }
        });
    }

    public void showOrHideUIControl() {
        playerControl.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onBackPressed() {
        if (fullscreen) {
            fullScreenButton.setImageDrawable(getResources().getDrawable(R.drawable.fullscreen_enter));
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
            if (getSupportActionBar() != null) {
                getSupportActionBar().show();
            }
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);
            fullscreen = false;
        } else {
            finish();
        }
    }
}