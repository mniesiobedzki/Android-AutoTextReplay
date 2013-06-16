package pl.niesiobedzki.marek.autotextreplay.location;

import android.content.Context;
import android.location.Criteria;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.util.Log;

/**
 * Created by marek on 16/6/13.
 */
public class MyLocation {

    private static String TAG = "MyLocation";

    private final LocationManager locationManager;
    private final MyLocationListener locationListener;
    private final Location location;

    public MyLocation(Context applicationContext){
        locationManager = (LocationManager) applicationContext.getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        String provider;
        provider = locationManager.getBestProvider(criteria, false);
        Log.i(TAG, "GPS Provider: " + provider);
        locationListener = new MyLocationListener();
        location = locationManager.getLastKnownLocation(provider);
    }

    public void requestLocationUpdates(){
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,5000,10,locationListener);
        Log.d(TAG, "Location Updateds Requested!");
    }

    public void removeGpsStatusListener(){
        locationManager.removeUpdates(locationListener);
        Log.d(TAG, "Location Updateds removed!");

    }
}
