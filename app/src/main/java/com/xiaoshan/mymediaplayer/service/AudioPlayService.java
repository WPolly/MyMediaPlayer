package com.xiaoshan.mymediaplayer.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.RemoteViews;

import com.xiaoshan.mymediaplayer.R;
import com.xiaoshan.mymediaplayer.activity.AudioPlayActivity;
import com.xiaoshan.mymediaplayer.bean.AudioItemInfo;

import java.io.IOException;
import java.util.List;
import java.util.Random;

public class AudioPlayService extends Service {

    public static final int MESSAGE_AUDIO_PLAY_SERVICE = 0;
    public static final int PLAY_MODE_ORDER = 1;
    public static final int PLAY_MODE_SINGLE = 2;
    public static final int PLAY_MODE_RANDOM = 3;
    public static final int NOTIFICATION_OPEN_PLAY_ACTIVITY = 4;
    private static final int NOTIFICATION_PRE = 5;
    private static final int NOTIFICATION_NEXT = 6;

    private Messenger audioPlayServiceMessenger;
    private MediaPlayer mMediaPlayer;
    private List<AudioItemInfo> mAudioItemInfos;

    private int mCurrentPosition = -2;
    private int mCurrentPlayMode;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            //拿到AudioPlayActivity和其handler.
            switch (msg.what) {
                case AudioPlayActivity.USER_INTERFACE:
                    mAudioPlayActivity = (AudioPlayActivity) msg.obj;
                    Messenger uiMessenger = msg.replyTo;
                    Message audioMessage = Message.obtain();
                    audioMessage.obj = AudioPlayService.this;
                    audioMessage.what = MESSAGE_AUDIO_PLAY_SERVICE;
                    try {
                        //用Activity中的handler发送包含AudioPlayService和其handler的消息.
                        uiMessenger.send(audioMessage);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    break;
            }
        }
    };
    private AudioPlayActivity mAudioPlayActivity;
    private AudioItemInfo mCurrentAudioItemInfo;
    private SharedPreferences mSp;
    private boolean isSameToCurrent;
    private NotificationManager mNotificationManager;
    private int mNotificationId;
    private LocalBroadcastManager mLocalBroadcastManager;


    public AudioPlayService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        audioPlayServiceMessenger = new Messenger(mHandler);

        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mNotificationId = 1;

        mLocalBroadcastManager = LocalBroadcastManager.getInstance(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int cmdNotification = intent.getIntExtra("cmd_notification", -1);
        switch (cmdNotification) {
            case NOTIFICATION_OPEN_PLAY_ACTIVITY:
                isSameToCurrent = true;
                break;
            case NOTIFICATION_PRE:
                pre();
                break;
            case NOTIFICATION_NEXT:
                next();
                break;
            default:
                mAudioItemInfos = (List<AudioItemInfo>) intent.getSerializableExtra("audio-list");
                int position = intent.getIntExtra("position", -1);
                if (position == mCurrentPosition) {
                    isSameToCurrent = true;
                } else {
                    isSameToCurrent = false;
                    mCurrentPosition = position;
                }
        }
        mSp = PreferenceManager.getDefaultSharedPreferences(this);
        mCurrentPlayMode = mSp.getInt("play_mode", PLAY_MODE_ORDER);
        return super.onStartCommand(intent, flags, startId);
    }

    public void openAudio() {
        if (mAudioItemInfos == null || mAudioItemInfos.isEmpty() || mCurrentPosition == -1) {
            return;
        }
        if (isSameToCurrent) {
            mAudioPlayActivity.updatePlayModeUI(mCurrentPlayMode);
            mAudioPlayActivity.updateUI(mCurrentAudioItemInfo);
            isSameToCurrent = false;
            return;
        }


        Intent i = new Intent("com.android.music.musicservicecommand");
        i.putExtra("command", "pause");
        sendBroadcast(i);

        release();

        mMediaPlayer = new MediaPlayer();
        try {
            mCurrentAudioItemInfo = mAudioItemInfos.get(mCurrentPosition);
            mMediaPlayer.setDataSource(mCurrentAudioItemInfo.path);
            mMediaPlayer.prepareAsync();
            mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    start();
                    mAudioPlayActivity.updatePlayModeUI(mCurrentPlayMode);
                    mAudioPlayActivity.updateUI(mCurrentAudioItemInfo);
                }
            });
            mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    next();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void release() {
        if (mMediaPlayer != null) {
            mMediaPlayer.reset();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return audioPlayServiceMessenger.getBinder();
    }

    public boolean isPlaying() {
        return mMediaPlayer != null && mMediaPlayer.isPlaying();
    }

    public void start() {
        if (mMediaPlayer != null) {
            mMediaPlayer.start();
            sendNotification();

            Intent intent = new Intent("com.xiaoshan.mymediaplayer.play");
            mLocalBroadcastManager.sendBroadcast(intent);
        }
    }

    public void pause() {
        if (mMediaPlayer != null) {
            mMediaPlayer.pause();
            mNotificationManager.cancel(mNotificationId);

            Intent intent = new Intent("com.xiaoshan.mymediaplayer.pause");
            mLocalBroadcastManager.sendBroadcast(intent);
        }
    }

    public int getDuration() {
        if (mMediaPlayer != null) {
            return mMediaPlayer.getDuration();
        }
        return 0;
    }

    public int getCurrentPlayingPosition() {
        if (mMediaPlayer != null) {
            return mMediaPlayer.getCurrentPosition();
        }
        return 0;
    }

    public void seekTo(int position) {
        if (mMediaPlayer != null) {
            mMediaPlayer.seekTo(position);
        }
    }

    public void pre() {
        switch (mCurrentPlayMode) {
            case PLAY_MODE_ORDER:
                mCurrentPosition = (mCurrentPosition == 0 ? mAudioItemInfos.size() - 1 : mCurrentPosition - 1);
                break;
            case PLAY_MODE_SINGLE:
                break;
            case PLAY_MODE_RANDOM:
                Random random = new Random();
                mCurrentPosition = random.nextInt(mAudioItemInfos.size());
                break;
        }
        openAudio();
    }

    public void next() {
        switch (mCurrentPlayMode) {
            case PLAY_MODE_ORDER:
                mCurrentPosition = (mCurrentPosition == mAudioItemInfos.size() - 1 ? 0 : mCurrentPosition + 1);
                break;
            case PLAY_MODE_SINGLE:
                break;
            case PLAY_MODE_RANDOM:
                Random random = new Random();
                mCurrentPosition = random.nextInt(mAudioItemInfos.size());
                break;
        }
        openAudio();
    }

    public void changePlayMode() {
        switch (mCurrentPlayMode) {
            case PLAY_MODE_ORDER:
                mCurrentPlayMode = PLAY_MODE_SINGLE;
                break;
            case PLAY_MODE_SINGLE:
                mCurrentPlayMode = PLAY_MODE_RANDOM;
                break;
            case PLAY_MODE_RANDOM:
                mCurrentPlayMode = PLAY_MODE_ORDER;
                break;
        }
        mAudioPlayActivity.updatePlayModeUI(mCurrentPlayMode);
        mSp.edit().putInt("play_mode", mCurrentPlayMode).apply();
    }


    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void sendNotification() {
        Notification.Builder builder = new Notification.Builder(this);
        RemoteViews remoteViews = getRemoteViews();
        PendingIntent pendingIntent = getActivityPendingIntent(NOTIFICATION_OPEN_PLAY_ACTIVITY);
        Notification notification = builder.setTicker("正在播放 " + mCurrentAudioItemInfo.title)
                .setSmallIcon(R.mipmap.icon_notification)
                .setOngoing(true)
                .setContent(remoteViews)
                .setContentIntent(pendingIntent)
                .build();
        mNotificationManager.cancel(mNotificationId);
        mNotificationManager.notify(mNotificationId,notification);
    }

    private RemoteViews getRemoteViews() {
        RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.notification_audio_playing);
        remoteViews.setTextViewText(R.id.tv_notification_title, mCurrentAudioItemInfo.title);
        remoteViews.setTextViewText(R.id.tv_notification_artist, mCurrentAudioItemInfo.artist);
        remoteViews.setOnClickPendingIntent(R.id.btn_notification_pre, getServicePendingIntent(NOTIFICATION_PRE));
        remoteViews.setOnClickPendingIntent(R.id.btn_notification_next, getServicePendingIntent(NOTIFICATION_NEXT));
        return remoteViews;
    }

    private PendingIntent getActivityPendingIntent(int cmd) {
        Intent intent = new Intent(this, AudioPlayActivity.class);
        intent.putExtra("cmd_notification", cmd);
        return PendingIntent.getActivity(this, cmd, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private PendingIntent getServicePendingIntent(int cmd) {
        Intent intent = new Intent(this, AudioPlayService.class);
        intent.putExtra("cmd_notification", cmd);
        return PendingIntent.getService(this, cmd, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }
}
