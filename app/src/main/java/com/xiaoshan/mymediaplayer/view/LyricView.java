package com.xiaoshan.mymediaplayer.view;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.xiaoshan.mymediaplayer.activity.AudioPlayActivity;
import com.xiaoshan.mymediaplayer.bean.LyricBean;
import com.xiaoshan.mymediaplayer.utils.LyricsLoadHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lishan on 2017/9/20.
 * 11:07
 */

public class LyricView extends ViewGroup implements View.OnClickListener {

    private int mCurrentPlayingPosition;
    private int mHighlightLyricIndex;
    private List<LyricBean> mLyricBeen;

    public LyricView(Context context) {
        super(context);
    }

    public LyricView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        measureChildren(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int midHeight = 500;
        View subView = getChildAt(0);
        if (subView == null) return;
        int measuredWidth = subView.getMeasuredWidth();
        int measuredHeight = subView.getMeasuredHeight();
        int overT = midHeight;
        int belowT = midHeight;
        for (int i=0; i<getChildCount()-1; i++) {
            if (i<mHighlightLyricIndex) {
                overT -= measuredHeight;
                TextView tvOver = (TextView) getChildAt(mHighlightLyricIndex - i-1);
                tvOver.setTextColor(Color.WHITE);
                tvOver.setTextSize(14);
                tvOver.layout(l, overT, l+measuredWidth, overT+measuredHeight);
            } else if (i==mHighlightLyricIndex) {
                TextView highLightText = (TextView) getChildAt(mHighlightLyricIndex);
                highLightText.setTextColor(Color.GREEN);
                highLightText.setTextSize(16);
                highLightText.layout(l, midHeight, l+measuredWidth, midHeight+measuredHeight+8);
            } else {
                belowT += measuredHeight;
                TextView tvBelow = (TextView) getChildAt(i);
                tvBelow.setTextColor(Color.WHITE);
                tvBelow.setTextSize(14);
                tvBelow.layout(l, belowT, l+measuredWidth, belowT+measuredHeight);
            }
        }
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
            }
        }
    }

    private List<TextView> initLyricsTextView() {
        List<TextView> tvs = new ArrayList<>();
        for (final LyricBean lyricBean : mLyricBeen) {
            TextView tv = new TextView(getContext());
            tv.setWidth(getResources().getDisplayMetrics().widthPixels);
            tv.setText(lyricBean.lyricContent);
            tv.setTextSize(14);
            tv.setGravity(Gravity.CENTER);
            tv.setOnClickListener(this);
            tv.setTag(lyricBean);
            tvs.add(tv);
        }
        return  tvs;
    }

    public void setAudioPath(String path) {
        String prefixAudioName = path.substring(0, path.lastIndexOf("."));
        String lrcPath = prefixAudioName + ".lrc";
        mLyricBeen = LyricsLoadHelper.parseLyrics(lrcPath);
        List<TextView> textViews = initLyricsTextView();
        for (TextView textView : textViews) {
            addView(textView);
        }
    }

    public void setCurrentTime(int currentPlayingPosition) {
        this.mCurrentPlayingPosition = currentPlayingPosition;
        if (mLyricBeen == null || mLyricBeen.isEmpty()) {
            return;
        }
        setHighlightLyricIndex();
        requestLayout();
    }

    @Override
    public void onClick(View v) {
        AudioPlayActivity context = (AudioPlayActivity) getContext();
        LyricBean tag = (LyricBean) v.getTag();
        context.getAudioPlayService().seekTo(tag.startShowTime);
    }
}

