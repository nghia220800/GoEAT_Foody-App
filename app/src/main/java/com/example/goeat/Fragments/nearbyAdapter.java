package com.example.goeat.Fragments;

/**
 * @author Admin
 * @date 11/20/2020
 */

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.goeat.DashboardActivity;
import com.example.goeat.Place;
import com.example.goeat.R;
import com.example.goeat.TabActivity;
import com.google.android.material.tabs.TabLayout;
import com.squareup.picasso.Picasso;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

/**
 * @author Admin
 * @date 11/19/2020
 */

class nearbyAdapter extends RecyclerView.Adapter<nearbyAdapter.NearbyViewHolder> {
    Context mContext;
    nearbyAdapter(Context c){
        mContext=c;
    }
    @NonNull
    @Override
    public NearbyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v;
        v=LayoutInflater.from(mContext.getApplicationContext()).inflate(R.layout.nearby_item,parent,false);
        final NearbyViewHolder holder=new NearbyViewHolder(v);
        holder.itemLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int index=holder.getAdapterPosition();
                String tag=TabActivity.placesList.get(holder.getAdapterPosition()).getCategories().get(0);
                Intent homeIntent = new Intent(mContext, DashboardActivity.class);
                homeIntent.putExtra("tag",tag);
                homeIntent.putExtra("index",index);
                homeIntent.putExtra("isHistory",false);
                mContext.startActivity(homeIntent);
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull NearbyViewHolder holder, int position) {
        holder.nameTxt.setText(TabActivity.placesList.get(position).getName());
        Picasso.get().load(TabActivity.placesList.get(position).getPhoto()).into(holder.foodImg);
        holder.tagTxt.setText("");
        for (String tag:TabActivity.placesList.get(position).getCategories()){

            holder.tagTxt.append(tag);
            if (tag!=TabActivity.placesList.get(position).getCategories().get(TabActivity.placesList.get(position).getCategories().size()-1)){
                holder.tagTxt.append(", ");
            }
        }
        ;
        if(TabActivity.placesList.get(position).getRating() > 7.5){
            holder.ratingTxt.setBackgroundResource(R.drawable.rating_point);
        }else if(TabActivity.placesList.get(position).getRating() > 5){
            holder.ratingTxt.setBackgroundResource(R.drawable.rating_point_medium);
        }else{
            holder.ratingTxt.setBackgroundResource(R.drawable.rating_point_low);
        }
        if (TabActivity.placesList.get(position).getRating()>=10.0){
            holder.ratingTxt.setText("10");
        }else
        holder.ratingTxt.setText(String.valueOf(TabActivity.placesList.get(position).getRating()));
    }

    @Override
    public int getItemCount() {
        return TabActivity.placesList.size();
    }

    public static class NearbyViewHolder extends RecyclerView.ViewHolder{
        TextView nameTxt,tagTxt,ratingTxt;
        ImageView foodImg;
        LinearLayout itemLayout;
        public NearbyViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTxt=itemView.findViewById(R.id.textView1);
            foodImg=itemView.findViewById(R.id.foodImg);
            tagTxt=itemView.findViewById(R.id.textView3);
            ratingTxt=itemView.findViewById(R.id.ratingTxt);
            itemLayout=itemView.findViewById(R.id.itemLayout);
        }
    }

}