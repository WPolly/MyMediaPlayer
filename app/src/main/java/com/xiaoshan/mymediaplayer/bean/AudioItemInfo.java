package com.xiaoshan.mymediaplayer.bean;

import android.database.Cursor;
import android.provider.MediaStore;

import java.io.Serializable;

/**
 * Created by xiaoshan on 2016/3/14.
 * 14:50
 */
public class AudioItemInfo implements Serializable {
    public AudioItemInfo(Cursor cursor) {
        this.title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
        this.artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
        this.path = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
    }

    public String title;
    public String artist;
    public String path;
}
