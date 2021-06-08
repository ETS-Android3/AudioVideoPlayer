package com.media.audiovideoplayer.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.google.android.exoplayer2.ui.PlayerControlView;
import com.google.android.exoplayer2.ui.PlayerView;
import com.media.audiovideoplayer.R;
import static com.media.audiovideoplayer.service.PlayerService.exoPlayer;

public class PlayerActivity extends AppCompatActivity {

    public PlayerView playerView;
    public ImageView audioImageView;
    public PlayerControlView playerControlView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        playerView=findViewById(R.id.video_player);
        audioImageView=findViewById(R.id.audio_player_image);
        audioImageView.setVisibility(View.INVISIBLE);
        playerControlView=findViewById(R.id.video_controller);
        playerView.setPlayer(exoPlayer);
        playerControlView.setPlayer(exoPlayer);
    }
}