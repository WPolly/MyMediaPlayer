package com.xiaoshan.mymediaplayer.bean;

import android.database.Cursor;
import android.provider.MediaStore;

import java.io.Serializable;

/**
 * Created by xiaoshan on 2016/3/14.
 * 14:50
 */
public class VideoItemInfo implements Serializable {
    public VideoItemInfo(Cursor cursor) {
        this.title = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.TITLE));
        this.duration = cursor.getLong(cursor.getColumnIndex(MediaStore.Video.Media.DURATION));
        this.size = cursor.getLong(cursor.getColumnIndex(MediaStore.Video.Media.SIZE));
        this.path = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATA));
    }

    public String title;
    public long duration;
    public long size;
    public String path;
}
