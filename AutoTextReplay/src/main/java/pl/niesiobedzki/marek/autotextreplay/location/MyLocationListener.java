package pl.niesiobedzki.marek.autotextreplay.location;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;

/**
 * Created by marek on 16/6/13.
 */
public class MyLocationListener implements LocationListener {

    private double latitude = 0;
    private double atitude = 0;
    private double lognitude = 0;

    @Override
    public void onLocationChanged(Location location) {
        latitude = location.getLatitude();
        atitude = location.getAltitude();
        lognitude = location.getLongitude();

        Log.i("GPS", "LATITUDE: " + latitude + ", LONGITUDE: "+lognitude + ", ATITUDE: "+atitude + ", PROVIDER: "+location.getProvider());
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }
}
