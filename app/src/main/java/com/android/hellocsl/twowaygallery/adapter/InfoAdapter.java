package com.android.hellocsl.twowaygallery.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.android.hellocsl.twowaygallery.R;


/**
 * Created by HelloCsl(cslgogogo@gmail.com) on 2015/9/24 0024.
 */
public class InfoAdapter extends BaseAdapter {
    private LayoutInflater mInflater;
    private boolean isHorizontal = true;

    public InfoAdapter(Context context, boolean isHorizontal) {
        mInflater = LayoutInflater.from(context);
        this.isHorizontal = isHorizontal;
    }

    @Override
    public int getCount() {
        return 10;
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
        ViewHolder holder;
        if (convertView == null) {
            if (isHorizontal) {
                convertView = mInflater.inflate(R.layout.item_horizontal_info, parent, false);
            } else {
                convertView = mInflater.inflate(R.layout.item_vertical_info, parent, false);
            }
            holder = new ViewHolder();
            holder.tvContent = (TextView) convertView.findViewById(R.id.tv_content);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.tvContent.setText("content:" + position);
        return convertView;
    }

    private class ViewHolder {
        public TextView tvContent;

    }
}
