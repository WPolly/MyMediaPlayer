package com.xiaoshan.mymediaplayer;

import android.app.Application;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;

/**
 * Created by xiaoshan on 2016/2/15.
 * 12:54
 */
public class BaseApplication extends Application {

    private static Context mContext;
    private static Thread mMainThread;
    private static Looper mMainLooper;
    private static long mMainThreadId;
    private static Handler mHandler;

    @Override
    public void onCreate() {

        mContext = getApplicationContext();

        mMainThread = Thread.currentThread();

        mMainLooper = getMainLooper();

        mMainThreadId = mMainThread.getId();

        mHandler = new Handler();
        super.onCreate();
    }

    public static Context getContext() {
        return mContext;
    }

    public static Thread getMainThread() {
        return mMainThread;
    }

    public static Looper getAppMainLooper() {
        return mMainLooper;
    }

    public static long getMainThreadId() {
        return mMainThreadId;
    }

    public static Handler getHandler() {
        return mHandler;
    }
}
