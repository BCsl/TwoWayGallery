package com.android.hellocsl.twowaygallery;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Gallery;
import android.widget.Toast;

import com.android.hellocsl.twowaygallery.adapter.InfoAdapter;
import com.android.hellocsl.twowaygallery.gallery.TwoWayAdapterView;
import com.android.hellocsl.twowaygallery.gallery.TwoWayGallery;

public class MainActivity extends Activity implements TwoWayAdapterView.OnItemSelectedListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);
        TwoWayGallery gallery = (TwoWayGallery) findViewById(R.id.gallery_horizontal);
        gallery.setAdapter(new InfoAdapter(this,true));
        gallery.setOnItemSelectedListener(this);
        TwoWayGallery galleryVertical = (TwoWayGallery) findViewById(R.id.gallery_vertical);
        galleryVertical.setAdapter(new InfoAdapter(this,false));
        gallery.setOnItemSelectedListener(this);
    }

    @Override
    public void onItemSelected(TwoWayAdapterView<?> parent, View view, int position, long id) {
        Toast.makeText(this,"onItemSelected:"+position,Toast.LENGTH_SHORT);
    }

    @Override
    public void onNothingSelected(TwoWayAdapterView<?> parent) {

    }
}
