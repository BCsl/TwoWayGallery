package com.android.hellocsl.twowaygallery;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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
    private InfoAdapter mHadapter, mVadapter;

    public static Intent newIntent(Context con) {
        return new Intent(con, CoverActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);
        TwoWayGallery gallery = (TwoWayGallery) findViewById(R.id.gallery_horizontal);
        mHadapter = new InfoAdapter(this, true);
        gallery.setAdapter(mHadapter);
        gallery.setOnItemSelectedListener(this);
        mGalleryVertical = (TwoWayGallery) findViewById(R.id.gallery_vertical);
        mTvHorizontal = (TextView) findViewById(R.id.tv_horizontal_info);
        mTvVertical = (TextView) findViewById(R.id.tv_vertical_info);
        mVadapter = new InfoAdapter(this, false);
        mGalleryVertical.setAdapter(mVadapter);
        gallery.setOnItemSelectedListener(this);
        gallery.setOnItemClickListener(this);
        mGalleryVertical.setOnItemSelectedListener(this);
        mGalleryVertical.setOnItemClickListener(this);
        mTvHorizontal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mHadapter.notifyDataSetChanged();
            }
        });
        mTvVertical.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mVadapter.notifyDataSetChanged();
            }
        });
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
