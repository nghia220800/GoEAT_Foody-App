package com.example.goeat.Fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.goeat.Place;
import com.example.goeat.R;
import com.example.goeat.TabActivity;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.Collections;

public class NearbyFragment extends Fragment {
    RecyclerView recyclerView;
    View v;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public NearbyFragment(){}

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_nearby, container, false);
        ((TabActivity)getActivity()).setFragmentRefreshListener(new TabActivity.FragmentRefreshListener() {
            @Override
            public void onRefresh() {
                Collections.shuffle(TabActivity.placesList);
                updateOperation();
            }
        });
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.v=view;
        recyclerView=v.findViewById(R.id.nearby_recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
    }

    @Override
    public void onResume() {
        super.onResume();
        updateOperation();
    }
    public void updateOperation(){
        nearbyAdapter nAdapter=new nearbyAdapter(getActivity());
        recyclerView.setAdapter(nAdapter);
        nAdapter.notifyDataSetChanged();
    }
}