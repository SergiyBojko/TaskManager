package com.serhiyboiko.taskmanager.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.serhiyboiko.taskmanager.R;

public class SplashScreenActivity extends AppCompatActivity {

    Handler mHandler;
    Runnable mRunnable;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen_activity);
        ImageView page1 = (ImageView) findViewById(R.id.page1);
        ImageView page2 = (ImageView) findViewById(R.id.page2);
        ImageView page3 = (ImageView) findViewById(R.id.page3);
        TextView appName = (TextView) findViewById(R.id.app_name);

        Animation page1Animation = AnimationUtils.loadAnimation(this, R.anim.splash_page_1);
        Animation page2Animation = AnimationUtils.loadAnimation(this, R.anim.splash_page_2);
        Animation page3Animation = AnimationUtils.loadAnimation(this, R.anim.splash_page_3);
        Animation appNameAnimation = AnimationUtils.loadAnimation(this, R.anim.splash_text);

        page3.startAnimation(page3Animation);
        page2.startAnimation(page2Animation);
        page1.startAnimation(page1Animation);
        appName.startAnimation(appNameAnimation);
        mHandler = new Handler();
        mRunnable = new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(SplashScreenActivity.this, TaskListActivity.class));
                SplashScreenActivity.this.finish();
                SplashScreenActivity.this.overridePendingTransition(0, R.anim.splash_activity_out);
            }
        };
        mHandler.postDelayed(mRunnable, 2000);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mHandler.removeCallbacks(mRunnable);
    }
}
