package com.media.audiovideoplayer.adapter;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ViewPagerAdapter extends FragmentPagerAdapter {


    private List<Fragment> mFragmentList;

    public ViewPagerAdapter(FragmentManager fm, List<Fragment> fragment) {
        super(fm);
        this.mFragmentList = fragment;
        notifyDataSetChanged();
    }

    @NotNull
    @Override
    public Fragment getItem(int position) {
        return mFragmentList.get(position);
    }

    @Override
    public int getCount() {
        return mFragmentList == null ? 0 : mFragmentList.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return (position == 0) ? "Video" : "Music";
    }
}
