package com.example.goeat.Fragments;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroupOverlay;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.goeat.R;

import androidx.annotation.NonNull;


/**
 * @author Admin
 * @date 11/19/2020
 */

class homeAdapter extends BaseAdapter {
    private Activity activity;
    private String[] items;
    private int[] imgs;
    public homeAdapter(Activity activity,String[] items,int[] imgs){
        this.activity = activity;
        this.items = items;
        this.imgs = imgs;
    }
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public int getCount() {
        return items.length;
    }

    @Override
    public Object getItem(int position) {
        return items[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
    static class ViewHolder{
        TextView tvName;
        ImageView imgs;
    }
    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        homeAdapter.ViewHolder holder;
        LayoutInflater inflater = activity.getLayoutInflater();

        if(view == null) {
            view = inflater.inflate(R.layout.home_item,null);
            holder = new homeAdapter.ViewHolder();
            holder.tvName = (TextView) view.findViewById(R.id.home_textView1);
            holder.imgs = view.findViewById(R.id.bg_img);
            view.setTag(holder);
        }else{
            holder = (homeAdapter.ViewHolder)view.getTag();
        }
        holder.tvName.setText(items[i]);
        holder.imgs.setImageResource(imgs[i]);


        return view;
    }
}
