package com.xiaoshan.mymediaplayer.fragment;

import android.content.AsyncQueryHandler;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.xiaoshan.mymediaplayer.activity.AudioPlayActivity;
import com.xiaoshan.mymediaplayer.adapter.AudioListAdapter;
import com.xiaoshan.mymediaplayer.bean.AudioItemInfo;
import com.xiaoshan.mymediaplayer.factory.ListViewFactory;
import com.xiaoshan.mymediaplayer.utils.UIUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by xiaoshan on 2016/3/13.
 * 13:35
 */
public class MusicFragment extends Fragment {

    private ListView mListViewInstance;
    private List<AudioItemInfo> mMusicItemInfos;

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
                AudioListAdapter audioListAdapter = new AudioListAdapter(getActivity(), cursor, true);
                mListViewInstance.setAdapter(audioListAdapter);

                mMusicItemInfos = new ArrayList<>();
                if (cursor.getCount() > 0) {
                    cursor.moveToFirst();
                    do {
                        AudioItemInfo audioItemInfo = new AudioItemInfo(cursor);
                        mMusicItemInfos.add(audioItemInfo);
                    } while (cursor.moveToNext());
                }
            }
        };

        int token = 0;
        Object cookie = null;
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {MediaStore.Audio.Media._ID, MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.DATA};
        String selection = null;
        String[] selectionArgs = null;
        String orderBy = MediaStore.Audio.Media.TITLE + " ASC";//" ASC"切记不能忘写空格!!
        asyncQueryHandler.startQuery(token, cookie, uri, projection, selection, selectionArgs, orderBy);
    }

    private void initListener() {
        mListViewInstance.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getActivity(), AudioPlayActivity.class);
                intent.putExtra("audio-list", (Serializable) mMusicItemInfos);
                intent.putExtra("position", position);
                startActivity(intent);
            }
        });

        mListViewInstance.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("提示")
                        .setMessage("确定删除该文件吗?")
                        .setNegativeButton("否",null)
                        .setPositiveButton("是", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String path = mMusicItemInfos.get(position).path;
                        Toast.makeText(getActivity(), path, Toast.LENGTH_LONG).show();
                        boolean isDeleted = UIUtils.deleteFile(path);
                        if (isDeleted) {
                            Toast.makeText(getActivity(), "删除成功", Toast.LENGTH_SHORT).show();
                            MediaScannerConnection.scanFile(getActivity(), new String[] { Environment
                                    .getExternalStorageDirectory().getAbsolutePath() }, null, null);
                        } else {
                            Toast.makeText(getActivity(), "删除失败", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                        .show();
                return true;
            }
        });
    }
}
