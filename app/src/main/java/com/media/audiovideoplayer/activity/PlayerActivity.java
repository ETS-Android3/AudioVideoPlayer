package com.media.audiovideoplayer.activity;

import static com.media.audiovideoplayer.adapter.VideoPlayerAdapter.resetAttributes;
import static com.media.audiovideoplayer.adapter.VideoPlayerAdapter.videoDataArrayList;
import static com.media.audiovideoplayer.service.PlayerService.exoPlayer;
import static com.media.audiovideoplayer.service.PlayerService.fullscreen;
import static com.media.audiovideoplayer.service.PlayerService.mediaControllerCompat;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.GranularRoundedCorners;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.PlayerControlView;
import com.google.android.exoplayer2.ui.StyledPlayerView;
import com.media.audiovideoplayer.R;
import com.media.audiovideoplayer.constants.AudioVideoConstants;
import com.media.audiovideoplayer.constants.AudioVideoEnum;
import com.media.audiovideoplayer.datamodel.VideoData;
import com.media.audiovideoplayer.service.PlayerService;
import com.media.audiovideoplayer.sharedpreferences.Preferences;

import java.util.ArrayList;
import java.util.Collections;

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
    private RecyclerView relatedVideosLayout;
    private boolean isTouched = false;

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
        relatedVideosLayout = findViewById(R.id.relatedVideosPanel);
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

    /**
     * Method used for Initiating Player Activity UI
     */

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
                relatedVideosLayout.setVisibility(View.INVISIBLE);
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
                relatedVideosLayout.setVisibility(View.GONE);
                new PlayMedia().execute();
                relatedVideosLayout.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
                relatedVideosLayout.setAdapter(new RelatedVideosPanel());
                break;
        }
    }

    /**
     * Method invoked for handling touch events in player activity
     */

    public void handleTouchEvent() {
        playerView.setOnTouchListener((v, event) -> {
            if (!isTouched) {
                playerControl.setVisibility(View.VISIBLE);
                playerControl.bringToFront();
                relatedVideosLayout.setVisibility(View.VISIBLE);
                Animation animation = AnimationUtils.loadAnimation(this, R.anim.pop_up);
                relatedVideosLayout.startAnimation(animation);
                isTouched = true;
                playerControlView.show();
                playerControlView.setShowTimeoutMs(5000);
                handler.postDelayed(() -> {
                            if (isTouched) {
                                playerControl.setVisibility(View.INVISIBLE);
                                Animation down = AnimationUtils.loadAnimation(this, R.anim.pop_down);
                                relatedVideosLayout.startAnimation(down);
                                down.setAnimationListener(new Animation.AnimationListener() {

                                    @Override
                                    public void onAnimationStart(Animation animation) {

                                    }

                                    @Override
                                    public void onAnimationEnd(Animation animation) {
                                        relatedVideosLayout.setVisibility(View.GONE);
                                        isTouched = false;
                                    }

                                    @Override
                                    public void onAnimationRepeat(Animation animation) {


                                    }
                                });
                            }
                        }
                        , 5000);
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

    /**
     * Method used for handling next previous button click
     */

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

    /**
     * Method used for handling fullscreen
     */

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


    /**
     * Method used for stretching the video to fullscreen
     */
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

    /**
     * Method used addFullScreen Functionality
     */

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
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
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


    /**
     * Method to show or hide player control
     */

    public void showOrHideUIControl() {
        playerControl.setVisibility(View.INVISIBLE);
    }

    /**
     * Method used for loading the video asynchronously
     */

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

    private class RelatedVideosPanel extends RecyclerView.Adapter<RelatedVideosPanel.RelatedVideosHolder> {

        private final static String TITLE = "Related Videos";


        @NonNull
        @Override
        public RelatedVideosHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.realted_videos_panel, parent, false);
            return new RelatedVideosHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RelatedVideosHolder holder, int position) {
            holder.title.setText(TITLE);
            holder.relatedVideos.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false));
            holder.relatedVideos.setAdapter(new RelatedVideosAdapter(videoDataArrayList));
        }

        @Override
        public int getItemCount() {
            return 1;
        }

        class RelatedVideosHolder extends RecyclerView.ViewHolder {

            private final TextView title;
            private RecyclerView relatedVideos;

            public RelatedVideosHolder(@NonNull View itemView) {
                super(itemView);
                title = itemView.findViewById(R.id.itemTitle);
                ;
                relatedVideos = itemView.findViewById(R.id.relatedVideos);
            }
        }


    }


    private class RelatedVideosAdapter extends RecyclerView.Adapter<RelatedVideosAdapter.RelatedVideos> {

        private final ArrayList<VideoData> videoData;

        public RelatedVideosAdapter(ArrayList<VideoData> dataArrayList) {
            this.videoData = dataArrayList;
            Collections.shuffle(videoData);
        }

        @NonNull
        @Override
        public RelatedVideos onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.related_videos, parent, false);
            return new RelatedVideos(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RelatedVideos holder, int position) {
            Glide.with(getApplicationContext()).asBitmap().load(videoData.get(position).getUrl()).into(holder.relatedVideosImage);
            holder.relatedVideosTitle.setText(videoData.get(position).getDisplayName());
        }

        @Override
        public int getItemCount() {
            return videoData.size();
        }

        class RelatedVideos extends RecyclerView.ViewHolder {

            private TextView relatedVideosTitle;
            private ImageView relatedVideosImage;

            public RelatedVideos(@NonNull View itemView) {
                super(itemView);
                relatedVideosTitle = itemView.findViewById(R.id.related_title);
                relatedVideosImage = itemView.findViewById(R.id.related_image);

                itemView.setOnClickListener(v -> {
                    sharedPreferences = Preferences.getSharedPreferences(getApplicationContext());
                    Intent playerService = new Intent(getApplicationContext(), PlayerService.class);
                    playerService.setAction(AudioVideoConstants.START_FOREGROUND);
                    sharedPreferences.edit()
                            .putInt("index", getAdapterPosition())
                            .putString("title", videoDataArrayList.get(getAdapterPosition()).getDisplayName())
                            .putString("artist", videoDataArrayList.get(getAdapterPosition()).getTitle())
                            .putString("filePath", videoDataArrayList.get(getAdapterPosition()).getUrl())
                            .putString("artist", videoDataArrayList.get(getAdapterPosition()).getTitle())
                            .putString("source", "VIDEO")
                            .putString("action", "def")
                            .putLong("duration", videoDataArrayList.get(getAdapterPosition()).getDuration())
                            .apply();
                    if (null != exoPlayer) {
                        //added start service just in case if service is not active
                        playerActivity.startService(playerService);
                        resetAttributes();
                        mediaControllerCompat.getTransportControls().play();
                        exoPlayer.seekTo(0);
                    } else {
                        playerActivity.startService(playerService);
                    }
                });
            }
        }


    }

    /**
     * Method used for loading album art for audio files
     *
     * @param fileUrl
     * @return
     */

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