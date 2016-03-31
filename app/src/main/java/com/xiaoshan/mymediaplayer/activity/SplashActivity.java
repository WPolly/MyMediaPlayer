package com.xiaoshan.mymediaplayer.activity;

import android.content.Intent;

import com.xiaoshan.mymediaplayer.R;
import com.xiaoshan.mymediaplayer.utils.UIUtils;

public class SplashActivity extends BaseActivity {


    @Override
    protected int getLayoutResId() {
        return R.layout.activity_splash;
    }

    @Override
    protected void initView() {

    }

    @Override
    protected void initData() {
        UIUtils.getMainThreadHandler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        }, 1000);
    }
}
