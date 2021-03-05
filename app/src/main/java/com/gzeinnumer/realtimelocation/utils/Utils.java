package com.gzeinnumer.realtimelocation.utils;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class Utils {
    static final String KEY_REQUESTING_LOCATION_UPDATES = "requesting_locaction_updates";

    static boolean requestingLocationUpdates(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(KEY_REQUESTING_LOCATION_UPDATES, false);
    }

    public static void setRequestingLocationUpdates(Context context, boolean requestingLocationUpdates) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(KEY_REQUESTING_LOCATION_UPDATES, requestingLocationUpdates)
                .apply();
    }

    public static String getLocationText(Location location) {
        return location == null ? "Unknown location" : "(" + location.getLatitude() + ", " + location.getLongitude() + ")";
    }

    public static String getAddress(Location location, Context applicationContext) {
        Geocoder geocoder = new Geocoder(applicationContext, Locale.getDefault());
        List<Address> addresses;

        try {
            addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            String address = addresses.get(0).getAddressLine(0);
            return address;
        } catch (IOException e) {
            e.printStackTrace();
            return "Error";
        }
    }

    public static String getDistanceInKM(String newLA, String newLG, String oldLA, String oldLG) {
        String latituteSales = newLA.equals("0.0") ? "-6.181583" : newLA;
        String longituteSales = newLG.equals("0.0") ? "106.832392" : newLG;

        String distance = "0";
        Location locationSales = new Location("");
        locationSales.setLatitude(Double.parseDouble(latituteSales));
        locationSales.setLongitude(Double.parseDouble(longituteSales));
        Location locationCustomer = new Location("");
        locationCustomer.setLatitude(Double.parseDouble(oldLA));
        locationCustomer.setLongitude(Double.parseDouble(oldLG));
        double dis = locationSales.distanceTo(locationCustomer);
        distance = String.valueOf(Math.round(dis));
        return distance;
    }
}
