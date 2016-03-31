package com.xiaoshan.mymediaplayer.fragment;

import android.content.AsyncQueryHandler;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.xiaoshan.mymediaplayer.activity.VideoPlayActivity;
import com.xiaoshan.mymediaplayer.adapter.VideoListAdapter;
import com.xiaoshan.mymediaplayer.bean.VideoItemInfo;
import com.xiaoshan.mymediaplayer.factory.ListViewFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by xiaoshan on 2016/3/13.
 * 13:35
 */
public class VideoFragment extends Fragment {

    private ListView mListViewInstance;
    private List<VideoItemInfo> mVideoItemInfos;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mListViewInstance = ListViewFactory.getListViewInstance();
        return mListViewInstance;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initData();
        initListener();
    }

    private void initData() {
        AsyncQueryHandler asyncQueryHandler = new AsyncQueryHandler(getActivity().getContentResolver()) {
            @Override
            protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
                VideoListAdapter videoListAdapter = new VideoListAdapter(getActivity(), cursor, false);
                mListViewInstance.setAdapter(videoListAdapter);

                mVideoItemInfos = new ArrayList<>();
                if (cursor.getCount() > 0) {
                    cursor.moveToFirst();
                    do {
                        VideoItemInfo videoItemInfo = new VideoItemInfo(cursor);
                        mVideoItemInfos.add(videoItemInfo);
                    } while (cursor.moveToNext());
                }
            }
        };

        int token = 0;
        Object cookie = null;
        Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {MediaStore.Video.Media._ID, MediaStore.Video.Media.TITLE,
                MediaStore.Video.Media.DURATION, MediaStore.Video.Media.SIZE, MediaStore.Video.Media.DATA};
        String selection = null;
        String[] selectionArgs = null;
        String orderBy = MediaStore.Video.Media.TITLE + " ASC";//" ASC"切记不能忘写空格!!
        asyncQueryHandler.startQuery(token, cookie, uri, projection, selection, selectionArgs, orderBy);
    }

    private void initListener() {
        mListViewInstance.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getActivity(), VideoPlayActivity.class);
                intent.putExtra("play_list", (Serializable) mVideoItemInfos);
                intent.putExtra("position", position);
                startActivity(intent);
            }
        });
    }
}
