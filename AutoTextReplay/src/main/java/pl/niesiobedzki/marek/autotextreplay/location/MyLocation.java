package pl.niesiobedzki.marek.autotextreplay.location;

import android.content.Context;
import android.location.Criteria;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.io.Serializable;

import pl.niesiobedzki.marek.autotextreplay.service.AutoTextReplayService;

/**
 * Created by marek on 16/6/13.
 */
public class MyLocation implements Serializable {

    private static String TAG = "MyLocation";

    private final LocationManager locationManager;
    private final MyLocationListener locationListener;
    private final Location location;
    private boolean active;
    private AutoTextReplayService mAutoTextReplayService;

    public MyLocation(Context applicationContext, AutoTextReplayService autoTextReplayService){
        mAutoTextReplayService = autoTextReplayService;
        locationManager = (LocationManager) applicationContext.getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        String provider;
        provider = locationManager.getBestProvider(criteria, false);
        Log.i(TAG, "GPS Provider: " + provider);
        locationListener = new MyLocationListener(mAutoTextReplayService);
        location = locationManager.getLastKnownLocation(provider);
        this.active = false;
    }

    public void requestLocationUpdates(){
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,5000,10,locationListener);
        active = true;
        Log.d(TAG, "Location Updateds Requested!");
    }

    public void removeGpsStatusListener(){
        locationManager.removeUpdates(locationListener);
        active = false;
        Log.d(TAG, "Location Updateds removed!");

    }

    /**
     *
     * @return
     */
    public Location getLocation(){
        return this.location;
    }

    public String toString() {
        return "Location: " + this.location.getLatitude() + "[lat], " + this.location.getLongitude() + "[lon].";
    }

    public boolean isActive() {
        return this.active;
    }
}
