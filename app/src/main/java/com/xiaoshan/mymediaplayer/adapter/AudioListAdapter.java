package com.xiaoshan.mymediaplayer.adapter;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.xiaoshan.mymediaplayer.R;
import com.xiaoshan.mymediaplayer.bean.AudioItemInfo;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by xiaoshan on 2016/3/13.
 * 13:41
 */
public class AudioListAdapter extends CursorAdapter {


    public AudioListAdapter(Context context, Cursor c, boolean autoRequery) {
        super(context, c, autoRequery);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View rootView = View.inflate(context, R.layout.item_audio_list, null);
        ViewHolder viewHolder = new ViewHolder(rootView);
        rootView.setTag(viewHolder);
        return rootView;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder viewHolder = (ViewHolder) view.getTag();
        AudioItemInfo audioItemInfo = new AudioItemInfo(cursor);
        viewHolder.mTvTitle.setText(audioItemInfo.title);
        viewHolder.mTvArtist.setText(audioItemInfo.artist);
    }


    static class ViewHolder {
        @InjectView(R.id.tv_title)
        TextView mTvTitle;
        @InjectView(R.id.tv_artist)
        TextView mTvArtist;

        ViewHolder(View view) {
            ButterKnife.inject(this, view);
        }
    }
}
