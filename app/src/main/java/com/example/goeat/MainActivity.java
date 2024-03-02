package com.example.goeat;


import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;


import android.location.LocationListener;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;

import com.example.goeat.auth.ScaleBitmap;
import com.google.firebase.BuildConfig;

import org.jetbrains.annotations.NotNull;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.util.NetworkLocationIgnorer;
import org.osmdroid.views.CustomZoomButtonsController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay;
import org.osmdroid.views.overlay.mylocation.DirectedLocationOverlay;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements LocationListener, SensorEventListener, MapView.OnFirstLayoutListener {
    float mAzimuthAngleSpeed = 0.0f;
    private MapView map = null;
    protected DirectedLocationOverlay myLocationOverlay;
    protected LocationManager mLocationManager;
    protected SensorManager mSensorManager;
    protected Sensor mOrientation;
    private GeoPoint mStartPoint,mEndPoint;
    private ArrayList<GeoPoint> waypoints = new ArrayList<>(2);

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Context ctx = getApplicationContext();
        Configuration.getInstance().setOsmdroidBasePath(new File(Environment.getExternalStorageDirectory(), "osmdroid"));
        Configuration.getInstance().setOsmdroidTileCache(new File(Environment.getExternalStorageDirectory(), "osmdroid/tiles"));
        Configuration.getInstance().setUserAgentValue(BuildConfig.APPLICATION_ID);
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        Objects.requireNonNull(getSupportActionBar()).setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setElevation(0);
        setContentView(R.layout.activity_main);
        ImageButton locateBtn = findViewById(R.id.locateBtn);
        map = findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setTilesScaledToDpi(true);

        map.setScrollableAreaLimitLatitude(MapView.getTileSystem().getMaxLatitude(), MapView.getTileSystem().getMinLatitude(), 0);
        map.getOverlayManager().getTilesOverlay().setLoadingBackgroundColor(Color.parseColor("#24b2b4"));
        map.getOverlayManager().getTilesOverlay().setLoadingLineColor(Color.parseColor("#ffffff"));
        map.setMaxZoomLevel(21.0);
        map.setMinZoomLevel(3.0);
        map.setMultiTouchControls(true);
        
        RotationGestureOverlay mRotationGestureOverlay = new RotationGestureOverlay(map);
        mRotationGestureOverlay.setEnabled(true);
        map.getOverlays().add(mRotationGestureOverlay);
        //map.setBuiltInZoomControls(false);
        map.getZoomController().setVisibility(CustomZoomButtonsController.Visibility.NEVER);

        mLocationManager = (LocationManager)getSystemService(LOCATION_SERVICE);
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mOrientation = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        getRoadAsync();
        myLocationOverlay = new DirectedLocationOverlay(this);
        Drawable myLocationDrawable= ResourcesCompat.getDrawable(getResources(),R.mipmap.current_location_icon,null);
        assert myLocationDrawable != null;
        Bitmap myLocationBitmap=((BitmapDrawable)myLocationDrawable).getBitmap();
        myLocationBitmap=ScaleBitmap.scaleDown(myLocationBitmap,80,true);
        myLocationOverlay.setDirectionArrow(myLocationBitmap);
        map.getOverlays().add(myLocationOverlay);
        Location location = null;
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            location = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (location == null)
                location = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }
        if (location != null) {
            onLocationChanged(location);
            Log.d("location null","its not null");
        } else {
            Log.d("currentLocation","current location not found");
            myLocationOverlay.setEnabled(false);
        }
        locateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                map.getController().animateTo(myLocationOverlay.getLocation());
            }
        });
    }
    @Override
    public void onResume() {
        super.onResume();
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
        boolean isOneProviderEnabled = startLocationUpdates();
        myLocationOverlay.setEnabled(isOneProviderEnabled);
        mSensorManager.registerListener(this, mOrientation, SensorManager.SENSOR_DELAY_NORMAL);
        if (mStartPoint!=null && mEndPoint !=null){
            centerTheRoute();
        }else{
            map.getController().setZoom(15.0);
            map.getController().setCenter(myLocationOverlay.getLocation());
        }
        map.onResume(); //needed for compass, my location overlays, v6.0.0 and up

    }

    @Override
    public void onPause() {
        super.onPause();
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().save(this, prefs);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
            mLocationManager.removeUpdates(this);
        mSensorManager.unregisterListener(this);
        map.onPause();  //needed for compass, my location overlays, v6.0.0 and up
    }

    boolean startLocationUpdates(){
        boolean result = false;
        for (final String provider : mLocationManager.getProviders(true)) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mLocationManager.requestLocationUpdates(provider, 2000, 0.0f, this);
                result = true;
            }
        }
        return result;
    }
    private final NetworkLocationIgnorer mIgnorer = new NetworkLocationIgnorer();
    long mLastTime = 0; // milliseconds
    double mSpeed = 0.0; // km/h
    @Override public void onLocationChanged(@NotNull final Location pLoc) {
        Log.d("currentProvider",pLoc.getProvider());
        //This should ignore network provider
        long currentTime = System.currentTimeMillis();
        if (mIgnorer.shouldIgnore(pLoc.getProvider(), currentTime))
            return;
        double dT = currentTime - mLastTime;
        if (dT < 100){
            return;
        }
        mLastTime = currentTime;

        GeoPoint newLocation = new GeoPoint(pLoc);
            //we get the location for the first time:
            myLocationOverlay.setEnabled(true);
        GeoPoint prevLocation = myLocationOverlay.getLocation();
        myLocationOverlay.setLocation(newLocation);
        mStartPoint=newLocation;
        myLocationOverlay.setAccuracy((int)pLoc.getAccuracy());
        Log.d("currentLocation","current:"+newLocation.getLatitude()+" "+newLocation.getLongitude());
        if (prevLocation!=null && pLoc.getProvider().equals(LocationManager.GPS_PROVIDER)){
            mSpeed = pLoc.getSpeed() * 3.6;
            /* TODO: check if speed is not too small */
            if (mSpeed >= 0.1){
                mAzimuthAngleSpeed = pLoc.getBearing();

                myLocationOverlay.setBearing(mAzimuthAngleSpeed);
            }
        }
        map.invalidate();
    }
    @Override public void onProviderDisabled(@NotNull String provider) {}

    @Override public void onProviderEnabled(@NotNull String provider) {}

    @Override public void onStatusChanged(String provider, int status, Bundle extras) {}

    @Override public void onAccuracyChanged(Sensor sensor, int accuracy) {
        myLocationOverlay.setAccuracy(accuracy);
        map.invalidate();
    }

    static float mAzimuthOrientation = 0.0f;
    @Override public void onSensorChanged(@NotNull SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ORIENTATION) if (mSpeed < 0.1) {
            float azimuth = event.values[0];
            if (Math.abs(azimuth - mAzimuthOrientation) > 2f) {
                mAzimuthOrientation = azimuth;
                myLocationOverlay.setBearing(mAzimuthOrientation);
                map.invalidate();
            }

        }
    }
    //cast long bits back to double
    double getDouble(@NotNull final SharedPreferences prefs, final String key) {
        return Double.longBitsToDouble(prefs.getLong(key, Double.doubleToLongBits(0)));
    }
    //ROUTING SECTION
    @SuppressLint("UseCompatLoadingForDrawables")
    public void getRoadAsync(){
        SharedPreferences sharedPref = getSharedPreferences("GOeAT", Context.MODE_PRIVATE);
        double mLadtitude=getDouble(sharedPref,"mStartLadtitude");
        double mLongtitude=getDouble(sharedPref,"mStartLongtitude");
        mStartPoint=new GeoPoint(mLadtitude,mLongtitude);
        mLadtitude=getDouble(sharedPref,"mEndLadtitude");
        mLongtitude=getDouble(sharedPref,"mEndLongtitude");
        mEndPoint=new GeoPoint(mLadtitude,mLongtitude);
        if (mStartPoint==null || mEndPoint==null) return;
        waypoints.add(mStartPoint);
        waypoints.add(mEndPoint);
        //Draw markers
        Marker startMarker = new Marker(map);
        startMarker.setPosition(mStartPoint);
        startMarker.setAnchor(Marker.ANCHOR_CENTER,Marker.ANCHOR_BOTTOM);
        startMarker.setIcon(getResources().getDrawable(R.mipmap.marker_current_location,null));
        map.getOverlays().add(startMarker);

        Marker endMarker = new Marker(map);
        endMarker.setAnchor(Marker.ANCHOR_CENTER,Marker.ANCHOR_BOTTOM);
        endMarker.setIcon(getResources().getDrawable(R.mipmap.marker_destination,null));
        endMarker.setPosition(mEndPoint);
        map.getOverlays().add(endMarker);

        //Routing
        RoutingAsync routingAsync=new RoutingAsync(MainActivity.this);
        routingAsync.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,waypoints);
    }
    public void centerTheRoute(){
        if (map.getScreenRect(null).height() <= 0) {
            mInitialBoundingBox = computeArea(waypoints);
            map.addOnFirstLayoutListener(this);
        } else
            map.zoomToBoundingBox(computeArea(waypoints), false);
    }
    public BoundingBox computeArea(ArrayList<GeoPoint> points) {

        double nord = 0, sud = 0, ovest = 0, est = 0;

        for (int i = 0; i < points.size(); i++) {
            if (points.get(i) == null) continue;

            double lat = points.get(i).getLatitude();
            double lon = points.get(i).getLongitude();

            if ((i == 0) || (lat > nord)) nord = lat;
            if ((i == 0) || (lat < sud)) sud = lat;
            if ((i == 0) || (lon < ovest)) ovest = lon;
            if ((i == 0) || (lon > est)) est = lon;

        }

        return new BoundingBox(nord+0.005, est+0.005, sud-0.005, ovest-0.005);

    }
    BoundingBox mInitialBoundingBox = null;
    @Override
    public void onFirstLayout(View v, int left, int top, int right, int bottom) {
        if (mInitialBoundingBox != null)
            map.zoomToBoundingBox(mInitialBoundingBox, false);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home){
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}