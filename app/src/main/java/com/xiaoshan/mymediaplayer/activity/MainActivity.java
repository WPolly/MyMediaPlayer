package com.xiaoshan.mymediaplayer.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.AnimationDrawable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.xiaoshan.mymediaplayer.R;
import com.xiaoshan.mymediaplayer.adapter.MediaViewPagerAdapter;
import com.xiaoshan.mymediaplayer.service.AudioPlayService;
import com.xiaoshan.mymediaplayer.utils.ServiceStatusUtils;
import com.xiaoshan.mymediaplayer.utils.UIUtils;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class MainActivity extends BaseActivity {


    @InjectView(R.id.tv_title_video)
    TextView mTvTitleVideo;
    @InjectView(R.id.tv_title_music)
    TextView mTvTitleMusic;
    @InjectView(R.id.indicator)
    View mIndicator;
    @InjectView(R.id.vp_main)
    ViewPager mVpMain;
    @InjectView(R.id.fab_playing)
    FloatingActionButton mFabPlaying;
    private int mIndicatorWidth;
    private AnimationDrawable mFabPlayingDrawable;
    private LocalBroadcastManager mLocalBroadcastManager;
    private AudioPlayBroadcastReceiver mAudioPlayBroadcastReceiver;
    private AudioPauseBroadcastReceiver mAudioPauseBroadcastReceiver;

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_main;
    }

    @Override
    protected void initView() {
        ButterKnife.inject(this);
    }

    @Override
    protected void initData() {
        mLocalBroadcastManager = LocalBroadcastManager.getInstance(getApplicationContext());

        mAudioPlayBroadcastReceiver = new AudioPlayBroadcastReceiver();
        IntentFilter intentFilterPlay = new IntentFilter("com.xiaoshan.mymediaplayer.play");
        mLocalBroadcastManager.registerReceiver(mAudioPlayBroadcastReceiver, intentFilterPlay);

        mAudioPauseBroadcastReceiver = new AudioPauseBroadcastReceiver();
        IntentFilter intentFilterPause = new IntentFilter("com.xiaoshan.mymediaplayer.pause");
        mLocalBroadcastManager.registerReceiver(mAudioPauseBroadcastReceiver, intentFilterPause);

        MediaViewPagerAdapter adapter = new MediaViewPagerAdapter(getSupportFragmentManager());
        int count = adapter.getCount();
        int displayWidth = getWindow().getWindowManager().getDefaultDisplay().getWidth();
        mIndicatorWidth = displayWidth / count;
        ViewGroup.LayoutParams layoutParams = mIndicator.getLayoutParams();
        layoutParams.width = mIndicatorWidth;
        mIndicator.requestLayout();

        mVpMain.setAdapter(adapter);
        mVpMain.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                float x = mIndicatorWidth * (position + positionOffset);
                mIndicator.setTranslationX(x);
            }

            @Override
            public void onPageSelected(int position) {
                if (position == 0) {
                    mTvTitleVideo.animate().scaleX(1.2f).scaleY(1.2f);
                    mTvTitleMusic.animate().scaleX(1.0f).scaleY(1.0f);
                    return;
                }

                if (position == 1) {
                    mTvTitleMusic.animate().scaleX(1.2f).scaleY(1.2f);
                    mTvTitleVideo.animate().scaleX(1.0f).scaleY(1.0f);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        mVpMain.setPageTransformer(true, new ViewPager.PageTransformer() {
            private static final float MIN_SCALE = 0.75f;

            @Override
            public void transformPage(View view, float position) {

                int pageWidth = view.getWidth();

                if (position < -1) { // [-Infinity,-1)
                    // This page is way off-screen to the left.
                    view.setAlpha(0);

                } else if (position <= 0) { // [-1,0]
                    // Use the default slide transition when moving to the left page
                    view.setAlpha(1);
                    view.setTranslationX(0);
                    view.setScaleX(1);
                    view.setScaleY(1);

                } else if (position <= 1) { // (0,1]
                    // Fade the page out.
                    view.setAlpha(1 - position);

                    // Counteract the default slide transition
                    view.setTranslationX(pageWidth * -position);

                    // Scale the page down (between MIN_SCALE and 1)
                    float scaleFactor = MIN_SCALE
                            + (1 - MIN_SCALE) * (1 - Math.abs(position));
                    view.setScaleX(scaleFactor);
                    view.setScaleY(scaleFactor);

                } else { // (1,+Infinity]
                    // This page is way off-screen to the right.
                    view.setAlpha(0);
                }
            }
        });

        mTvTitleVideo.setScaleX(1.2f);
        mTvTitleVideo.setScaleY(1.2f);

        boolean serviceRunning = ServiceStatusUtils.isServiceRunning(getApplicationContext(), AudioPlayService.class.getName());
        int padding = UIUtils.dip2Px(6);
        mFabPlaying.setPadding(padding, padding, padding, padding);
        mFabPlayingDrawable = (AnimationDrawable) mFabPlaying.getDrawable();
        mFabPlayingDrawable.stop();
        mFabPlaying.setVisibility(serviceRunning ? View.VISIBLE : View.INVISIBLE);
    }

    @OnClick({R.id.tv_title_video, R.id.tv_title_music, R.id.fab_playing})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_title_video:
                mVpMain.setCurrentItem(0);
                break;
            case R.id.tv_title_music:
                mVpMain.setCurrentItem(1);
                break;
            case R.id.fab_playing:
                Intent intent = new Intent(this, AudioPlayActivity.class);
                intent.putExtra("cmd_notification", AudioPlayService.NOTIFICATION_OPEN_PLAY_ACTIVITY);
                startActivity(intent);
                break;
        }
    }

    class AudioPlayBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (mFabPlaying != null) {
                mFabPlaying.setVisibility(View.VISIBLE);
                mFabPlayingDrawable.start();
            }
        }
    }

    class AudioPauseBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (mFabPlaying != null) {
                mFabPlayingDrawable.stop();
            }
        }
    }

    @Override
    protected void onDestroy() {
        if (mAudioPauseBroadcastReceiver != null) {
            mLocalBroadcastManager.unregisterReceiver(mAudioPauseBroadcastReceiver);
            mAudioPauseBroadcastReceiver = null;
        }

        if (mAudioPlayBroadcastReceiver != null) {
            mLocalBroadcastManager.unregisterReceiver(mAudioPlayBroadcastReceiver);
            mAudioPlayBroadcastReceiver = null;
        }
        super.onDestroy();
    }
}
