package com.example.goeat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import org.jetbrains.annotations.NotNull;
import org.osmdroid.util.GeoPoint;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static android.content.Context.LOCATION_SERVICE;
import static androidx.core.content.ContextCompat.checkSelfPermission;
import static java.lang.System.currentTimeMillis;

@SuppressWarnings("ALL")
public class GeocodingAsync extends AsyncTask<Void, Void, Address> implements LocationListener{
    @SuppressLint("StaticFieldLeak")
    Activity contextParent;
    public LocationManager mLocationManager;
    public long mLastime=0;

    public GeocodingAsync(@org.jetbrains.annotations.NotNull Activity contextParent) {
        this.contextParent = contextParent;
        this.mLocationManager = (LocationManager)contextParent.getSystemService(LOCATION_SERVICE);
        Log.d("Geocoding:", "reverse-geocoding current location AsyncTask created");
    }
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if (checkSelfPermission(contextParent, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
            mLocationManager.requestLocationUpdates(mLocationManager.GPS_PROVIDER, 1, 0.0f, this);
    }
    @Override
    protected Address doInBackground(Void... voids) {
        Location location = null;
        Address address=null;
        String district="";
            if (checkSelfPermission(contextParent, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                location = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (location == null)
                    location = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }
        SharedPreferences sharedPref = contextParent.getSharedPreferences("GOeAT", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        final GeoPoint currentPoint = new GeoPoint(location);
        Log.d("test",currentPoint.toString());
        try {
            address=getAddress(currentPoint);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (address==null) {
            Log.d("test","null");
            return null;
        }
        else {
            if (address.getSubAdminArea()!=null) {
                district=address.getSubAdminArea();
                Log.d("test",district);
                editor.putString("curAddress",district);
            }
            editor=putDouble(editor,"mStartLadtitude",location.getLatitude());
            editor=putDouble(editor,"mStartLongtitude",location.getLongitude());
            editor.apply();
        }
        return address;
    }
    @Override
    public void onLocationChanged(Location location) {
    }
    @Override public void onProviderDisabled(String provider) {}

    @Override public void onProviderEnabled(String provider) {}

    @Override public void onStatusChanged(String provider, int status, Bundle extras) {}

    @Override
    protected void onPostExecute(Address address) {
    }

    public Address getAddress(@NotNull GeoPoint p) throws IOException {
        Geocoder geocoder = new Geocoder(contextParent);
        //String theAddress;
        double dLatitude = p.getLatitude();
        double dLongitude = p.getLongitude();
        List<Address> addresses=new ArrayList<Address>();
        while(addresses.size()<=0){
            addresses= geocoder.getFromLocation(dLatitude, dLongitude, 1);
        }
        Address address = addresses.size() > 0 ? addresses.get(0) : null;
        Log.d("test",address.toString());
        return address != null ? address : null;
    }
    public String getAddressStr(Address address){
        String theAddress;
            StringBuilder sb = new StringBuilder();
        if (address == null) theAddress = null;
        else {
            int n = address.getMaxAddressLineIndex();
            int i=0;
            while (i<=n) {
                if (i!=0) sb.append(", ");
                sb.append(address.getAddressLine(i));
                i++;
            }
            theAddress = sb.toString();
        }
        if (theAddress != null) return theAddress;
        return "";
    }
    //cast to get shared preferences
    SharedPreferences.Editor putDouble(@NotNull final SharedPreferences.Editor edit, final String key, final double value) {
        return edit.putLong(key, Double.doubleToRawLongBits(value));
    }
}
