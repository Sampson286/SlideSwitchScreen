package com.zyc.slideswitchscreen;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

public class MainActivity extends AppCompatActivity {
    private ChageScreenFrameLayout chageScreenFrameLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        chageScreenFrameLayout=(ChageScreenFrameLayout)findViewById(R.id.chageScreenFrameLayout);
        View i=new ImageView(this);
        i.setBackgroundResource(R.mipmap.guide_1);
        i.setEnabled(true);
        i.setClickable(true);
        View i2=new ImageView(this);
        i2.setEnabled(true);
        i2.setClickable(true);
        i2.setBackgroundResource(R.mipmap.guide_2);
        chageScreenFrameLayout.addView(i);
        chageScreenFrameLayout.addView(i2);
    }
}
