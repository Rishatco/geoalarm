package android.rishat.myapplication;

import android.content.Context;
import android.location.Location;
import android.preference.PreferenceManager;

import java.text.DateFormat;
import java.util.Date;

public class Common {

    public static final String KEY_REQUESTING_LOCATIONS_UPDATES = "LocationUpdates";

    public static String getLocationText(Location mLocation) {

        return mLocation== null ? "Unknown Location": new StringBuilder().append(mLocation.getLatitude()).append("/").append(mLocation.getLongitude()).toString();
    }

    public static CharSequence getLocationTitle(MyBackgroundServices myBackgroundServices) {
        return  String.format("Location Updated: %1$s0", DateFormat.getDateInstance().format(new Date()));
    }

    public static void setRequestingLocationUpdates(Context context, boolean value) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit().putBoolean(KEY_REQUESTING_LOCATIONS_UPDATES, value).apply();
    }

    public static boolean requestingLocationUpdates(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(KEY_REQUESTING_LOCATIONS_UPDATES,false);
    }
}