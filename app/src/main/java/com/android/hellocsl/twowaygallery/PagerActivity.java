package com.android.hellocsl.twowaygallery;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.android.hellocsl.twowaygallery.adapter.ImageAdapter;
import com.hellocsl.twowaygallery.TwoWayGallery;

/**
 * Created by HelloCsl(cslgogogo@gmail.com) on 2015/10/4 0004.
 */
public class PagerActivity extends Activity {

    public static Intent newIntent(Context con) {
        return new Intent(con, PagerActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cycle_pager);
        ((TwoWayGallery) findViewById(R.id.cycle_pager)).setAdapter(new ImageAdapter(this));
        ((TwoWayGallery) findViewById(R.id.vertical_cycle_pager)).setAdapter(new ImageAdapter(this));
    }
}
