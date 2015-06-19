package com.hasan.android.movieapp.Adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.util.List;

/**
 * Created by hasan on 6/12/2015.
 */
public class ImageAdapter extends BaseAdapter {

    Context context;
    List<String> poster_path;
    public ImageAdapter(Context context, List<String> poster_path){
        this.context = context;
        this.poster_path = poster_path;
    }
    @Override
    public int getCount() {
        return poster_path.toArray().length;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        if (convertView == null) {
            // if it's not recycled, initialize with new ImageView
            imageView = new ImageView(context);
        } else {
            imageView = (ImageView) convertView; //recylcing the same view
        }

        //setup the poster path URL
        String basepath="http://image.tmdb.org/t/p/w342/";
        String relativePath=poster_path.get(position);

        //Picasso.with(mContext).load(basepath+relativePath).resize(342,300).centerCrop().into(imageView);
        Glide.with(context).load(basepath+relativePath).into(imageView);
        return imageView;

    }
}
