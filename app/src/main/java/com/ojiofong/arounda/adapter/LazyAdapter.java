package com.ojiofong.arounda.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.ojiofong.arounda.R;
import com.ojiofong.arounda.R.id;
import com.ojiofong.arounda.R.layout;
import com.ojiofong.arounda.ViewHolder;
import com.ojiofong.arounda.ui.MainActivity;
import com.ojiofong.arounda.ui.PlaceListActivity;
import com.ojiofong.arounda.ui.PlaceDetailActivity;
import com.ojiofong.arounda.utils.GPSUtil;

import java.util.ArrayList;
import java.util.HashMap;

public class LazyAdapter extends BaseAdapter {

    private Activity activity;
    private ArrayList<HashMap<String, String>> data;
    private static LayoutInflater inflater = null;
    private ListView clv;
    public Context context;

    public LazyAdapter(Context context, ListView lv, Activity a, ArrayList<HashMap<String, String>> d) {
        this.context = context;
        clv = lv;
        activity = a;
        data = d;
        inflater = LayoutInflater.from(activity);
    }

    public int getCount() {
        return data.size();
    }

    public Object getItem(int position) {
        return position;
    }

    public long getItemId(int position) {
        return position;
    }

    @SuppressLint("InflateParams")
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = inflater.inflate(layout.card_list_item, parent, false);

            holder = new ViewHolder();

            //TextView placeReference = (TextView) convertView.findViewById(R.id.reference); // reference
            holder.placeName = (TextView) convertView.findViewById(id.name); // name
            holder.placeRatingBar = (RatingBar) convertView.findViewById(id.place_ratingbar); // rating bar
            holder.placeAddress = (TextView) convertView.findViewById(id.address); // address
            holder.distance = (TextView) convertView.findViewById(id.distance); // distance
            //	TextView placeLatitude = (TextView) convertView.findViewById(R.id.itemLat); // lat
            //	TextView placeLongitude = (TextView) convertView.findViewById(R.id.itemLong); // long
            holder.directionBtn = (ImageView) convertView.findViewById(id.direction); // direction
            holder.cardView = (CardView) convertView.findViewById(id.card_item_place_list); // card view item

            //Setting default my custom font typeFace
            Typeface tf = Typeface.createFromAsset(context.getAssets(), "fonts/roboto_regular.ttf");
            holder.placeName.setTypeface(tf);
            holder.placeAddress.setTypeface(tf);
            holder.distance.setTypeface(tf);

            convertView.setTag(holder);

        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        HashMap<String, String> place = new HashMap<String, String>();
        place = data.get(position);


        // Setting all values in listview
        //placeReference.setText(place.get(PlaceListActivity.KEY_REFERENCE));
        holder.placeName.setText(place.get(PlaceListActivity.KEY_NAME));

        if (place.get(PlaceListActivity.KEY_RATING) != null) {
            holder.placeRatingBar.setRating(Float.parseFloat(place.get(PlaceListActivity.KEY_RATING)));
        } else {
            holder.placeRatingBar.setVisibility(View.GONE); //unreachable code
        }

        holder.placeAddress.setText(place.get(PlaceListActivity.KEY_ADDRESS));
        //	placeLatitude.setText(place.get(PlaceListActivity.KEY_LATITUDE));
        //	placeLongitude.setText(place.get(PlaceListActivity.KEY_LONGITUDE));

        SharedPreferences defaultSharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        String str = defaultSharedPrefs.getString("distanceUnit", "NULL");

        if (str.matches("milesValue")) {
            holder.distance.setText(place.get(PlaceListActivity.KEY_DISTANCE) + " mi");
        } else if (str.matches("kmValue")) {
            holder.distance.setText(place.get(PlaceListActivity.KEY_DISTANCE) + " km");
        } else {
            //use default
            holder.distance.setText(place.get(PlaceListActivity.KEY_DISTANCE) + " mi");
        }

        holder.directionBtn.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                final int position = clv.getPositionForView((View) v.getParent());
                HashMap<String, String> currentPlace = new HashMap<String, String>();
                currentPlace = data.get(position);

                GPSUtil gps = new GPSUtil(context);
                if (gps.canGetLocation()) {

                    double sLat = MainActivity.LAST_KNOWN_LAT != null ? MainActivity.LAST_KNOWN_LAT : gps.getLastKnownLatitude();
                    double sLong = MainActivity.LAST_KNOWN_LON != null ? MainActivity.LAST_KNOWN_LON : gps.getLastKnownLongitude();
                    String dLat = currentPlace.get(PlaceListActivity.KEY_LATITUDE);
                    String dLong = currentPlace.get(PlaceListActivity.KEY_LONGITUDE);
                    String iUrl = "http://maps.google.com/maps?saddr=" + sLat + "," + sLong + "&daddr=" + dLat + "," + dLong + "&dirflg=d";

                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(iUrl)).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                } else {
                    // can't get location
                    // GPS or Network is not enabled
                    // Ask user to enable GPS/network in settings
                    gps.showSettingsAlert();
                }
            }
        });

        final HashMap<String, String> finalPlace = place;
        holder.cardView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoPlaceDetailActivity(finalPlace, position);
            }
        });


        return convertView;
    }

    private void gotoPlaceDetailActivity(HashMap<String, String> currentP, int position) {

        if (currentP != null) {
            String name = currentP.get(PlaceListActivity.KEY_NAME);
            String addy = currentP.get(PlaceListActivity.KEY_ADDRESS);
            String ref = currentP.get(PlaceListActivity.KEY_REFERENCE);
            String lat = currentP.get(PlaceListActivity.KEY_LATITUDE);
            String lon = currentP.get(PlaceListActivity.KEY_LONGITUDE);
            String dis = currentP.get(PlaceListActivity.KEY_DISTANCE);
            String getLat = String.valueOf(new GPSUtil(activity).getAppLat());
            String getLng = String.valueOf(new GPSUtil(activity).getAppLng());

            Intent i = new Intent(activity, PlaceDetailActivity.class);
            i.putExtra("key_name", name);
            i.putExtra("key_addy", addy);
            i.putExtra("key_ref", ref);
            i.putExtra("key_lat", lat);
            i.putExtra("key_lon", lon);
            i.putExtra("key_dis", dis);
            i.putExtra("key_oLat", getLat);
            i.putExtra("key_oLng", getLng);
            activity.startActivity(i);
            activity.overridePendingTransition(R.anim.right_slide_in, R.anim.left_slide_out); // for starting activity only

        }
    }

}