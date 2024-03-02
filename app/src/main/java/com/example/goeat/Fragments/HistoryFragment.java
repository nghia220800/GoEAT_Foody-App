package com.example.goeat.Fragments;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
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

import static com.example.goeat.TabActivity.historyList;
import static com.example.goeat.TabActivity.visitedList;

public class HistoryFragment extends Fragment{
    RecyclerView recyclerView;
    View v;
    DatabaseReference db;
    public HistoryFragment(){

    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_history, container, false);
//        dashboard.setOnHistoryChangeListener(new DashboardActivity.OnHistoryChangeListener() {
//            @Override
//            public void onHistoryChange() {
//                updateOperation();
//            }
//        });
        return rootView;
    }
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.v=view;
        recyclerView=v.findViewById(R.id.history_recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        //updateOperation();
    }
    @Override
    public void onResume() {
        updateOperation();
        super.onResume();
    }
    public void updateOperation(){
        queryToFoodInfo();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.d("historytesting",visitedList.size()+"");
                historyAdapter hAdapter=new historyAdapter(getActivity());
                recyclerView.setAdapter(hAdapter);
            }
        },1000);
    }

    public void queryToFoodInfo(){
        visitedList.clear();
        db = FirebaseDatabase.getInstance().getReference("Places").child("HoChiMinh");
        for (HistoryVal val: historyList){
            db.child(val.getDistrict()).child(val.getPlaceID()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Log.d("historytesting",snapshot.getValue().toString());
                    TabActivity.visitedList.add(snapshot.getValue(Place.class));
                    db.removeEventListener(this);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.d("history","faild on loading history");
                }
            });
        }
    }
}