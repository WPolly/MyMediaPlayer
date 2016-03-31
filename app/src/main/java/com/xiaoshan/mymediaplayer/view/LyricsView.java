package com.xiaoshan.mymediaplayer.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import com.xiaoshan.mymediaplayer.R;
import com.xiaoshan.mymediaplayer.bean.LyricBean;
import com.xiaoshan.mymediaplayer.utils.LyricsLoadHelper;
import com.xiaoshan.mymediaplayer.utils.UIUtils;

import java.util.List;

/**
 * Created by xiaoshan on 2016/3/26.
 * 15:51
 */
public class LyricsView extends View {

    private Paint mPaint;
    private int mDefaultColor;
    private int mHighlightColor;
    private int mCurrentPlayingPosition;
    private int mHighlightLyricIndex;
    private List<LyricBean> mLyricBeen;
    private int mHighlightTextX;
    private int mHighlightTextY;
    private float mDefaultTextSize;
    private float mHighlightTextSize;
    private int mDefaultTextHeight;

    public LyricsView(Context context) {
        this(context, null);
    }

    public LyricsView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mPaint = new Paint();
        mDefaultColor = Color.WHITE;
        mHighlightColor = UIUtils.getColor(R.color.colorAccent);
        mDefaultTextSize = UIUtils.dip2Px(13);
        mHighlightTextSize = UIUtils.dip2Px(15);
        mPaint.setColor(mDefaultColor);
        mPaint.setAntiAlias(true);
        mPaint.setTextSize(mDefaultTextSize);
        mPaint.setTextAlign(Paint.Align.CENTER);
    }

    @Override
    protected void onDraw(Canvas canvas) {

        mHighlightTextX = getWidth() / 2;
        mHighlightTextY = getHeight() / 2;

        if (mLyricBeen == null || mLyricBeen.isEmpty()) {
            mPaint.setColor(mHighlightColor);
            mPaint.setTextSize(mHighlightTextSize);
            canvas.drawText("木有找到歌词哟。。", mHighlightTextX, mHighlightTextY, mPaint);
            return;
        }

        if (mHighlightLyricIndex != mLyricBeen.size() - 1) {
            int highlightTime = mLyricBeen.get(mHighlightLyricIndex + 1).startShowTime - mLyricBeen.get(mHighlightLyricIndex).startShowTime;
            float speed = ((float) mDefaultTextHeight) / highlightTime;
            int showedTime = mCurrentPlayingPosition - mLyricBeen.get(mHighlightLyricIndex).startShowTime;
            canvas.translate(0, -showedTime * speed);
        }

        drawHighlightLyrics(canvas);//canvas移动,实际是绘画坐标的移动,一定要先移动坐标再重绘,否则没效果.
        drawNormalLyrics(canvas);
    }


    private void drawHighlightLyrics(Canvas canvas) {
        mPaint.setColor(mHighlightColor);
        mPaint.setTextSize(mHighlightTextSize);
        String lyricContent = mLyricBeen.get(mHighlightLyricIndex).lyricContent;
        canvas.drawText(lyricContent, mHighlightTextX, mHighlightTextY, mPaint);
    }

    private void drawNormalLyrics(Canvas canvas) {
        mPaint.setColor(mDefaultColor);
        mPaint.setTextSize(mDefaultTextSize);
        mDefaultTextHeight = UIUtils.dip2Px(16);
        for (int i = 0; i < mHighlightLyricIndex; i++) {
            String lyricContent = mLyricBeen.get(i).lyricContent;
            int Y = mHighlightTextY - (mHighlightLyricIndex - i) * mDefaultTextHeight;
            canvas.drawText(lyricContent, mHighlightTextX, Y, mPaint);
        }

        for (int i = mHighlightLyricIndex + 1; i < mLyricBeen.size(); i++) {
            String lyricContent = mLyricBeen.get(i).lyricContent;
            int Y = mHighlightTextY + (i - mHighlightLyricIndex) * mDefaultTextHeight;
            canvas.drawText(lyricContent, mHighlightTextX, Y, mPaint);
        }

    }

    public void setCurrentTime(int currentPlayingPosition) {
        this.mCurrentPlayingPosition = currentPlayingPosition;
        if (mLyricBeen == null || mLyricBeen.isEmpty()) {
            return;
        }
        setHighlightLyricIndex();
        invalidate();
    }

    private void setHighlightLyricIndex() {
        for (int i = 0; i < mLyricBeen.size(); i++) {
            if (mCurrentPlayingPosition >= mLyricBeen.get(i).startShowTime) {

                try {
                    if (mCurrentPlayingPosition < mLyricBeen.get(i + 1).startShowTime) {
                        mHighlightLyricIndex = i;
                        break;
                    }
                } catch (IndexOutOfBoundsException e) {
                    mHighlightLyricIndex = mLyricBeen.size() - 1;
                }
            } else {
                mHighlightLyricIndex = 0;
            }
        }
    }

    public void setAudioPath(String path) {
        String prefixAudioName = path.substring(0, path.lastIndexOf("."));
        String lrcPath = prefixAudioName + ".lrc";
        mLyricBeen = LyricsLoadHelper.parseLyrics(lrcPath);
        if (mLyricBeen == null || mLyricBeen.isEmpty()) {
            invalidate();
        }
    }

}
