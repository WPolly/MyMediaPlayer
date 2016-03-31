package com.xiaoshan.mymediaplayer.activity;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.text.format.DateFormat;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.VideoView;

import com.apkfuns.logutils.LogUtils;
import com.xiaoshan.mymediaplayer.R;
import com.xiaoshan.mymediaplayer.bean.VideoItemInfo;
import com.xiaoshan.mymediaplayer.utils.StringUtils;
import com.xiaoshan.mymediaplayer.utils.UIUtils;

import java.io.Serializable;
import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;


public class VideoPlayActivity extends BaseActivity {

    @InjectView(R.id.vv_player)
    VideoView mVvPlayer;
    @InjectView(R.id.tv_title)
    TextView mTvTitle;
    @InjectView(R.id.iv_battery)
    ImageView mIvBattery;
    @InjectView(R.id.tv_system_time)
    TextView mTvSystemTime;
    @InjectView(R.id.bt_voice_toggle)
    Button mBtVoiceToggle;
    @InjectView(R.id.sb_volume)
    SeekBar mSbVolume;
    @InjectView(R.id.tv_current_position)
    TextView mTvCurrentPosition;
    @InjectView(R.id.sb_video)
    SeekBar mSbVideo;
    @InjectView(R.id.tv_duration)
    TextView mTvDuration;
    @InjectView(R.id.btn_exit)
    Button mBtnExit;
    @InjectView(R.id.btn_pre)
    Button mBtnPre;
    @InjectView(R.id.btn_play)
    Button mBtnPlay;
    @InjectView(R.id.btn_next)
    Button mBtnNext;
    @InjectView(R.id.btn_fullscreen)
    Button mBtnFullscreen;
    @InjectView(R.id.ll_bottom_ctrl)
    LinearLayout mLlBottomCtrl;
    @InjectView(R.id.ll_top_ctrl)
    LinearLayout mLlTopCtrl;
    @InjectView(R.id.view_brightness)
    View mViewBrightness;
    @InjectView(R.id.ll_loading)
    LinearLayout mLlLoading;

    private BroadcastReceiver mBatteryStateReceiver;
    private int mPosition;
    private ArrayList<VideoItemInfo> mVideoList;

    private static final int UPDATE_TIME = 0;
    private static final int UPDATE_CURRENT_POSITION = 1;
    private static final int HIDE_CTRL_LAYOUT = 2;

    private AudioManager mAudioManager;

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATE_TIME:
                    updateCurrentTime();
                    break;
                case UPDATE_CURRENT_POSITION:
                    updateCurrentPosition();
                    break;
                case HIDE_CTRL_LAYOUT:
                    if (isCtrlLayoutShowing) {
                        toggleShowCtrlLayout();
                    }
                    break;
            }
        }
    };
    private int mCurrentVolume;
    private int mMaxVolume;
    private int mSavedVolume;
    private int mMaxLightness;
    private int mCurrentLightness;
    private GestureDetector mGestureDetector;
    private float mVolumeScreenHeightRatio;
    private float mLightnessScreenHeightRatio;
    private GestureDetector.SimpleOnGestureListener mGestureListener;
    private boolean isFullScreen;
    private int mDefaultWidth;
    private int mDefaultHeight;
    private int mLlTopCtrlMeasuredHeight;
    private int mLlBottomCtrlMeasuredHeight;
    private boolean isCtrlLayoutShowing;

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_video_play;
    }

    @Override
    protected void initView() {
        ButterKnife.inject(this);
        init();

        mLlTopCtrl.measure(0,0);
        mLlTopCtrlMeasuredHeight = mLlTopCtrl.getMeasuredHeight();
        mLlTopCtrl.setTranslationY(-mLlTopCtrlMeasuredHeight);
        mLlBottomCtrl.measure(0,0);
        mLlBottomCtrlMeasuredHeight = mLlBottomCtrl.getMeasuredHeight();
        mLlBottomCtrl.setTranslationY(mLlBottomCtrlMeasuredHeight);
    }

    @SuppressWarnings("unchecked")
    private void init() {
        Intent intent = getIntent();
        Uri data = intent.getData();
        if (data != null) {
            mVvPlayer.setVideoURI(data);
            LogUtils.wtf(data);
            String path = data.getPath();
            mTvTitle.setText(path);
            mBtnPre.setEnabled(false);
            mBtnNext.setEnabled(false);
        } else {
            Serializable videoListSerializable = intent.getSerializableExtra("play_list");
            mPosition = intent.getIntExtra("position", -1);
            if (mPosition != -1 && videoListSerializable != null) {
                mVideoList = (ArrayList<VideoItemInfo>) videoListSerializable;
            }
            refreshBtnPreAndNextEnable();
            prepareToPlay();
        }
    }

    @Override
    protected void initData() {
        mVvPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mVvPlayer.start();
                ViewGroup.LayoutParams layoutParams = mVvPlayer.getLayoutParams();
                mDefaultWidth = layoutParams.width;
                mDefaultHeight = layoutParams.height;
                setVideoTitle();
                refreshBtnPlayBg();
                final int duration = mVvPlayer.getDuration();
                mSbVideo.setMax(duration);
                mTvDuration.setText(StringUtils.formatMillis(duration));
                updateCurrentPosition();
                mp.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {
                    @Override
                    public void onBufferingUpdate(MediaPlayer mp, int percent) {
                        int secondProgress = duration * percent / 100;
                        mSbVideo.setSecondaryProgress(secondProgress);
                    }
                });
            }
        });

        mVvPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mVvPlayer.seekTo(0);
                refreshBtnPlayBg();
                mSbVideo.setProgress(0);
            }
        });


        registerBatteryReceiver();
        initSbVolume();

        mMaxLightness = 255;
        mCurrentLightness = UIUtils.getScreenBrightness(this);
        mLightnessScreenHeightRatio = ((float) mMaxLightness) / UIUtils.getScreenHeight(VideoPlayActivity.this);
        mVolumeScreenHeightRatio = ((float) mMaxVolume) / UIUtils.getScreenHeight(VideoPlayActivity.this);
        mGestureListener = new GestureDetector.SimpleOnGestureListener() {

            private int tempCurrentVolume;
            private int tempCurrentLightness;
            private boolean isLeft;
            private boolean isRight;

            @Override
            public void onLongPress(MotionEvent e) {
                playOrPause();
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                float movedY = e1.getY() - e2.getY();
                if (isRight) {
                    changeVolumeViaGesture(movedY);
                } else if (isLeft) {
                    changeLightnessViaGesture(movedY);
                }
                return true;
            }

            private void changeVolumeViaGesture(float movedY) {
                int changedVolume = (int) (movedY * mVolumeScreenHeightRatio);
                int result = tempCurrentVolume + changedVolume;
                if (result >= mMaxVolume) {
                    result = mMaxVolume;
                } else if (result <= 0) {
                    result = 0;
                }
                setStreamVolume(result);
            }

            private void changeLightnessViaGesture(float movedY) {
                int changedLightness = (int) (movedY * mLightnessScreenHeightRatio);
                int result = tempCurrentLightness + changedLightness;
                if (result >= mMaxLightness) {
                    result = mMaxLightness;
                } else if (result <= 0) {
                    result = 0;
                }
                setLightness(result);
            }

            @Override
            public boolean onDown(MotionEvent e) {
                isLeft = e.getX() < UIUtils.getScreenWidth(VideoPlayActivity.this) / 3;
                isRight = e.getX() > 2 * UIUtils.getScreenWidth(VideoPlayActivity.this) / 3;
                tempCurrentVolume = mCurrentVolume;
                tempCurrentLightness = mCurrentLightness;
                return super.onDown(e);
            }

            @Override
            public boolean onDoubleTap(MotionEvent e) {
                toggleFullScreen();
                updateBtnFullScreenBg();
                return true;
            }

            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                toggleShowCtrlLayout();
                return true;
            }
        };
        mSbVideo.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mVvPlayer.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                cancelHideCtrlLayout();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                autoHideCtrlLayout();
            }
        });
    }

    private void prepareToPlay() {
        String path = mVideoList.get(mPosition).path;
        mVvPlayer.setVideoPath(path);
    }

    @Override
    protected void initListener() {
        mGestureDetector = new GestureDetector(this, mGestureListener);
    }

    private void setLightness(int result) {
        UIUtils.setWindowBrightness(this, result);
        mCurrentLightness = result;
    }

    private void initSbVolume() {
        mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        mMaxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        mCurrentVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        mSbVolume.setMax(mMaxVolume);
        mSbVolume.setProgress(mCurrentVolume);
        mSbVolume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    setStreamVolume(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                cancelHideCtrlLayout();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                autoHideCtrlLayout();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        updateCurrentTime();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mGestureDetector.onTouchEvent(event);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                cancelHideCtrlLayout();
                break;

            case MotionEvent.ACTION_UP:
                autoHideCtrlLayout();
                break;
        }
        return super.onTouchEvent(event);
    }

    @OnClick({R.id.vv_player, R.id.bt_voice_toggle, R.id.btn_exit,
            R.id.btn_pre, R.id.btn_play, R.id.btn_next, R.id.btn_fullscreen})
    public void onClick(View view) {
        cancelHideCtrlLayout();
        switch (view.getId()) {
            case R.id.vv_player:
                break;
            case R.id.bt_voice_toggle:
                toggleMute();
                break;
            case R.id.btn_exit:
                finish();
                break;
            case R.id.btn_pre:
                mPosition -- ;
                refreshBtnPreAndNextEnable();
                prepareToPlay();
                break;
            case R.id.btn_play:
                playOrPause();
                break;
            case R.id.btn_next:
                mPosition++;
                refreshBtnPreAndNextEnable();
                prepareToPlay();
                break;
            case R.id.btn_fullscreen:
                toggleFullScreen();
                updateBtnFullScreenBg();
                break;
        }
        autoHideCtrlLayout();
    }

    private void toggleShowCtrlLayout() {
        int duration = 150;
        if (isCtrlLayoutShowing) {
            mLlTopCtrl.animate().translationY(-mLlTopCtrlMeasuredHeight).setDuration(duration);
            mLlBottomCtrl.animate().translationY(mLlBottomCtrlMeasuredHeight).setDuration(duration);
        } else {
            mLlTopCtrl.animate().translationY(0).setDuration(duration);
            mLlBottomCtrl.animate().translationY(0).setDuration(duration);
            autoHideCtrlLayout();
        }
        isCtrlLayoutShowing = !isCtrlLayoutShowing;
    }

    private void toggleFullScreen() {
        ViewGroup.LayoutParams layoutParams = mVvPlayer.getLayoutParams();
        if (isFullScreen) {
            layoutParams.width = mDefaultWidth;
            layoutParams.height = mDefaultHeight;
        } else {
            layoutParams.width = UIUtils.getScreenWidth(this);
            layoutParams.height = UIUtils.getScreenHeight(this);
        }
        mVvPlayer.requestLayout();
        isFullScreen = !isFullScreen;
    }

    private void updateBtnFullScreenBg() {
        if (isFullScreen) {
            mBtnFullscreen.setBackgroundResource(R.drawable.selector_btn_defaultscreen);
        } else {
            mBtnFullscreen.setBackgroundResource(R.drawable.selector_btn_fullscreen);
        }
    }


    private void refreshBtnPreAndNextEnable() {
        mBtnPre.setEnabled(mPosition > 0);
        mBtnNext.setEnabled(mPosition < mVideoList.size() -1);
    }

    private void playOrPause() {
        if (mVvPlayer.isPlaying()) {
            mVvPlayer.pause();
        } else {
            mVvPlayer.start();
        }
        refreshBtnPlayBg();
    }

    private void refreshBtnPlayBg() {
        if (mVvPlayer.isPlaying()) {
            mBtnPlay.setBackgroundResource(R.drawable.selector_btn_pause);
        } else {
            mBtnPlay.setBackgroundResource(R.drawable.selector_btn_play);
        }
    }


    private void setStreamVolume(int progress) {
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, AudioManager.ADJUST_SAME);
        mCurrentVolume = progress;
        mSbVolume.setProgress(mCurrentVolume);
    }

    private void updateCurrentTime() {
        long timeInMillis = System.currentTimeMillis();
        CharSequence currentTime = DateFormat.format("HH:mm:ss", timeInMillis);
        mTvSystemTime.setText(currentTime);
        mHandler.sendEmptyMessageDelayed(UPDATE_TIME, 1000);
    }

    private void updateCurrentPosition() {
        int currentPosition = mVvPlayer.getCurrentPosition();
        CharSequence charSequence = StringUtils.formatMillis(currentPosition);
        mTvCurrentPosition.setText(charSequence);
        mSbVideo.setProgress(currentPosition);
        mHandler.sendEmptyMessageDelayed(UPDATE_CURRENT_POSITION, 300);
    }

    private void autoHideCtrlLayout() {
        mHandler.removeMessages(HIDE_CTRL_LAYOUT);
        mHandler.sendEmptyMessageDelayed(HIDE_CTRL_LAYOUT, 5500);
    }

    private void cancelHideCtrlLayout() {
        mHandler.removeMessages(HIDE_CTRL_LAYOUT);
    }

    private void setVideoTitle() {
        if (mVideoList != null) {
            String title = mVideoList.get(mPosition).title;
            mTvTitle.setText(title);
        }
    }

    private void registerBatteryReceiver() {
        mBatteryStateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                updateBatteryState(intent);
            }
        };

        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(mBatteryStateReceiver, intentFilter);
    }

    private void updateBatteryState(Intent intent) {
        int level = intent.getIntExtra("level", 0);
        int resId;
        if (level == 0) {
            resId = R.mipmap.ic_battery_0;
        } else if (level <= 10) {
            resId = R.mipmap.ic_battery_10;
        } else if (level <= 20) {
            resId = R.mipmap.ic_battery_20;
        } else if (level <= 40) {
            resId = R.mipmap.ic_battery_40;
        } else if (level <= 60) {
            resId = R.mipmap.ic_battery_60;
        } else if (level <= 80) {
            resId = R.mipmap.ic_battery_80;
        } else {
            resId = R.mipmap.ic_battery_100;
        }
        mIvBattery.setBackgroundResource(resId);
    }

    private void toggleMute() {
        if (mCurrentVolume > 0) {
            mSavedVolume = mCurrentVolume;
            setStreamVolume(0);
        } else {
            setStreamVolume(mSavedVolume);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        mHandler.removeMessages(UPDATE_TIME);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mBatteryStateReceiver != null) {
            unregisterReceiver(mBatteryStateReceiver);
            mBatteryStateReceiver = null;
        }
        mHandler.removeMessages(UPDATE_CURRENT_POSITION);
    }

}
