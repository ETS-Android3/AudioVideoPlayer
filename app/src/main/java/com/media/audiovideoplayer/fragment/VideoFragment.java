package com.media.audiovideoplayer.fragment;


import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.media.audiovideoplayer.R;
import com.media.audiovideoplayer.adapter.VideoPlayerAdapter;
import com.media.audiovideoplayer.datamodel.VideoData;

import java.util.ArrayList;

public class VideoFragment extends Fragment {


    private static RecyclerView videoPlayerRecyclerView;
    private VideoPlayerAdapter videoPlayerAdapter;
    public  ArrayList<VideoData> videoDataArrayList;
    private boolean isGridViewChanged;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_video, container, false);
        if (view != null) {
            videoPlayerRecyclerView = view.findViewById(R.id.video_recycler_view);
            videoPlayerRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 1));
            ArrayList<VideoData> videoData = loadVideosFromInternalStorage();
            if (null != videoData) {
                if (!videoData.isEmpty())
                    videoPlayerAdapter = new VideoPlayerAdapter(videoData, getActivity(), getContext());
                else
                    Toast.makeText(getContext(), "No Video Files to be loaded", Toast.LENGTH_LONG).show();
                videoPlayerRecyclerView.setAdapter(videoPlayerAdapter);
            }
        }
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.change_layout, menu);
        if (isGridViewChanged)
            menu.getItem(1).setIcon(R.drawable.grid_view);
        else
            menu.getItem(1).setIcon(R.drawable.list_view);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.layout_change) {
            if (!isGridViewChanged) {
                videoPlayerRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
                videoPlayerRecyclerView.setAdapter(videoPlayerAdapter);
                item.setIcon(R.drawable.grid_view);
                isGridViewChanged = true;
            } else {
                videoPlayerRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 1));
                videoPlayerRecyclerView.setAdapter(videoPlayerAdapter);
                item.setIcon(R.drawable.list_view);
                isGridViewChanged = false;
            }
        } else if (item.getItemId() == R.id.action_search) {
            SearchView searchView = (SearchView) item.getActionView();
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    if (null!= videoPlayerAdapter)
                        videoPlayerAdapter.getFilter().filter(newText);
                    return true;
                }
            });
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Loading Videos from Internal Storage
     *
     * @return
     */
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
                videoDataArrayList.add(videoData);
            } while (cursor.moveToNext());
        }
        assert cursor != null;
        cursor.close();
        return videoDataArrayList;
    }
}