package com.media.audiovideoplayer.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SectionIndexer;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.media.audiovideoplayer.R;
import com.media.audiovideoplayer.datamodel.AudioData;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;

public class AudioPlayerAdapter extends RecyclerView.Adapter<AudioPlayerAdapter.AudioHolder> implements SectionIndexer {

    private ArrayList<AudioData> audioData;
    private Activity av;
    private Context context;
    private ArrayList<Integer> sectionsList;

    public AudioPlayerAdapter(ArrayList<AudioData> audioDataArrayList, Activity activity, Context context) {
        this.audioData = audioDataArrayList;
        this.context = context;
        this.av = activity;
    }

    @NonNull
    @Override
    public AudioHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.audio_list, parent, false);
        return new AudioHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AudioHolder holder, int position) {
        holder.bindAudioData(audioData.get(position).getMusicTitle(), audioData.get(position).getArtist(), audioData.get(position).getFileUrl());
    }

    @Override
    public int getItemCount() {
        return audioData.size();
    }

    @Override
    public String[] getSections() {
        sectionsList = new ArrayList<>();
        ArrayList<String> sections = new ArrayList<>();
        String[] section;
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
        return sections.toArray(new String[sections.size()]);
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

        private ImageView audioImageView;
        private TextView title_text_view;
        private TextView artist_text_view;
        private TextView menu;

        public AudioHolder(@NonNull View itemView) {
            super(itemView);
            audioImageView = itemView.findViewById(R.id.music_image);
            title_text_view = itemView.findViewById(R.id.music_item_label);
            artist_text_view = itemView.findViewById(R.id.music_item_artist);
            menu = itemView.findViewById(R.id.music_card_menu);
        }

        public void bindAudioData(String title, String artist, String fileUrl) {
            title_text_view.setText(title);
            artist_text_view.setText(artist);
            Glide.with(context).asBitmap().skipMemoryCache(true).load(getImage(fileUrl)).into(audioImageView);
        }

        public Bitmap getImage(String fileUrl) {
            MediaMetadataRetriever retriever;
            Bitmap icon;
            try {
                retriever = new MediaMetadataRetriever();
                retriever.setDataSource(fileUrl);
                byte[] data = retriever.getEmbeddedPicture();
                if (data != null) {
                    icon = BitmapFactory.decodeByteArray(data, 0, data.length);
                } else {
                    icon = BitmapFactory.decodeResource(context.getResources(), R.drawable.music_image);
                }
            } catch (Exception e) {
                icon = BitmapFactory.decodeResource(context.getResources(), R.drawable.music_image);
            }
            return icon;
        }

        /*public Uri getImageUri(Context inContext, Bitmap inImage) {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
            String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
            return Uri.parse(path);
        }*/

    }
}
