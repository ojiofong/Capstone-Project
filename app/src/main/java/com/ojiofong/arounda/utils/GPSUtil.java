package com.ojiofong.arounda.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.preference.PreferenceManager;
import android.provider.Settings;

import com.ojiofong.arounda.R.string;
import com.ojiofong.arounda.ui.MainActivity;

public class GPSUtil {

    public static final String PREF_LAST_LAT = "last_lat";
    public static final String PREF_LAST_LNG = "last_lng";

    private final Context mContext;

    public GPSUtil(Context context) {
        this.mContext = context;
    }

    public boolean isNetworkEnabled() {
        LocationManager manager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        boolean networkStatus = manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        return networkStatus;
    }

    public boolean canGetLocation() {
        return isGPSEnabled() || isNetworkEnabled();
    }

    public boolean isGPSEnabled() {
        LocationManager manager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        boolean gpsStatus = manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        return gpsStatus;
    }


    public void setLastKnownLatitude(String latString) {
        PreferenceManager.getDefaultSharedPreferences(mContext).edit().putString(PREF_LAST_LAT, latString).apply();
    }

    public void setLastKnownLongitude(String lngString) {
        PreferenceManager.getDefaultSharedPreferences(mContext).edit().putString(PREF_LAST_LNG, lngString).apply();
    }

    /**
     * Function to get latitude
     */
    public Double getLastKnownLatitude() {
        String latString = PreferenceManager.getDefaultSharedPreferences(mContext).getString(PREF_LAST_LAT, null);
        Double latitude = MainActivity.LAST_KNOWN_LAT;
        if (latString != null) {
            latitude = Double.parseDouble(latString);
        }
        return latitude;
    }

    /**
     * Function to get longitude
     */
    public Double getLastKnownLongitude() {

        String lngString = PreferenceManager.getDefaultSharedPreferences(mContext).getString(PREF_LAST_LNG, null);
        Double longitude = MainActivity.LAST_KNOWN_LON;
        if (lngString != null) {
            longitude = Double.parseDouble(lngString);
        }
        return longitude;
    }


    public Double getAppLat() {
        SharedPreferences getPref = mContext.getSharedPreferences("settings", Context.MODE_PRIVATE);
        Boolean usingCurrentLocation = getPref.getBoolean("usecurrentloc_Pref", true); // default to true if nothing
        String latString = getPref.getString("lat_Pref", null);

        if (usingCurrentLocation) {

            return getLastKnownLatitude();
        } else {
            return Double.parseDouble(latString);
        }
    }

    public double getAppLng() {
        SharedPreferences getPref = mContext.getSharedPreferences("settings", Context.MODE_PRIVATE);
        Boolean usingCurrentLocation = getPref.getBoolean("usecurrentloc_Pref", true); // default to true if nothing
        String lngString = getPref.getString("lng_Pref", null);

        if (usingCurrentLocation) {
            return getLastKnownLongitude();
        } else {
            return Double.parseDouble(lngString);
        }
    }

    /**
     * Function to show settings alert dialog On pressing Settings button will
     * launch Settings Options
     */
    public void showSettingsAlert() {
        //	CharSequence[] myItems = {"first", "second", "third"};

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);
        alertDialog.setTitle(mContext.getString(string.gps_location_settings));
        alertDialog.setMessage(mContext.getString(string.enable_gps_message));
        alertDialog.setCancelable(false);
        alertDialog.setPositiveButton(mContext.getString(string.settings), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                mContext.startActivity(intent);
            }
        });
        alertDialog.setNegativeButton(mContext.getString(string.cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        alertDialog.show();
    }

    public void showGoogleLocationSettingsAlert() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);
        alertDialog.setTitle(mContext.getString(string.gps_location_settings));
        alertDialog.setMessage(mContext.getString(string.enable_google_location));
        alertDialog.setCancelable(false);
        alertDialog.setPositiveButton(mContext.getString(string.settings), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                mContext.startActivity(intent);
            }
        });
        alertDialog.setNegativeButton(mContext.getString(string.cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        alertDialog.show();
    }

}
