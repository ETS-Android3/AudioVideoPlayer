package com.media.audiovideoplayer.fragment;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.media.audiovideoplayer.R;
import com.media.audiovideoplayer.adapter.VideoPlayerAdapter;
import com.media.audiovideoplayer.datamodel.VideoData;

import java.util.ArrayList;
import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link VideoFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class VideoFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private static RecyclerView videoPlayerRecyclerView;
    private VideoPlayerAdapter videoPlayerAdapter;
    private ArrayList<VideoData> videoDataArrayList;
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public VideoFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment VideoFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static VideoFragment newInstance(String param1, String param2) {
        VideoFragment fragment = new VideoFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_video, container, false);
        if (view != null) {
            videoPlayerRecyclerView = view.findViewById(R.id.video_recycler_view);
            videoPlayerRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 1));
            videoPlayerAdapter = new VideoPlayerAdapter(loadVideosFromInternalStorage(), getActivity(), getContext());
            videoPlayerRecyclerView.setAdapter(videoPlayerAdapter);
        }
        return view;
    }

    public ArrayList<VideoData> loadVideosFromInternalStorage() {
        videoDataArrayList = new ArrayList<>();
        ContentResolver contentResolver = getContext().getContentResolver();
        Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        Cursor cursor = contentResolver.query(uri, null, null, null, MediaStore.Video.Media.DEFAULT_SORT_ORDER);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                String title = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DISPLAY_NAME));
                String videoTitle = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.TITLE));
                String data = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATA));
                long duration = cursor.getLong(cursor.getColumnIndex(MediaStore.Video.Media.DURATION));
                VideoData videoData = new VideoData(title, videoTitle, data, duration);
              /*  if (videoData.getTitle().toLowerCase(Locale.ROOT).contains("java")) {
                    videoDataArrayList.add(videoData);
                }*/
                videoDataArrayList.add(videoData);
            } while (cursor.moveToNext());
        }
        assert cursor != null;
        cursor.close();
        return videoDataArrayList;
    }
}