package com.xiaoshan.mymediaplayer.activity;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.drawable.AnimationDrawable;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.xiaoshan.mymediaplayer.R;
import com.xiaoshan.mymediaplayer.bean.AudioItemInfo;
import com.xiaoshan.mymediaplayer.service.AudioPlayService;
import com.xiaoshan.mymediaplayer.utils.StringUtils;
import com.xiaoshan.mymediaplayer.view.LyricView;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class AudioPlayActivity extends BaseActivity {


    public static final int USER_INTERFACE = 0;
    private static final int UPDATE_PLAY_TIME = 1;

    @InjectView(R.id.main_toolbar)
    Toolbar mAudioPlayToolbar;
    @InjectView(R.id.iv_vision)
    ImageView mIvVision;
    @InjectView(R.id.tv_artist)
    TextView mTvArtist;
//    @InjectView(R.id.custom_lyrics_view)
//    LyricsView mCustomLyricsView;
    @InjectView(R.id.custom_lyric_view)
    LyricView mCustomLyricView;
    @InjectView(R.id.tv_play_time)
    TextView mTvPlayTime;
    @InjectView(R.id.sb_audio)
    SeekBar mSbAudio;
    @InjectView(R.id.btn_play_mode)
    Button mBtnPlayMode;
    @InjectView(R.id.btn_pre)
    Button mBtnPre;
    @InjectView(R.id.btn_play)
    Button mBtnPlay;
    @InjectView(R.id.btn_next)
    Button mBtnNext;

    private ServiceConnection mConn;

    public AudioPlayService getAudioPlayService() {
        return mAudioPlayService;
    }

    private AudioPlayService mAudioPlayService;
    private Messenger mUIMessenger;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case AudioPlayService.MESSAGE_AUDIO_PLAY_SERVICE:
                    //拿到AudioPlayService的引用,与其handler;
                    mAudioPlayService = (AudioPlayService) msg.obj;
                    mAudioPlayService.openAudio();
                    break;

                case UPDATE_PLAY_TIME:
                    updatePlayTime();
                    break;
            }
        }
    };
    private int mDuration;
    private AnimationDrawable mVisionAnim;

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_audio_play;
    }

    @Override
    protected void initView() {
        ButterKnife.inject(this);
        initToolbar();
        mVisionAnim = (AnimationDrawable) mIvVision.getBackground();
    }

    @Override
    protected void initData() {
        mUIMessenger = new Messenger(mHandler);
        Intent intent = getIntent();
        intent.setClass(this, AudioPlayService.class);
        startService(intent);
        mConn = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                //拿到audioService的handler
                Messenger audioPlayServiceMessenger = new Messenger(service);
                Message uiMessage = Message.obtain();
                uiMessage.what = USER_INTERFACE;
                uiMessage.obj = AudioPlayActivity.this;
                uiMessage.replyTo = mUIMessenger;
                try {
                    //将AudioPlayActivity中的handler与Activity本身传到audioService的handler中
                    audioPlayServiceMessenger.send(uiMessage);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        };
        bindService(intent, mConn, BIND_AUTO_CREATE);
    }

    private void initToolbar() {
        setSupportActionBar(mAudioPlayToolbar);
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(true);
            supportActionBar.setTitle("音乐播放器");
        }
        mAudioPlayToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }


    @OnClick({R.id.btn_play_mode, R.id.btn_pre, R.id.btn_play, R.id.btn_next})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_play_mode:
                mAudioPlayService.changePlayMode();
                break;
            case R.id.btn_pre:
                mAudioPlayService.pre();
                break;
            case R.id.btn_play:
                playOrPause();
                break;
            case R.id.btn_next:
                mAudioPlayService.next();
                break;
        }
    }

    private void playOrPause() {
        if (mAudioPlayService.isPlaying()) {
            mAudioPlayService.pause();
        } else {
            mAudioPlayService.start();
        }
        updatePlayBtnState();
    }

    private void updatePlayBtnState() {
        if (mAudioPlayService.isPlaying()) {
            mBtnPlay.setBackgroundResource(R.drawable.selector_audio_btn_pause);
            mVisionAnim.start();
        } else {
            mBtnPlay.setBackgroundResource(R.drawable.selector_audio_btn_play);
            mVisionAnim.stop();
        }
    }

    public void updateUI(AudioItemInfo audioItemInfo) {
        mCustomLyricView.setAudioPath(audioItemInfo.path);
        updatePlayBtnState();
        mAudioPlayToolbar.setTitle(audioItemInfo.title);
        mTvArtist.setText(audioItemInfo.artist);
        mDuration = mAudioPlayService.getDuration();
        mSbAudio.setMax(mAudioPlayService.getDuration());
        mSbAudio.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mAudioPlayService.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        updatePlayTime();
    }

    @SuppressLint("SetTextI18n")
    private void updatePlayTime() {
        CharSequence duration = StringUtils.formatMillis(mDuration);
        int currentPlayingPosition = mAudioPlayService.getCurrentPlayingPosition();
        CharSequence currentPosition = StringUtils.formatMillis(currentPlayingPosition);
        mSbAudio.setProgress(currentPlayingPosition);
        mTvPlayTime.setText(currentPosition + "/" + duration);
        mCustomLyricView.setCurrentTime(currentPlayingPosition);
        mHandler.sendEmptyMessageDelayed(UPDATE_PLAY_TIME, 30);
    }

    public void updatePlayModeUI(int playMode) {
        switch (playMode) {
            case AudioPlayService.PLAY_MODE_ORDER:
                mBtnPlayMode.setBackgroundResource(R.drawable.selector_audio_btn_playmode_order);
                break;
            case AudioPlayService.PLAY_MODE_SINGLE:
                mBtnPlayMode.setBackgroundResource(R.drawable.selector_audio_btn_playmode_single);
                break;
            case AudioPlayService.PLAY_MODE_RANDOM:
                mBtnPlayMode.setBackgroundResource(R.drawable.selector_audio_btn_playmode_random);
                break;
        }
    }

    @Override
    protected void onDestroy() {
        unbindService(mConn);
        mHandler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }
}
