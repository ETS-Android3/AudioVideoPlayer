package com.media.audiovideoplayer.adapter;

import static com.media.audiovideoplayer.constants.AudioVideoConstants.ALBUM_ART;
import static com.media.audiovideoplayer.service.PlayerService.currentPosition;
import static com.media.audiovideoplayer.service.PlayerService.exoPlayer;
import static com.media.audiovideoplayer.service.PlayerService.isPaused;
import static com.media.audiovideoplayer.service.PlayerService.mediaControllerCompat;

import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.SectionIndexer;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.media.audiovideoplayer.R;
import com.media.audiovideoplayer.activity.PlayerActivity;
import com.media.audiovideoplayer.constants.AudioVideoConstants;
import com.media.audiovideoplayer.datamodel.AudioData;
import com.media.audiovideoplayer.datamodel.VideoData;
import com.media.audiovideoplayer.service.PlayerService;
import com.media.audiovideoplayer.sharedpreferences.Preferences;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Locale;

public class AudioPlayerAdapter extends RecyclerView.Adapter<AudioPlayerAdapter.AudioHolder> implements SectionIndexer  {

    public static ArrayList<AudioData> audioData;
    ArrayList<AudioData> audioDataAll;
    private final Activity av;
    private final Context context;
    private ArrayList<Integer> sectionsList;
    public static int selectedPosition = -1;
    public static AudioPlayerAdapter audioPlayerAdapter;

    public AudioPlayerAdapter(ArrayList<AudioData> audioDataArrayList, Activity activity, Context context) {
        audioData = audioDataArrayList;
        this.audioDataAll = new ArrayList<>(audioData);
        this.context = context;
        this.av = activity;
        audioPlayerAdapter = this;
    }

    @NonNull
    @Override
    public AudioHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.audio_list, parent, false);
        return new AudioHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AudioHolder holder, int position) {
        holder.bindAudioData(audioData.get(position).getMusicTitle(), audioData.get(position).getArtist(), audioData.get(position).getAlbumArt());

        //Updating Currently playing song gif dynamically
        if (selectedPosition == position) {
            Glide.with(context).asGif().load(R.drawable.musicplay).into(holder.music_gif);
            holder.title_text_view.setEllipsize(TextUtils.TruncateAt.END);
            holder.artist_text_view.setEllipsize(TextUtils.TruncateAt.END);
        } else {
            holder.music_gif.setImageBitmap(null);
        }

        //Toggle Pop up Menu
        holder.menu.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(context, v);
            popupMenu.inflate(R.menu.music_operation);
            popupMenu.setOnMenuItemClickListener(item -> {
                switch (item.getItemId()) {
                    case R.id.share:
                        Intent shareIntent = new Intent(Intent.ACTION_SEND);
                        shareIntent.setType("audio/*");
                        shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(audioData.get(position).getFileUrl()));
                        context.startActivity(Intent.createChooser(shareIntent, "Share Song"));
                        break;
                }
                return false;
            });
            popupMenu.show();
        });

    }

    @Override
    public int getItemCount() {
        return audioData.size();
    }


    @Override
    public String[] getSections() {
        sectionsList = new ArrayList<>();
        ArrayList<String> sections = new ArrayList<>();
        int i = 0;
        int size = audioData.size();
        while (i < size) {
            String musicTitle = String.valueOf(audioData.get(i).getMusicTitle().toUpperCase(Locale.ROOT).charAt(0));
            String[] stringArray = musicTitle.split("\\W+");
            StringBuilder result = new StringBuilder();
            for (String j : stringArray) {
                result.append(j);
            }
            if (!sections.contains(result.toString())) {
                sections.add(result.toString());
                sectionsList.add(i);
            }
            i++;
            Collections.sort(sections);
        }
        return sections.toArray(new String[0]);
    }

    @Override
    public int getPositionForSection(int sectionIndex) {
        return sectionsList.get(sectionIndex);
    }

    @Override
    public int getSectionForPosition(int position) {
        return 0;
    }

    class AudioHolder extends RecyclerView.ViewHolder {

        private final ImageView audioImageView;
        private final TextView title_text_view;
        private final TextView artist_text_view;
        private final ImageView music_gif;
        private TextView menu;
        private SharedPreferences sharedPreferences;
        private RelativeLayout relativeLayout;

        public AudioHolder(@NonNull View itemView) {
            super(itemView);
            audioImageView = itemView.findViewById(R.id.music_image);
            title_text_view = itemView.findViewById(R.id.music_item_label);
            artist_text_view = itemView.findViewById(R.id.music_item_artist);
            menu = itemView.findViewById(R.id.music_card_menu);
            music_gif = itemView.findViewById(R.id.music_gif);
            relativeLayout = itemView.findViewById(R.id.relative_music_layout);
            relativeLayout.setOnClickListener(v -> {
                sharedPreferences = Preferences.getSharedPreferences(context);
                Intent playerActivityIntent = new Intent(context, PlayerActivity.class);
                Intent playerService = new Intent(context, PlayerService.class);
                playerService.setAction(AudioVideoConstants.START_FOREGROUND);
                sharedPreferences.edit()
                        .putInt("index", audioData.get(getAdapterPosition()).getIndex())
                        .putString("title", audioData.get(getAdapterPosition()).getMusicTitle())
                        .putString("filePath", audioData.get(getAdapterPosition()).getFileUrl())
                        .putString("artist", audioData.get(getAdapterPosition()).getArtist())
                        .putString("source", "AUDIO")
                        .putString("action", "def")
                        .putLong("duration", audioData.get(getAdapterPosition()).getDuration())
                        .apply();
                if (null != exoPlayer) {
                    //added start service just in case if service is not active
                    av.startService(playerService);
                    resetAttributes();
                    mediaControllerCompat.getTransportControls().play();
                    exoPlayer.seekTo(0);
                    av.startActivity(playerActivityIntent);
                } else {
                    av.startService(playerService);
                    av.startActivity(playerActivityIntent);
                }
                updateMusicRecyclerViewGraphics(true);
            });
        }

        public void updateMusicRecyclerViewGraphics(boolean isValidSongIndex) {
            selectedPosition = (isValidSongIndex) ? sharedPreferences.getInt("index", -1) : -1;
            notifyDataSetChanged();
        }

        public void bindAudioData(String title, String artist, String fileUrl) {
            title_text_view.setText(title);
            artist_text_view.setText(artist);
            title_text_view.setSelected(true);
            artist_text_view.setSelected(true);
            new ImageLoader().execute(getAlbumArt(fileUrl));
        }

        public Uri getAlbumArt(String fileUrl) {
            final Uri albumUri = Uri.parse(ALBUM_ART);
            return ContentUris.withAppendedId(albumUri, Long.parseLong(fileUrl));
        }

        public class ImageLoader extends AsyncTask<Uri, Void, Uri> {
            Uri icon;
            @Override
            protected Uri doInBackground(Uri... strings) {
                icon = strings[0];
                return icon;
            }

            @Override
            protected void onPostExecute(Uri bitmap) {
                Glide.with(context).load(bitmap).error(R.drawable.music_image).diskCacheStrategy(DiskCacheStrategy.ALL).skipMemoryCache(false).into(audioImageView);
            }
        }
    }

    public void resetAttributes() {
        exoPlayer.pause();
        isPaused = true;
        currentPosition = 0L;
    }

}
