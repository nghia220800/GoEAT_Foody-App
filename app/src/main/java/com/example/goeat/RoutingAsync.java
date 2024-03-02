package com.example.goeat;

import android.annotation.SuppressLint;
import android.app.Activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.widget.TextView;

import org.osmdroid.bonuspack.routing.OSRMRoadManager;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Polyline;

import java.util.ArrayList;

public class RoutingAsync extends AsyncTask<ArrayList<GeoPoint>, Void, Road> {
    @SuppressLint("StaticFieldLeak")
    Activity mContext;
    private RoadManager roadManager;
    public RoutingAsync(Activity context) {
        this.mContext = context;
        roadManager=new OSRMRoadManager(mContext);
    }
    @SafeVarargs
    @Override
    protected final Road doInBackground(ArrayList<GeoPoint>... params) {
        ArrayList<GeoPoint> waypoints = params[0];
        Road road=roadManager.getRoad(waypoints);
        while(road.mStatus != Road.STATUS_OK){
            road=roadManager.getRoad(waypoints);
        }
        return road;
    }
    @SuppressLint("SetTextI18n")
    @Override
    protected void onPostExecute(Road result) {
        MapView map=mContext.findViewById(R.id.map);
        TextView routeLen=mContext.findViewById(R.id.routeLength);
        TextView routeInfo=mContext.findViewById(R.id.routeInfo);
        Polyline roadOverlay = RoadManager.buildRoadOverlay(result, Color.parseColor("#24B2B4"),20);
        roadOverlay.getOutlinePaint().setStrokeCap(Paint.Cap.ROUND);
        SharedPreferences sharedPref = mContext.getSharedPreferences("GOeAT", Context.MODE_PRIVATE);
        routeInfo.setText(sharedPref.getString("mRouteInfo",""));
        routeLen.setText("Distance: "+ (int) Math.round(roadOverlay.getDistance() / 1000d) +" km");
        map.getOverlays().add(0,roadOverlay);
    }
}
