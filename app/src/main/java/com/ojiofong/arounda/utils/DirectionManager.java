package com.ojiofong.arounda.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.net.URLConnection;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.view.ContextThemeWrapper;
import android.widget.Toast;

import com.ojiofong.arounda.R;
import com.ojiofong.arounda.ui.MainActivity;

public class DirectionManager {

    private Context context;
    private Double latFromIntent, lonFromIntent, originLat, originLng, getLat, getLng, mLastKnownLat, mLastKnownLng;
    private int pos, iconID;

    public DirectionManager(Context context) {
        this.context = context;
        initialize();
    }

    private void initialize() {
        GPSUtil gps = new GPSUtil(context);
        mLastKnownLat = MainActivity.LAST_KNOWN_LAT != null ? MainActivity.LAST_KNOWN_LAT : gps.getLastKnownLatitude();
        mLastKnownLng = MainActivity.LAST_KNOWN_LON != null ? MainActivity.LAST_KNOWN_LON : gps.getLastKnownLongitude();

    }

    public void performAction(int iconId, String flag, Double dLatFromIntent, Double dLonFromIntent, Double oLat, Double oLon) {
        // TODO Auto-generated method stub
        this.iconID = iconId;
        this.latFromIntent = dLatFromIntent;
        this.lonFromIntent = dLonFromIntent;
        this.getLat = oLat;
        this.getLng = oLon;

        SharedPreferences getPref = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
        Boolean usingCurrentLocation = getPref.getBoolean("usecurrentloc_Pref", true); // default to true if nothing

        if (usingCurrentLocation) {

            getDirection(latFromIntent, lonFromIntent, flag);
        } else {
            showDirectionDialog(latFromIntent, lonFromIntent, flag);
        }

    }

    private void showDirectionDialog(final Double dLat, final Double dLng, final String dirflg) {
        // called on getDirection if not using current location only
        // create alert dialog if not using current location

        if (dLat == null || dLng == null) {
            Toast.makeText(context.getApplicationContext(), context.getString(R.string.select_location), Toast.LENGTH_SHORT).show();
            return;
            //get out of method.. not applicable to single map activity
        }

        final String[] myItems = {context.getString(R.string.current_location), context.getString(R.string.changed_location)};
        pos = 0; // making sure default is 0
        Drawable dr = context.getResources().getDrawable(iconID);
        Bitmap bitmap = ((BitmapDrawable) dr).getBitmap();
        // Scale it to 50 x 50
        Drawable d = new BitmapDrawable(context.getResources(), Bitmap.createScaledBitmap(bitmap, 65, 65, true));


        Context mThemeContext = new ContextThemeWrapper(context, R.style.AppTheme);
        AlertDialog.Builder builder = new AlertDialog.Builder(mThemeContext);
        builder.setTitle(context.getString(R.string.get_direction_from));
        builder.setIcon(d);
        builder.setSingleChoiceItems(myItems, 0, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                pos = which;

            }
        });
        builder.setCancelable(true);
        builder.setNegativeButton(context.getString(R.string.cancel), null);
        builder.setPositiveButton(context.getString(R.string.okay), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (pos == 0) {
                    originLat = mLastKnownLat;
                    originLng = mLastKnownLng;

                } else {
                    originLat = getLat;
                    originLng = getLng;
                }

                GPSUtil gps = new GPSUtil(context);
                if (gps.canGetLocation()) {

                    StringBuilder sb = new StringBuilder();
                    sb.append("http://maps.google.com/maps?saddr=");
                    sb.append(originLat).append(",").append(originLng);
                    sb.append("&daddr=").append(dLat).append(",").append(dLng);
                    sb.append("&dirflg=").append(dirflg);

                    String iUrl = sb.toString();

                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(iUrl)).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                    ((Activity) context).overridePendingTransition(R.anim.right_slide_in, R.anim.left_slide_out); // for starting
                    // activity only

                } else if (!gps.canGetLocation()) {
                    // GPS or Network is not enabled
                    // Ask user to enable GPS/network in settings
                    gps.showSettingsAlert();
                } else {
                    //unreachable but no probs
                    Toast.makeText(context, context.getString(R.string.select_location), Toast.LENGTH_SHORT).show();
                }

            }
        });
        builder.show();

    }

    private void getDirection(Double dLat, Double dLng, String dirflg) {

        if (dLat == null || dLng == null) {
            Toast.makeText(context, context.getString(R.string.select_location), Toast.LENGTH_SHORT).show();
            return;
            //get out of method.. not applicable to single map activity
        }

        GPSUtil gps = new GPSUtil(context);

        if (gps.canGetLocation()) {

            double originLat = mLastKnownLat;
            double originLng = mLastKnownLng;

            StringBuilder sb = new StringBuilder();
            sb.append("http://maps.google.com/maps?saddr=");
            sb.append(originLat).append(",").append(originLng);
            sb.append("&daddr=").append(dLat).append(",").append(dLng);
            sb.append("&dirflg=").append(dirflg);

            String iUrl = sb.toString();

            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(iUrl)).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
            ((Activity) context).overridePendingTransition(R.anim.right_slide_in, R.anim.left_slide_out); // for starting activity only

        } else if (!gps.canGetLocation()) {
            // GPS or Network is not enabled
            // Ask user to enable GPS/network in settings
            gps.showSettingsAlert();
        }
    }

    public Double getAppLat() {
        SharedPreferences getPref = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
        Boolean usingCurrentLocation = getPref.getBoolean("usecurrentloc_Pref", true); // default to true if nothing
        String latString = getPref.getString("lat_Pref", null);

        if (usingCurrentLocation) {

            return mLastKnownLat;
        } else {
            return Double.parseDouble(latString);
        }
    }

    public double getAppLng() {
        SharedPreferences getPref = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
        Boolean usingCurrentLocation = getPref.getBoolean("usecurrentloc_Pref", true); // default to true if nothing
        String lngString = getPref.getString("lng_Pref", null);

        if (usingCurrentLocation) {
            return mLastKnownLng;
        } else {
            return Double.parseDouble(lngString);
        }
    }

    public double calculateDistance(double lat1, double lon1, double lat2, double lon2) {

        float[] results = new float[3];
        Location.distanceBetween(lat1, lon1, lat2, lon2, results);

        BigDecimal bd = new BigDecimal(results[0]); // results in meters
        BigDecimal bdRounded = bd.setScale(2, RoundingMode.HALF_UP);

        double distance = bdRounded.doubleValue(); // distance in meters

        SharedPreferences spref = PreferenceManager.getDefaultSharedPreferences(context);
        String str = spref.getString("distanceUnit", "NuLL");

        if (str.matches("kmValue")) {
            // converting distance to KiloMeters
            distance = (Double) (distance * 0.001f); // convert m to KM
            bd = new BigDecimal(distance);
            bdRounded = bd.setScale(2, RoundingMode.HALF_UP);
            distance = bdRounded.doubleValue();

            return distance;

        } else {
            // use default
            // Convert distance to Miles
            distance = (Double) (distance * 0.000621371f); // convert m to Miles
            bd = new BigDecimal(distance);
            bdRounded = bd.setScale(2, RoundingMode.HALF_UP);
            distance = bdRounded.doubleValue();

            return distance;
        }
    }

    public String getTravelTime(Double oLat, Double oLng, Double dLat, Double dLng, String travelMode) {
        // https://maps.googleapis.com/maps/api/directions/json?origin=38.904014,-77.040014&destination=38.948179,-77.066445

        StringBuilder urlString;
        urlString = new StringBuilder("https://maps.googleapis.com/maps/api/directions/json?");
        urlString.append("origin=").append(oLat).append(",").append(oLng);
        urlString.append("&destination=").append(dLat).append(",").append(dLng);
        urlString.append("&mode=").append(travelMode);

        String theUrl = urlString.toString();
        //	Log.d("theUrl", "theUrl: " + theUrl);

        StringBuilder content = new StringBuilder();
        try {
            URL url = new URL(theUrl);
            URLConnection urlConnection = url.openConnection();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), "UTF-8"), 8);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                content.append(line).append("\n");
            }
            bufferedReader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        String jsonContent = content.toString();
        //	Log.d("jsoncontent", "jsoncontent: " + jsonContent);
        String time = null;

        try {
            JSONObject jsonObject = new JSONObject(jsonContent);
            JSONArray routes = jsonObject.getJSONArray("routes");
            //	Log.d("routes", "routes------" + routes.toString());
            JSONArray legs = routes.getJSONObject(0).getJSONArray("legs");
            JSONObject duration = legs.getJSONObject(0).getJSONObject("duration");
            time = duration.optString("text", null);
            //	Log.d("time", "time-------" + time);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return time;

    }

}
