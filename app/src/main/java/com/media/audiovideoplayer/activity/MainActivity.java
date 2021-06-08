package com.media.audiovideoplayer.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import android.os.Bundle;

import com.google.android.material.tabs.TabLayout;
import com.media.audiovideoplayer.R;
import com.media.audiovideoplayer.adapter.ViewPagerAdapter;
import com.media.audiovideoplayer.fragment.AudioFragment;
import com.media.audiovideoplayer.fragment.VideoFragment;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static TabLayout tabLayout;
    private static ViewPager viewPager;
    private ViewPagerAdapter viewPagerAdapter;
    private ArrayList<Fragment> fragments;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tabLayout = findViewById(R.id.tab_layout);
        viewPager = findViewById(R.id.view_Pager);
        initiateTabs();
    }

    public void initiateTabs() {
        fragments = new ArrayList<>();
        fragments.add(new VideoFragment());
        fragments.add(new AudioFragment());
        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), fragments);
        viewPager.setAdapter(viewPagerAdapter);
        tabLayout.setupWithViewPager(viewPager);

    }
}