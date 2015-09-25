package com.android.hellocsl.twowaygallery;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Gallery;

import com.android.hellocsl.twowaygallery.adapter.InfoAdapter;
import com.android.hellocsl.twowaygallery.gallery.TwoWayGallery;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);
        TwoWayGallery gallery = (TwoWayGallery) findViewById(R.id.gallery_horizontal);
        gallery.setAdapter(new InfoAdapter(this,true));
        TwoWayGallery galleryVertical = (TwoWayGallery) findViewById(R.id.gallery_vertical);
        galleryVertical.setAdapter(new InfoAdapter(this,false));
    }

}
