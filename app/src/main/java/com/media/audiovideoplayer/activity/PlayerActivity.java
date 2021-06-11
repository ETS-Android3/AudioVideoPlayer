package com.media.audiovideoplayer.activity;

import static com.media.audiovideoplayer.service.PlayerService.exoPlayer;
import static com.media.audiovideoplayer.service.PlayerService.fullscreen;
import static com.media.audiovideoplayer.service.PlayerService.mediaControllerCompat;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.GranularRoundedCorners;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.PlayerControlView;
import com.google.android.exoplayer2.ui.StyledPlayerView;
import com.media.audiovideoplayer.R;
import com.media.audiovideoplayer.constants.AudioVideoEnum;
import com.media.audiovideoplayer.sharedpreferences.Preferences;

public class PlayerActivity extends AppCompatActivity {
    public StyledPlayerView playerView;
    public ImageView audioImageView;
    public PlayerControlView playerControlView;
    public static Activity playerActivity;
    public SharedPreferences sharedPreferences;
    private String audioFilePath;
    private ImageButton nextButton, prevButton, stretchVideo;
    public static ImageButton playPauseButton;
    private ImageButton fullScreenButton;
    private LinearLayout playerControl;
    private Handler handler;
    private TextView musicTitle;
    private boolean isVideoStretched = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        playerView = findViewById(R.id.video_player);
        audioImageView = findViewById(R.id.audio_player_image);
        musicTitle = findViewById(R.id.music_title);
        playerControl = findViewById(R.id.showProgress);
        playPauseButton = findViewById(R.id.playPause);
        stretchVideo = findViewById(R.id.stretch);
        nextButton = findViewById(R.id.next);
        prevButton = findViewById(R.id.prev);
        playerControlView = findViewById(R.id.player_control);
        fullScreenButton = findViewById(R.id.exo_fullscreen_button);
        sharedPreferences = Preferences.getSharedPreferences(getApplicationContext());
        playerActivity = this;
        handler = new Handler();
        initiatePlayerUI();
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
                musicTitle.setVisibility(View.VISIBLE);
                musicTitle.setSelected(true);
                musicTitle.setText(sharedPreferences.getString("title", "def"));
                playerView.setVisibility(View.INVISIBLE);
                audioImageView.setVisibility(View.VISIBLE);
                fullScreenButton.setVisibility(View.INVISIBLE);
                stretchVideo.setVisibility(View.INVISIBLE);
                audioFilePath = sharedPreferences.getString("filePath", "def");
                Bitmap audioIcon = getBitmapImage(audioFilePath);
                Glide.with(getApplicationContext()).asBitmap().load(audioIcon).transform(new GranularRoundedCorners(15, 15, 15, 15)).into(audioImageView);
                break;
            case VIDEO:
                musicTitle.setVisibility(View.INVISIBLE);
                playerView.setVisibility(View.VISIBLE);
                audioImageView.setVisibility(View.INVISIBLE);
                fullScreenButton.setVisibility(View.VISIBLE);
                stretchVideo.setVisibility(View.VISIBLE);
                new PlayMedia().execute();
                break;
        }
    }

    public void handleTouchEvent() {
        playerView.setOnTouchListener((v, event) -> {
            playerControl.setVisibility(View.VISIBLE);
            if (event.getAction() == MotionEvent.ACTION_UP) {
                handler.postDelayed(() -> {
                            playerControl.setVisibility(View.INVISIBLE);
                        }
                        , 5000);
                playerControlView.show();
                playerControlView.setShowTimeoutMs(5000);
            }
            return true;
        });
        audioImageView.setOnTouchListener((v, event) -> {
            playerControl.setVisibility(View.VISIBLE);
            if (event.getAction() == MotionEvent.ACTION_UP) {
                handler.postDelayed(() -> playerControl.setVisibility(View.INVISIBLE), 5000);
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

    public void handleFullScreen() {
        if (fullscreen) {
            fullScreenButton.setImageResource(R.drawable.fullscreen_exit);
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
            if (getSupportActionBar() != null) {
                getSupportActionBar().hide();
            }
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);
            stretchVideo.setImageResource(R.drawable.stretch_on);
        } else {
            fullScreenButton.setImageResource(R.drawable.fullscreen_enter);
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
            if (getSupportActionBar() != null) {
                getSupportActionBar().show();
            }
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);
            stretchVideo.setImageResource(R.drawable.stretch_on);
        }
    }

    public void stretchVideo() {
        stretchVideo.setOnClickListener(v -> {
            if (isVideoStretched) {
                stretchVideo.setImageResource(R.drawable.stretch_on);
                playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);
                isVideoStretched = false;
            } else {
                stretchVideo.setImageResource(R.drawable.stretch_off);
                playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FILL);
                isVideoStretched = true;
            }
        });
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
                stretchVideo.setImageResource(R.drawable.stretch_on);
                fullscreen = false;
            } else {
                fullScreenButton.setImageResource(R.drawable.fullscreen_exit);
                getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
                if (getSupportActionBar() != null) {
                    getSupportActionBar().hide();
                }
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                stretchVideo.setImageResource(R.drawable.stretch_on);
                playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);
                fullscreen = true;
            }
        });
    }

    public void showOrHideUIControl() {
        playerControl.setVisibility(View.INVISIBLE);
    }

    public class PlayMedia extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);
            playerView.setPlayer(exoPlayer);
            playerControlView.setPlayer(exoPlayer);
            addFunctionalityFullScreen();
            handleFullScreen();
            stretchVideo();
        }
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

    @Override
    public void onBackPressed() {
        if (fullscreen) {
            fullScreenButton.setImageResource(R.drawable.fullscreen_enter);
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
            if (getSupportActionBar() != null) {
                getSupportActionBar().show();
            }
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            stretchVideo.setImageResource(R.drawable.stretch_on);
            playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);
            fullscreen = false;
        } else {
            finish();
        }
    }


}