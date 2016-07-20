package com.yellowmessenger.sdk.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.yellowmessenger.sdk.R;
import com.yellowmessenger.sdk.models.Featured;

import java.util.List;

/**
 * Created by kishore on 14/07/16.
 */

public class FeaturedAdapter extends BaseAdapter {
    private Context context;
    private List<Featured> values;

    public FeaturedAdapter(Context context, List<Featured> values) {
        this.context = context;
        this.values = values;
    }


    @Override
    public int getCount() {
        return values.size();
    }

    @Override
    public Object getItem(int position) {
        return values.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Featured featured = values.get(position);
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View gridView;
        if (convertView == null) {
            gridView = inflater.inflate(R.layout.fragment_featured, parent,false);
            TextView title = (TextView)gridView.findViewById(R.id.title);
            TextView banner = (TextView)gridView.findViewById(R.id.banner);
            ImageView imageView = (ImageView)gridView.findViewById(R.id.imageView);
            banner.setText(featured.getBanner());
            title.setText(featured.getTitle());
            DrawableManager.getInstance(context).fetchDrawableOnThread(featured.getImage(),imageView);
        } else {
            gridView = (View) convertView;
        }

        return gridView;
    }
}
