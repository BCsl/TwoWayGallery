package com.android.hellocsl.twowaygallery;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.android.hellocsl.twowaygallery.adapter.InfoAdapter;
import com.hellocsl.twowaygallery.TwoWayAdapterView;
import com.hellocsl.twowaygallery.TwoWayGallery;


public class MainActivity extends Activity implements TwoWayAdapterView.OnItemSelectedListener, TwoWayAdapterView.OnItemClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);
        TwoWayGallery gallery = (TwoWayGallery) findViewById(R.id.gallery_horizontal);
        gallery.setAdapter(new InfoAdapter(this, true));
        gallery.setOnItemSelectedListener(this);
        TwoWayGallery galleryVertical = (TwoWayGallery) findViewById(R.id.gallery_vertical);
        galleryVertical.setAdapter(new InfoAdapter(this, false));
        gallery.setOnItemSelectedListener(this);
        gallery.setOnItemClickListener(this);
    }

    @Override
    public void onItemSelected(TwoWayAdapterView<?> parent, View view, int position, long id) {
//        Toast.makeText(this, "onItemSelected:" + position, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onNothingSelected(TwoWayAdapterView<?> parent) {

    }

    @Override
    public void onItemClick(TwoWayAdapterView<?> parent, View view, int position, long id) {
        Toast.makeText(this, "onItemClick:" + position, Toast.LENGTH_SHORT).show();
    }
}
