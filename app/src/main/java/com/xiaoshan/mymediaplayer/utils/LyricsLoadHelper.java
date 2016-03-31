package com.xiaoshan.mymediaplayer.utils;

import com.xiaoshan.mymediaplayer.bean.LyricBean;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by xiaoshan on 2016/3/27.
 * 21:03
 */
public class LyricsLoadHelper {

    public static List<LyricBean> parseLyrics(String path) {

        try {
            List<LyricBean> lyricBeen = new ArrayList<>();
            File lyricsFile = new File(path);
            FileInputStream fis = new FileInputStream(lyricsFile);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fis, "utf-8"));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                if (line.contains(".")) {
                    LyricBean lyricBean = new LyricBean();
                    String minute = line.substring(1, 3);
                    String second = line.substring(4, 6);
                    String millisecond = line.substring(7, 9);
                    int minutes = Integer.valueOf(minute);
                    int seconds = Integer.valueOf(second);
                    int milliseconds = Integer.valueOf(millisecond);
                    lyricBean.startShowTime = minutes*60*1000 + seconds*1000 + milliseconds*10;
                    lyricBean.lyricContent = line.substring(10);
                    lyricBeen.add(lyricBean);
                }
            }
            return lyricBeen;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
