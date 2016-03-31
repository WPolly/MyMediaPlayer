package com.xiaoshan.mymediaplayer.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.xiaoshan.mymediaplayer.fragment.MusicFragment;
import com.xiaoshan.mymediaplayer.fragment.VideoFragment;

/**
 * Created by xiaoshan on 2016/3/13.
 * 10:53
 */
public class MediaViewPagerAdapter extends FragmentPagerAdapter {
    public MediaViewPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new VideoFragment();
            case 1:
                return new MusicFragment();
        }

        return null;
    }

    @Override
    public int getCount() {
        return 2;
    }
}
