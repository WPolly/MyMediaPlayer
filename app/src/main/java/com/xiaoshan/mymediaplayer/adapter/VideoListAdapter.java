package com.xiaoshan.mymediaplayer.adapter;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.xiaoshan.mymediaplayer.R;
import com.xiaoshan.mymediaplayer.bean.VideoItemInfo;
import com.xiaoshan.mymediaplayer.utils.StringUtils;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by xiaoshan on 2016/3/13.
 * 13:41
 */
public class VideoListAdapter extends CursorAdapter {


    public VideoListAdapter(Context context, Cursor c, boolean autoRequery) {
        super(context, c, autoRequery);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View rootView = View.inflate(context, R.layout.item_video_list, null);
        ViewHolder viewHolder = new ViewHolder(rootView);
        rootView.setTag(viewHolder);
        return rootView;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder viewHolder = (ViewHolder) view.getTag();
        VideoItemInfo videoItemInfo = new VideoItemInfo(cursor);
        viewHolder.mTvTitle.setText(videoItemInfo.title);
        viewHolder.mTvDuration.setText(StringUtils.formatMillis(videoItemInfo.duration));
        viewHolder.mTvSize.setText(StringUtils.formatFileSize(videoItemInfo.size));
    }

    static class ViewHolder {
        @InjectView(R.id.tv_title)
        TextView mTvTitle;
        @InjectView(R.id.tv_size)
        TextView mTvSize;
        @InjectView(R.id.tv_duration)
        TextView mTvDuration;

        ViewHolder(View view) {
            ButterKnife.inject(this, view);
        }
    }
}
