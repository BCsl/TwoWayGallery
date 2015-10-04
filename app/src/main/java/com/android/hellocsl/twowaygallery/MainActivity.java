package com.android.hellocsl.twowaygallery;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

/**
 * Created by HelloCsl(cslgogogo@gmail.com) on 2015/10/4 0004.
 */
public class MainActivity extends Activity implements View.OnClickListener {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.btn_cover).setOnClickListener(this);
        findViewById(R.id.btn_pager).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_pager:
                startActivity(PagerActivity.newIntent(MainActivity.this));
                break;
            case R.id.btn_cover:
                startActivity(CoverActivity.newIntent(MainActivity.this));
                break;
            default:
                break;
        }
    }
}
