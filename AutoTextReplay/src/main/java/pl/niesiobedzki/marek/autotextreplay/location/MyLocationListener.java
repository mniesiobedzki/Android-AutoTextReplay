package pl.niesiobedzki.marek.autotextreplay.location;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import pl.niesiobedzki.marek.autotextreplay.service.AutoTextReplayService;

/**
 * Created by marek on 16/6/13.
 */
public class MyLocationListener implements LocationListener {

    private static final String TAG = "MyLocationListener";

    private double latitude = 0;
    private double altitude = 0;
    private double longitude = 0;
    private AutoTextReplayService mAutoTextReplayService;
    private float accuracy = 0;

    public MyLocationListener(AutoTextReplayService autoTextReplayService) {

        mAutoTextReplayService = autoTextReplayService;
    }

    @Override
    public void onLocationChanged(Location location) {
        latitude = location.getLatitude();
        altitude = location.getAltitude();
        longitude = location.getLongitude();
        accuracy = location.getAccuracy();

        Log.i("GPS", "LATITUDE: " + latitude + ", LONGITUDE: " + longitude + ", ATITUDE: " + altitude + ", PROVIDER: " + location.getProvider());

        // mAutoTextReplayService.sendToActivityNewGpsCordinates(location);

        Intent intent = new Intent("serviceToActivity");
        sendLocationBroadcast(intent);

        storeSharedPreferences();
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {
        Log.d(TAG, "MyLocationListener.onStatusChanged(" + s + ", " + i + ", bundle)");

    }

    private void sendLocationBroadcast(Intent intent) {
        intent.putExtra("latitude", latitude);
        intent.putExtra("longitude", longitude);
        LocalBroadcastManager.getInstance(mAutoTextReplayService.getApplicationContext()).sendBroadcast(intent);
    }

    @Override
    public void onProviderEnabled(String s) {
        Log.d(TAG, "MyLocationListener.onProviderEnabled(" + s + ")");
    }

    @Override
    public void onProviderDisabled(String s) {
        Log.d(TAG, "MyLocationListener.onProviderDisabled(" + s + ")");
        SharedPreferences settings = mAutoTextReplayService.getApplicationContext().getSharedPreferences(AutoTextReplayService.PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(AutoTextReplayService.GPS_LOCATION, false);
        editor.commit();
    }

    private void storeSharedPreferences() {

        SharedPreferences settings = mAutoTextReplayService.getApplicationContext().getSharedPreferences(AutoTextReplayService.PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(AutoTextReplayService.GPS_LOCATION, true);
        editor.putFloat(AutoTextReplayService.GPS_LATITUDE, (float) latitude);
        editor.putFloat(AutoTextReplayService.GPS_LONGITUDE, (float) longitude);
        editor.putFloat(AutoTextReplayService.GPS_ALTITUDE, (float) altitude);
        editor.putFloat(AutoTextReplayService.GPS_ACCURACY, accuracy);

        /* Commit the edits */
        editor.commit();
    }
}
