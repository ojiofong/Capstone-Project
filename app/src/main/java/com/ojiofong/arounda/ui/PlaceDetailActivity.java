package com.ojiofong.arounda.ui;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.ojiofong.arounda.R;
import com.ojiofong.arounda.data.Review;
import com.ojiofong.arounda.utils.AlertDialogManager;
import com.ojiofong.arounda.utils.Configuration;
import com.ojiofong.arounda.utils.DirectionManager;
import com.ojiofong.arounda.utils.GPSUtil;
import com.ojiofong.arounda.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class PlaceDetailActivity extends AppCompatActivity implements OnClickListener {

    private static final String TAG = PlaceDetailActivity.class.getSimpleName();

    TextView phone_tv, web_tv, web2_tv, review_tv, contactDetails_tv, descriptionText_tv, statusSingle_tv;
    TextView ratingNum_tv, menuHeader_tv, menuInfo_tv, loadMoreReviews, streetView_tv, mapView_tv;
    TextView openingHoursHeader_tv, day0, day1, day2, day3, day4, day5, day6;
    TextView distance_tv, driving_tv, walking_tv;
    ImageView phone_iv, web_iv, web2_iv, direction_iv, person_iv, rating_iv, share_iv;
    ProgressDialog pDialog;
    String referenceFromIntent, nameFromIntent, addyFromIntent, disFromIntent, openingHoursStr, drivingTime, walkingTime;
    Double latFromIntent, lonFromIntent, oLatFromIntent, oLngFromIntent, mLastKnownLat, mLastKnownLng;
    String placeName, placePhone, placeWeb, placeWeb2, placeReview, placeAddress, placeReviewCombo, placeRating;
    String myUrl, reviewContent;
    String firstDayID;
    JSONArray jsonarrayReveiws, jsonarrayPeriods = new JSONArray();
    ArrayList<Review> listReviews = new ArrayList<Review>();
    @SuppressLint("UseSparseArrays")
    HashMap<Integer, String> hoursMap = new HashMap<Integer, String>();
    RatingBar ratingbar;
    boolean okay = true;
    Typeface tf;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single);
        receiveIntents();
        initToolBar();
        initializeAndHide();

        if (!Utils.isNetworkConnected(this)) {
            AlertDialogManager alert = new AlertDialogManager();
            alert.showAlertDialog(PlaceDetailActivity.this, getString(R.string.internet_error_title), getString(R.string.internet_error_message), false);
            return;
        }

        myUrl = makeSingleUrl(referenceFromIntent);
        //System.out.println(myUrl);

        if (savedInstanceState == null)
            new GetPlacesTask().execute();

    }

    private void receiveIntents() {

        referenceFromIntent = getIntent().getStringExtra("key_ref");
        nameFromIntent = getIntent().getStringExtra("key_name");
        addyFromIntent = getIntent().getStringExtra("key_addy");
        latFromIntent = Double.parseDouble(getIntent().getStringExtra("key_lat")); // convert string intent to double
        lonFromIntent = Double.parseDouble(getIntent().getStringExtra("key_lon")); // convert string intent to double
        disFromIntent = getIntent().getStringExtra("key_dis");
        oLatFromIntent = Double.parseDouble(getIntent().getStringExtra("key_oLat"));
        oLngFromIntent = Double.parseDouble(getIntent().getStringExtra("key_oLng"));

        GPSUtil gps = new GPSUtil(getApplicationContext());
        mLastKnownLat = MainActivity.LAST_KNOWN_LAT != null ? MainActivity.LAST_KNOWN_LAT : gps.getLastKnownLatitude();
        mLastKnownLng = MainActivity.LAST_KNOWN_LON != null ? MainActivity.LAST_KNOWN_LON : gps.getLastKnownLongitude();

    }

    private void initToolBar() {

        // initialize ToolBar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(false);
            getSupportActionBar().setTitle(nameFromIntent);
            getSupportActionBar().setSubtitle(addyFromIntent);

        }

        //getSupportActionBar().setTitle(Html.fromHtml("<font color=\"#071754\">" + nameFromIntent + "</font>"));
        //getSupportActionBar().setSubtitle(Html.fromHtml("<font color=\"#071754\">" + addyFromIntent + "</font>"));

    }

    private void initializeAndHide() {

        tf = Typeface.createFromAsset(getAssets(), "fonts/roboto_regular.ttf");

        (findViewById(R.id.powered_by_google_singleact)).setVisibility(View.GONE);

        distance_tv = (TextView) findViewById(R.id.textDistance);
        driving_tv = (TextView) findViewById(R.id.textDriving);
        driving_tv.setOnClickListener(this);
        walking_tv = (TextView) findViewById(R.id.textWalking);
        walking_tv.setOnClickListener(this);
        streetView_tv = (TextView) findViewById(R.id.streetView_tv);
        streetView_tv.setOnClickListener(this);
        mapView_tv = (TextView) findViewById(R.id.mapView_tv);
        mapView_tv.setOnClickListener(this);
        share_iv = (ImageView) findViewById(R.id.shareImageView1);
        share_iv.setOnClickListener(this);
        statusSingle_tv = (TextView) findViewById(R.id.statusSingle);
        statusSingle_tv.setTypeface(tf);


        contactDetails_tv = (TextView) findViewById(R.id.contactDetails1);
        contactDetails_tv.setTypeface(tf);
        contactDetails_tv.setText(getString(R.string.contact_details).toUpperCase(Locale.getDefault()));
        phone_tv = (TextView) findViewById(R.id.phone1);
        // phone_tv.setOnClickListener(this);
        web_tv = (TextView) findViewById(R.id.website1);
        web2_tv = (TextView) findViewById(R.id.website2);
        review_tv = (TextView) findViewById(R.id.reviews1);
        review_tv.setText(getString(R.string.reviews).toUpperCase(Locale.getDefault()));
        phone_iv = (ImageView) findViewById(R.id.imagePhone1);
        web_iv = (ImageView) findViewById(R.id.imageWeb1);
        web2_iv = (ImageView) findViewById(R.id.imageWeb2);
        rating_iv = (ImageView) findViewById(R.id.imageRating);
        person_iv = (ImageView) findViewById(R.id.person1);
        descriptionText_tv = (TextView) findViewById(R.id.description_text);
        descriptionText_tv.setOnClickListener(this);
        direction_iv = (ImageView) findViewById(R.id.directionimageView1);
        direction_iv.setOnClickListener(this);
        ratingNum_tv = (TextView) findViewById(R.id.ratingNumberTV);
        ratingbar = (RatingBar) findViewById(R.id.ratingbarSingle);

        // Not using menu for now
//		menuHeader_tv = (TextView) findViewById(R.id.menuHeader);
//		menuInfo_tv = (TextView) findViewById(R.id.menuInfo);
//		menuHeader_tv.setVisibility(View.GONE);
//		menuInfo_tv.setVisibility(View.GONE);

        // Opening Hours
        openingHoursHeader_tv = (TextView) findViewById(R.id.openingHours1);
        openingHoursHeader_tv.setText(getString(R.string.opening_hours).toUpperCase(Locale.getDefault()));
        day0 = (TextView) findViewById(R.id.sunday);
        day1 = (TextView) findViewById(R.id.monday);
        day2 = (TextView) findViewById(R.id.tuesday);
        day3 = (TextView) findViewById(R.id.wednesday);
        day4 = (TextView) findViewById(R.id.thursday);
        day5 = (TextView) findViewById(R.id.friday);
        day6 = (TextView) findViewById(R.id.saturday);

        loadMoreReviews = (TextView) findViewById(R.id.loadMoreReviews);


        // hide views
        findViewById(R.id.card_contact).setVisibility(View.GONE);
        findViewById(R.id.card_opening_hours).setVisibility(View.GONE);
        findViewById(R.id.card_reviews).setVisibility(View.GONE);

        driving_tv.setVisibility(View.GONE);
        distance_tv.setVisibility(View.GONE);
        walking_tv.setVisibility(View.GONE);

        review_tv.setVisibility(View.GONE);
        web2_tv.setVisibility(View.GONE);
        web_tv.setVisibility(View.GONE);
        phone_tv.setVisibility(View.GONE);
        phone_iv.setVisibility(View.GONE);
        web_iv.setVisibility(View.GONE);
        web2_iv.setVisibility(View.GONE);

        rating_iv.setVisibility(View.GONE);
        person_iv.setVisibility(View.GONE);
        ratingNum_tv.setVisibility(View.GONE);
        descriptionText_tv.setVisibility(View.GONE);
        ratingbar.setVisibility(View.GONE);
        loadMoreReviews.setVisibility(View.GONE);

        openingHoursHeader_tv.setVisibility(View.GONE);
        day6.setVisibility(View.GONE);
        day5.setVisibility(View.GONE);
        day4.setVisibility(View.GONE);
        day3.setVisibility(View.GONE);
        day2.setVisibility(View.GONE);
        day1.setVisibility(View.GONE);
        day0.setVisibility(View.GONE);

    }

    private String makeSingleUrl(String reference) {
        // https://maps.googleapis.com/maps/api/place/details/json?reference=Cm??????oTH3g&sensor=true&key=AddYourOwnKeyHere

        StringBuilder urlString;

        urlString = new StringBuilder("https://maps.googleapis.com/maps/api/place/details/json?");
        urlString.append("reference=");
        urlString.append(reference);
        urlString.append("&sensor=false&key=" + Configuration.getApiKey());

        return urlString.toString();
    }

    private String getUrlContents(String theUrl) {

        //theUrl = "http://...."
        StringBuilder content = new StringBuilder();
        try {
            URL url = new URL(theUrl);
            URLConnection urlConnection = url.openConnection();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), "UTF-8"), 8);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                content.append(line + "\n");
            }
            bufferedReader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return content.toString();
    }

    private void updateAndShowViews() {


        (findViewById(R.id.powered_by_google_singleact)).setVisibility(View.VISIBLE);

        SharedPreferences spref = PreferenceManager.getDefaultSharedPreferences(PlaceDetailActivity.this);
        String str = spref.getString("distanceUnit", "NuLL");
        String unit;

        if (str.matches("kmValue")) {
            unit = getString(R.string.km);
        } else {
            unit = getString(R.string.mi);
        }

        // called onPostExecute AsyncTask to ensure we have values to work with
        if (disFromIntent != null) {
            distance_tv.setVisibility(View.VISIBLE);
            distance_tv.setText(disFromIntent + " " + unit);
            distance_tv.setTypeface(tf);
        }

        if (drivingTime != null) {
            driving_tv.setVisibility(View.VISIBLE);
            driving_tv.setText(drivingTime);
            driving_tv.setTypeface(tf);
        }

        if (walkingTime != null) {
            walking_tv.setVisibility(View.VISIBLE);
            walking_tv.setText(walkingTime);
            walking_tv.setTypeface(tf);
        }

        if (placePhone == null && placeWeb == null && placeWeb2 == null) {
            statusSingle_tv.setText(getString(R.string.further_details_unavailable));
            statusSingle_tv.setTypeface(tf);
        } else {
            statusSingle_tv.setVisibility(View.GONE);
            statusSingle_tv.setTypeface(tf);
        }

        if (placePhone != null) {
            findViewById(R.id.card_contact).setVisibility(View.VISIBLE);
            phone_tv.setText(placePhone);
            phone_tv.setVisibility(View.VISIBLE);
            phone_tv.setOnClickListener(this);
            phone_iv.setVisibility(View.VISIBLE);
            phone_tv.setTypeface(tf);
        }

        if (placeWeb != null) {
            findViewById(R.id.card_contact).setVisibility(View.VISIBLE);
            web_tv.setVisibility(View.VISIBLE);
            web_iv.setVisibility(View.VISIBLE);
            web_tv.setText(placeWeb);
            web_tv.setOnClickListener(this);
            web_tv.setTypeface(tf);
        }

        if (placeWeb2 != null) {
            findViewById(R.id.card_contact).setVisibility(View.VISIBLE);
            web2_tv.setText(placeWeb2);
            web2_tv.setVisibility(View.VISIBLE);
            web2_tv.setOnClickListener(this);
            web2_iv.setVisibility(View.VISIBLE);
            web2_tv.setTypeface(tf);
        }

        if (placeRating != null && reviewContent != null) {
            findViewById(R.id.card_reviews).setVisibility(View.VISIBLE);
            // show rating icon
            rating_iv.setVisibility(View.VISIBLE);
            rating_iv.setOnClickListener(this);

            // update and show rating number TextView
            ratingNum_tv.setText(placeRating);
            ratingNum_tv.setVisibility(View.VISIBLE);
            ratingNum_tv.setTypeface(tf);

            // update and show ratingBar
            ratingbar.setRating(Float.parseFloat(placeRating));
            ratingbar.setVisibility(View.VISIBLE);

        }

        if (reviewContent != null) {
            findViewById(R.id.card_reviews).setVisibility(View.VISIBLE);
            // show review TextView divider
            review_tv.setVisibility(View.VISIBLE);
            review_tv.setTypeface(tf);
            // update and show descriptionText_tv TextView
            descriptionText_tv.setText(placeReviewCombo);
            descriptionText_tv.setVisibility(View.VISIBLE);
            descriptionText_tv.setTypeface(tf);

            person_iv.setVisibility(View.VISIBLE);

            loadMoreReviews.setVisibility(View.VISIBLE);
            loadMoreReviews.setTypeface(tf);
            loadMoreReviews.setOnClickListener(this);

            //rePositionPoweredByGoogle();

        }

        if (okay && openingHoursStr != null && (jsonarrayPeriods.length() == 6 || jsonarrayPeriods.length() == 7)) {

            findViewById(R.id.card_opening_hours).setVisibility(View.VISIBLE);

            Log.d(TAG, "finally in bro");

            // retrieve string from HashMap
            day0.setText(hoursMap.get(0));
            day1.setText(hoursMap.get(1));
            day2.setText(hoursMap.get(2));
            day3.setText(hoursMap.get(3));
            day4.setText(hoursMap.get(4));
            day5.setText(hoursMap.get(5));
            day6.setText(hoursMap.get(6));

            openingHoursHeader_tv.setVisibility(View.VISIBLE);
            day0.setVisibility(View.VISIBLE);
            day1.setVisibility(View.VISIBLE);
            day2.setVisibility(View.VISIBLE);
            day3.setVisibility(View.VISIBLE);
            day4.setVisibility(View.VISIBLE);
            day5.setVisibility(View.VISIBLE);
            day6.setVisibility(View.VISIBLE);

            getWeekDay();

        }

    }

    private static String padRight(String s, int n) {
        return String.format("%1$-" + n + "s", s);
    }

    private void getWeekDay() {
        // Called on updateAndShowViews only if openingHours is available

        Calendar date = Calendar.getInstance();
        int day = date.get(Calendar.DAY_OF_WEEK);

        switch (day) {
            case 1:
                // Sunday
                day0.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
                day0.setTextColor(getResources().getColor(R.color.colorAccent));
                break;

            case 2:
                // Monday
                day1.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
                day1.setTextColor(getResources().getColor(R.color.colorAccent));
                break;

            case 3:
                // Tuesday
                day2.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
                day2.setTextColor(getResources().getColor(R.color.colorAccent));
                break;

            case 4:
                // Wednesday
                day3.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
                day3.setTextColor(getResources().getColor(R.color.colorAccent));
                break;

            case 5:
                // Thursday
                day4.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
                day4.setTextColor(getResources().getColor(R.color.colorAccent));
                break;

            case 6:
                // Friday
                day5.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
                //day5.setBackgroundColor(Color.rgb(197, 238, 250));
                day5.setTextColor(getResources().getColor(R.color.colorAccent));
                break;

            case 7:
                // Saturday
                day6.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
                //day6.setBackgroundColor(getResources().getColor(R.color.colorAccent));
                day6.setTextColor(getResources().getColor(R.color.colorAccent));
                break;
        }
    }

    private String getAMPM(String time) {
        // called in AsyncTask background to set 12 hour time
        // Time must be in the String format e.g String time = "2101";
        Calendar date = Calendar.getInstance();
        // Calendar.HOUR_OF_DAY is in 24-hour format
        date.set(Calendar.HOUR_OF_DAY, Integer.parseInt(time.substring(0, 2)));
        // Calendar.HOUR is in 12-hour format but let's format it for display purposes only since we want 09:00 and not 9:00
        String hour = String.format("%02d", date.get(Calendar.HOUR));

        // time.get(Calendar.MINUTE) returns the exact minute integer e.g for 10:04 will show 10:4
        // For display purposes only We could just return the last two substring or format Calender.MINUTE as shown below
        date.set(Calendar.MINUTE, Integer.parseInt(time.substring(2, 4)));
        String minute = String.format("%02d", date.get(Calendar.MINUTE));

        // time.get(Calendar.AM_PM) returns integer 0 or 1 so let's set the right String value
        String AM_PM = date.get(Calendar.AM_PM) == 0 ? "AM" : "PM";

        String result = hour + ":" + minute + " " + AM_PM;

        // fixing 0:01 PM
        if (date.get(Calendar.HOUR) == 0 && date.get(Calendar.AM_PM) == 1) {
            result = ("12" + ":" + minute + " " + AM_PM);
        }

        // Calendar.HOUR is in 12-hour format
        return result;
    }

    private String getTextToShare() {

        StringBuilder sb = new StringBuilder();
        if (placeName != null) {
            sb.append("Name: " + placeName);
        }
        if (placeAddress != null) {
            sb.append("\n" + getString(R.string.address) + ": " + placeAddress);
        }
        if (placePhone != null) {
            sb.append("\n" + getString(R.string.phone) + ": " + placePhone);
        }
        if (placeWeb != null) {
            sb.append("\n" + getString(R.string.website) + ": " + placeWeb);
        }
        if (placeWeb2 != null) {
            sb.append("\n" + getString(R.string.url) + ": " + placeWeb2);
        }

        sb.append("\n...\n" + getString(R.string.shared_from) + " " + getString(R.string.app_name) + " Android App");
        sb.append("\n" + "http://play.google.com/store/apps/details?id=" + getPackageName());

        return sb.toString();

    }


    private class GetPlacesTask extends AsyncTask<Void, Void, Void> {
        String jsonResult;
        JSONObject jsonobject;

        @Override
        protected Void doInBackground(Void... params) {

            DirectionManager dm = new DirectionManager(PlaceDetailActivity.this);
            drivingTime = dm.getTravelTime(oLatFromIntent, oLngFromIntent, latFromIntent, lonFromIntent, "driving");
            walkingTime = dm.getTravelTime(oLatFromIntent, oLngFromIntent, latFromIntent, lonFromIntent, "walking");
            //	Log.d("dTWalk", "d and w time: " + drivingTime + " " + walkingTime);

            jsonResult = getUrlContents(myUrl);
            try {
                jsonobject = new JSONObject(jsonResult).getJSONObject("result");
                jsonarrayReveiws = jsonobject.getJSONArray("reviews");
                jsonarrayPeriods = jsonobject.getJSONObject("opening_hours").getJSONArray("periods");
            } catch (JSONException e) {
                // TODO Auto-generated catch block.
                e.printStackTrace();
            }

            try {
                placeName = jsonobject.getString("name");
                placeAddress = jsonobject.getString("formatted_address");
                placePhone = jsonobject.getString("formatted_phone_number");
                placeWeb = jsonobject.getString("website");
                placeWeb2 = jsonobject.optString("url", null);
                placeRating = jsonobject.getString("rating");
                reviewContent = jsonobject.getString("reviews"); // used ONLY to ensure value is not null

                if (reviewContent != null) {
                    StringBuilder sb = new StringBuilder();
                    HashMap<String, String> map;

                    for (int i = 0; i < jsonarrayReveiws.length(); i++) {
                        String reviewRating = jsonarrayReveiws.getJSONObject(i).getString("rating");
                        String reviewAuthor = jsonarrayReveiws.getJSONObject(i).getString("author_name");
                        String reviewTime = jsonarrayReveiws.getJSONObject(i).getString("time");
                        String reviewText = jsonarrayReveiws.getJSONObject(i).getString("text").replace("&#39;", "'").replace("&amp;#39;", "'");

                        long timestamp = Long.parseLong(reviewTime);

                        Date convertedTime = new Date((long) timestamp * 1000);
                        // System.out.println(convertedTime);

                        String reviewTotal = reviewAuthor + "\n" + convertedTime + "\n" + reviewText + "\n\n";

                        sb.append(reviewTotal);

                        map = new HashMap<String, String>();
                        map.put("reviewRating", reviewRating);
                        map.put("reviewAuthor", reviewAuthor);
                        map.put("reviewTime", convertedTime + "");
                        map.put("reviewText", reviewText);

                        listReviews.add(new Review(reviewAuthor + "\n" + convertedTime, reviewText, reviewRating));
                    }

                    placeReviewCombo = sb.toString();
                }

                openingHoursStr = jsonobject.optString("opening_hours", null); // used ONLY to ensure value is not null
                // System.out.println(openingHoursStr);
                if (openingHoursStr == null) {
                    return null;
                }
                firstDayID = jsonarrayPeriods.getJSONObject(0).getJSONObject("open").optString("day", null);
                final int count = jsonarrayPeriods.length();
                if (!openingHoursStr.matches("blank") && firstDayID != null && (jsonarrayPeriods.length() == 6 || jsonarrayPeriods.length() == 7)) {
                    // NB assigning fixed width
                    String[] days = {getString(R.string.sunday), getString(R.string.monday), getString(R.string.tuesday), getString(R.string.wednesday),
                            getString(R.string.thursday), getString(R.string.friday), getString(R.string.saturday)};
                    // String[] dayIDArray = { "0", "1", "2", "3", "4", "5", "6" }; // trying to match jsonStrings returned for dayID
                    String Closed = getString(R.string.closed);
                    String str = null, closeTime, openTime;
                    String lastDayID = jsonarrayPeriods.getJSONObject(count - 1).getJSONObject("open").optString("day", null); // n.b 5.

                    for (int i = 0; i < jsonarrayPeriods.length(); i++) {
                        closeTime = jsonarrayPeriods.getJSONObject(i).getJSONObject("close").getString("time");
                        openTime = jsonarrayPeriods.getJSONObject(i).getJSONObject("open").getString("time");
                        // dayID = jsonarrayPeriods.getJSONObject(i).getJSONObject("open").optString("day", null); //using open time for
                        // accuracy

                        if (firstDayID.matches("1") && lastDayID.matches("6") && count == 6) {
                            // Sunday closed
                            hoursMap.put(0, padRight("Sunday", 11) + padRight(Closed, 19));
                            str = padRight(days[i + 1], 11) + getAMPM(openTime) + " - " + getAMPM(closeTime);
                            hoursMap.put(i + 1, str);
                        } else if (firstDayID.matches("0") && lastDayID.matches("5") && count == 6) {
                            // Saturday closed
                            if (i != 6) {
                                str = padRight(days[i], 11) + getAMPM(openTime) + " - " + getAMPM(closeTime);
                                hoursMap.put(i, str);
                            }
                            hoursMap.put(6, padRight("Saturday", 11) + padRight(Closed, 19));
                        } else if (firstDayID.matches("0") && lastDayID.matches("6") && count == 7) {
                            // all open
                            str = padRight(days[i], 11) + getAMPM(openTime) + " - " + getAMPM(closeTime);
                            hoursMap.put(i, str);
                        } else {
                            okay = false; // to prevent initializing textView
                        }

                        if (okay) {
                            //System.out.println(str);
                            //System.out.println("Array jsonarrayPeriods Count: " + count);
                        }

                    }
                }

            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            // TODO Auto-generated method sR.id.directionimageView1tub
            super.onPostExecute(result);
            //System.out.println(jsonResult);

            updateAndShowViews();

        }
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.phone1:
                Intent dialIntent = new Intent("android.intent.action.DIAL");
                dialIntent.setData(Uri.parse("tel:" + phone_tv.getText().toString().trim()));

                // best practice to check if activity to handle text Action_Send intent is present
                PackageManager packageManager = getPackageManager();
                List<ResolveInfo> activitiesAvailable = packageManager.queryIntentActivities(dialIntent, 0);
                boolean isSafe = activitiesAvailable.size() > 0;

                if (isSafe) {
                    // if safe continue
                    startActivity(dialIntent);
                    overridePendingTransition(R.anim.right_slide_in, R.anim.left_slide_out); // for starting activity only
                }


                break;

            case R.id.website1:
                String url = web_tv.getText().toString().trim();
                Intent webIntent = new Intent(Intent.ACTION_VIEW);
                webIntent.setData(Uri.parse(url));
                startActivity(webIntent);
                overridePendingTransition(R.anim.right_slide_in, R.anim.left_slide_out); // for starting activity only
                break;

            case R.id.website2:
                String url2 = web2_tv.getText().toString().trim();
                Intent webIntent2 = new Intent(Intent.ACTION_VIEW);
                webIntent2.setData(Uri.parse(url2));
                startActivity(webIntent2);
                overridePendingTransition(R.anim.right_slide_in, R.anim.left_slide_out); // for starting activity only
                break;

            case R.id.loadMoreReviews:
                if (reviewContent != null) {
                    Intent i = new Intent(PlaceDetailActivity.this, ReviewsActivity.class);
                    i.putExtra("arrayListFromIntent", listReviews);
                    i.putExtra("placeName", placeName);
                    i.putExtra("placeAddress", placeAddress);
                    startActivity(i);
                }
                break;

            case R.id.shareImageView1:

                if (placeName != null) {
                    // if there's something to display, continue
                    String str = getTextToShare();

                    Intent i = new Intent(Intent.ACTION_SEND);
                    i.setType("text/plain");
                    i.putExtra(Intent.EXTRA_TEXT, str);

                    // best practice to check if activity to handle text Action_Send intent is present
                    List<ResolveInfo> activities = getPackageManager().queryIntentActivities(i, 0);
                    boolean isIntentSafe = activities.size() > 0;

                    if (isIntentSafe) {
                        // if safe continue
                        startActivity(i);

                    }

                }

                break;

            case R.id.directionimageView1:

                DirectionManager dm = new DirectionManager(PlaceDetailActivity.this);
                dm.performAction(R.drawable.direction_btn, "d", latFromIntent, lonFromIntent, oLatFromIntent, oLngFromIntent);

                break;

            case R.id.streetView_tv:
                Intent si = new Intent(PlaceDetailActivity.this, StreetViewActivity.class);
                si.putExtra("key_name", nameFromIntent);
                si.putExtra("key_addy", addyFromIntent);
                si.putExtra("key_lat", String.valueOf(latFromIntent));
                si.putExtra("key_lon", String.valueOf(lonFromIntent));
                startActivity(si);
                overridePendingTransition(R.anim.right_slide_in, R.anim.left_slide_out); // for starting activity only
                break;

            case R.id.mapView_tv:
                Intent mi = new Intent(PlaceDetailActivity.this, MapActivitySingle.class);
                mi.putExtra("key_name", nameFromIntent);
                mi.putExtra("key_addy", addyFromIntent);
                mi.putExtra("key_lat", String.valueOf(latFromIntent));
                mi.putExtra("key_lon", String.valueOf(lonFromIntent));
                mi.putExtra("getLat", oLatFromIntent);
                mi.putExtra("getLng", oLngFromIntent);
                startActivity(mi);
                overridePendingTransition(R.anim.right_slide_in, R.anim.left_slide_out); // for starting activity only
                break;

            case R.id.textDriving:
                DirectionManager dm1 = new DirectionManager(PlaceDetailActivity.this);
                dm1.performAction(R.drawable.ic_map_car, "d", latFromIntent, lonFromIntent, oLatFromIntent, oLngFromIntent);
                break;

            case R.id.textWalking:
                DirectionManager dm2 = new DirectionManager(PlaceDetailActivity.this);
                dm2.performAction(R.drawable.ic_map_walking, "w", latFromIntent, lonFromIntent, oLatFromIntent, oLngFromIntent);
                break;
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // TODO Auto-generated method stub
        switch (item.getItemId()) {

            case android.R.id.home:
                this.finish();
                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        // TODO Auto-generated method stub
        super.onBackPressed();
        this.finish();
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("okay", okay);
        outState.putString("disFromIntent", disFromIntent);
        outState.putString("drivingTime", drivingTime);
        outState.putString("walkingTime", walkingTime);
        outState.putString("placeName", placeName);
        outState.putString("placeAddress", placeAddress);
        outState.putString("placePhone", placePhone);
        outState.putString("placeWeb", placeWeb);
        outState.putString("placeWeb2", placeWeb2);
        outState.putString("placeRating", placeRating);
        outState.putString("placeReviewCombo", placeReviewCombo);
        outState.putString("reviewContent", reviewContent);
        outState.putString("openingHoursStr", openingHoursStr);
        outState.putSerializable("listReviews", listReviews);
        outState.putSerializable("hoursMap", hoursMap);
        outState.putString("jsonarrayPeriods", jsonarrayPeriods.toString());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        okay = (boolean) savedInstanceState.getBoolean("okay");
        disFromIntent = (String) savedInstanceState.get("disFromIntent");
        drivingTime = (String) savedInstanceState.get("drivingTime");
        walkingTime = (String) savedInstanceState.get("walkingTime");
        placeName = (String) savedInstanceState.get("placeName");
        placeAddress = (String) savedInstanceState.get("placeAddress");
        placePhone = (String) savedInstanceState.get("placePhone");
        placeWeb = (String) savedInstanceState.get("placeWeb");
        placeWeb2 = (String) savedInstanceState.get("placeWeb2");
        placeRating = (String) savedInstanceState.get("placeRating");
        placeReviewCombo = (String) savedInstanceState.get("placeReviewCombo");
        reviewContent = (String) savedInstanceState.get("reviewContent");
        openingHoursStr = (String) savedInstanceState.get("openingHoursStr");
        listReviews = (ArrayList<Review>) savedInstanceState.getSerializable("listReviews");
        hoursMap = (HashMap<Integer, String>) savedInstanceState.getSerializable("hoursMap");
        try {
            String s = (String) savedInstanceState.get("jsonarrayPeriods");
            jsonarrayPeriods = new JSONArray(s);
            Log.d(TAG, jsonarrayPeriods.length() + " <-finally-> "+ jsonarrayPeriods.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        updateAndShowViews();
    }
}
