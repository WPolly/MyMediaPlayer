package com.xiaoshan.mymediaplayer.bean;

/**
 * Created by xiaoshan on 2016/3/26.
 * 12:10
 */
public class LyricBean {

    public int startShowTime;
    public String lyricContent;

    @Override
    public String toString() {
        return "LyricBean{" +
                "startShowTime=" + startShowTime +
                ", lyricContent='" + lyricContent + '\'' +
                '}';
    }
}
