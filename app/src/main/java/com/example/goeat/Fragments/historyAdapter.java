package com.example.goeat.Fragments;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.goeat.DashboardActivity;
import com.example.goeat.HistoryVal;
import com.example.goeat.Place;
import com.example.goeat.R;
import com.example.goeat.TabActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.logging.Handler;

public class historyAdapter extends RecyclerView.Adapter<historyAdapter.HistoryViewHolder> {
    Context mContext;
    private DatabaseReference mDatabase;
    historyAdapter(Context c){
        mContext=c;
    }

    public static class HistoryViewHolder extends RecyclerView.ViewHolder{
        TextView nameTxt,tagTxt,ratingTxt;
        ImageView foodImg;
        LinearLayout itemLayout;
        public HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTxt=itemView.findViewById(R.id.textView1);
            foodImg=itemView.findViewById(R.id.foodImg);
            tagTxt=itemView.findViewById(R.id.textView3);
            ratingTxt=itemView.findViewById(R.id.ratingTxt);
            itemLayout=itemView.findViewById(R.id.itemLayout);
        }
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v;
        v=LayoutInflater.from(mContext.getApplicationContext()).inflate(R.layout.nearby_item,parent,false);
        final HistoryViewHolder holder=new HistoryViewHolder(v);
        holder.itemLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int index=holder.getAdapterPosition();
                String tag=TabActivity.visitedList.get(index).getCategories().get(0);
                Intent hisIntent = new Intent(mContext, DashboardActivity.class);
                hisIntent.putExtra("tag",tag);
                hisIntent.putExtra("index",index);
                hisIntent.putExtra("isHistory",true);
                mContext.startActivity(hisIntent);
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        holder.nameTxt.setText(TabActivity.visitedList.get(position).getName());
        Picasso.get().load(TabActivity.visitedList.get(position).getPhoto()).into(holder.foodImg);
        holder.tagTxt.setText("");
        for (String tag:TabActivity.visitedList.get(position).getCategories()){

            holder.tagTxt.append(tag);
            if (tag!=TabActivity.visitedList.get(position).getCategories().get(TabActivity.visitedList.get(position).getCategories().size()-1)){
                holder.tagTxt.append(", ");
            }
        }
        ;
        if(TabActivity.visitedList.get(position).getRating() > 7.5){
            holder.ratingTxt.setBackgroundResource(R.drawable.rating_point);
        }else if(TabActivity.visitedList.get(position).getRating() > 5){
            holder.ratingTxt.setBackgroundResource(R.drawable.rating_point_medium);
        }else{
            holder.ratingTxt.setBackgroundResource(R.drawable.rating_point_low);
        }
        if (TabActivity.visitedList.get(position).getRating()>=10.0){
            holder.ratingTxt.setText("10");
        }else
            holder.ratingTxt.setText(String.valueOf(TabActivity.visitedList.get(position).getRating()));
    }

    @Override
    public int getItemCount() {
        return TabActivity.visitedList.size();
    }

}
