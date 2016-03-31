package com.xiaoshan.mymediaplayer.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;

/**
 * Created by xiaoshan on 2016/3/10.
 * 20:59
 */
public abstract class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(getLayoutResId());
        initView();
        initData();
        initListener();
    }

    protected abstract int getLayoutResId();

    protected abstract void initView();

    protected void initData() {

    }

    protected void initListener() {

    }
}
