package com.android.hellocsl.twowaygallery.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.hellocsl.twowaygallery.R;

import java.util.HashMap;

/**
 * Created by HelloCsl(cslgogogo@gmail.com) on 2015/10/4 0004.
 */
public class ImageAdapter extends BaseAdapter {
    private HashMap<Integer, Integer> mResMap = new HashMap<Integer, Integer>() {
        {
            put(0, R.drawable.photo1);
            put(1, R.drawable.photo2);
            put(2, R.drawable.photo3);
            put(3, R.drawable.photo4);
        }
    };

    private final LayoutInflater mInflater;

    public ImageAdapter(Context context) {
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return 4;
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder vh;
        if (convertView == null) {
            vh = new ViewHolder();
            convertView = mInflater.inflate(R.layout.item_image, parent, false);
            vh.imageView = (ImageView) convertView.findViewById(R.id.iv_image);
            vh.tvTitle = (TextView) convertView.findViewById(R.id.tv_title);
            convertView.setTag(vh);
        } else {
            vh = (ViewHolder) convertView.getTag();
        }
        vh.tvTitle.setText("item :" + position);
        vh.imageView.setImageResource(mResMap.get(position));
        return convertView;
    }

    class ViewHolder {
        ImageView imageView;
        TextView tvTitle;
    }
}
