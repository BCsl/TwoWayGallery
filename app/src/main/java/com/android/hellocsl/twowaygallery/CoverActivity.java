package com.android.hellocsl.twowaygallery;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.hellocsl.twowaygallery.adapter.InfoAdapter;
import com.hellocsl.twowaygallery.TwoWayAdapterView;
import com.hellocsl.twowaygallery.TwoWayGallery;


public class CoverActivity extends Activity implements TwoWayAdapterView.OnItemSelectedListener, TwoWayAdapterView.OnItemClickListener {
    private TextView mTvVertical, mTvHorizontal;
    private TwoWayGallery mGalleryVertical;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);
        TwoWayGallery gallery = (TwoWayGallery) findViewById(R.id.gallery_horizontal);
        gallery.setAdapter(new InfoAdapter(this, true));
        gallery.setOnItemSelectedListener(this);
        mGalleryVertical = (TwoWayGallery) findViewById(R.id.gallery_vertical);
        mTvHorizontal = (TextView) findViewById(R.id.tv_horizontal_info);
        mTvVertical = (TextView) findViewById(R.id.tv_vertical_info);
        mGalleryVertical.setAdapter(new InfoAdapter(this, false));
        gallery.setOnItemSelectedListener(this);
        gallery.setOnItemClickListener(this);
        mGalleryVertical.setOnItemSelectedListener(this);
        mGalleryVertical.setOnItemClickListener(this);
    }

    @Override
    public void onItemSelected(TwoWayAdapterView<?> parent, View view, int position, long id) {
        if (parent == mGalleryVertical) {
            mTvVertical.setText("selected position:" + position);
        } else {
            mTvHorizontal.setText("selected position:" + position);
        }
    }

    @Override
    public void onNothingSelected(TwoWayAdapterView<?> parent) {

    }

    @Override
    public void onItemClick(TwoWayAdapterView<?> parent, View view, int position, long id) {
        Toast.makeText(this, "onItemClick:" + position, Toast.LENGTH_SHORT).show();
    }
}
