package com.media.audiovideoplayer.adapter;

import static com.media.audiovideoplayer.service.PlayerService.currentPosition;
import static com.media.audiovideoplayer.service.PlayerService.exoPlayer;
import static com.media.audiovideoplayer.service.PlayerService.isPaused;
import static com.media.audiovideoplayer.service.PlayerService.mediaControllerCompat;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.media.audiovideoplayer.R;
import com.media.audiovideoplayer.activity.PlayerActivity;
import com.media.audiovideoplayer.constants.AudioVideoConstants;
import com.media.audiovideoplayer.datamodel.VideoData;
import com.media.audiovideoplayer.service.PlayerService;
import com.media.audiovideoplayer.sharedpreferences.Preferences;

import java.util.ArrayList;

public class VideoPlayerAdapter extends RecyclerView.Adapter<VideoPlayerAdapter.VideoHolder> {


    public static ArrayList<VideoData> videoDataArrayList;
    private final Activity av;
    private final Context context;
    private SharedPreferences sharedPreferences;

    public VideoPlayerAdapter(ArrayList<VideoData> videoData, Activity activity, Context context) {
        videoDataArrayList = videoData;
        this.av = activity;
        this.context = context;

    }

    @NonNull
    @Override
    public VideoHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.video_list, parent, false);
        return new VideoHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VideoHolder holder, int position) {
        holder.bindData(videoDataArrayList.get(position).getUrl(), videoDataArrayList.get(position).getDisplayName());
        holder.share_video.setOnClickListener(v -> {
            // To be Added Later
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("video/*");
            intent.putExtra(Intent.EXTRA_STREAM, Uri.parse(videoDataArrayList.get(position).getUrl()));
            context.startActivity(Intent.createChooser(intent, "Share Video"));
        });
    }

    @Override
    public int getItemCount() {
        return videoDataArrayList.size();
    }

    public class VideoHolder extends RecyclerView.ViewHolder {
        private final TextView textView;
        private final ImageView imageView;
        private final ImageButton share_video;

        public VideoHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.video_card_text);
            imageView = itemView.findViewById(R.id.video_card_image);
            share_video = itemView.findViewById(R.id.share_video);

            itemView.setOnClickListener(v -> {
                sharedPreferences = Preferences.getSharedPreferences(context);
                Intent playerActivityIntent = new Intent(context, PlayerActivity.class);
                Intent playerService = new Intent(context, PlayerService.class);
                playerService.setAction(AudioVideoConstants.START_FOREGROUND);
                sharedPreferences.edit()
                        .putInt("index", getAdapterPosition())
                        .putString("title", videoDataArrayList.get(getAdapterPosition()).getDisplayName())
                        .putString("artist", videoDataArrayList.get(getAdapterPosition()).getTitle())
                        .putString("filePath", videoDataArrayList.get(getAdapterPosition()).getUrl())
                        .putString("source", "VIDEO")
                        .putString("action", "def")
                        .putLong("duration", videoDataArrayList.get(getAdapterPosition()).getDuration())
                        .apply();
                if (exoPlayer != null) {
                    if (exoPlayer.isPlaying()) {
                        resetAttributes();
                        mediaControllerCompat.getTransportControls().play();
                        exoPlayer.seekTo(0);
                        av.startActivity(playerActivityIntent);
                    } else {
                        av.startService(playerService);
                        resetAttributes();
                        mediaControllerCompat.getTransportControls().play();
                        av.startActivity(playerActivityIntent);
                        if (exoPlayer.isPlaying()) {
                            exoPlayer.seekTo(0);
                        } else {
                            exoPlayer.seekTo(0);
                        }
                    }
                } else {
                    av.startService(playerService);
                    av.startActivity(playerActivityIntent);
                }
            });
        }

        public void bindData(String imageUrl, String title) {
            textView.setText(title);
            Glide.with(context).asBitmap().load(imageUrl).into(imageView);
        }
    }

    public void resetAttributes() {
        exoPlayer.pause();
        isPaused = true;
        currentPosition = 0L;
    }

}
