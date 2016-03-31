package com.xiaoshan.mymediaplayer.factory;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.widget.ListView;

import com.xiaoshan.mymediaplayer.R;
import com.xiaoshan.mymediaplayer.utils.UIUtils;


/**
 * Created by xiaoshan on 2016/2/23.
 * 13:50
 */
public class ListViewFactory {

    public static ListView getListViewInstance() {
        ListView lv = new ListView(UIUtils.getContext());
        lv.setCacheColorHint(Color.TRANSPARENT);
        lv.setSelector(R.color.colorTransparent);
        lv.setOverScrollMode(View.OVER_SCROLL_NEVER);
        return lv;
    }
}
