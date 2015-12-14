package com.ojiofong.arounda.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.ojiofong.arounda.PlaceHelper;
import com.ojiofong.arounda.R;
import com.ojiofong.arounda.adapter.LazyAdapter;
import com.ojiofong.arounda.data.Place;
import com.ojiofong.arounda.utils.AlertDialogManager;
import com.ojiofong.arounda.utils.Configuration;
import com.ojiofong.arounda.utils.GPSUtil;
import com.ojiofong.arounda.utils.Utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Locale;

public class PlaceListActivity extends AppCompatActivity {


    public static final String TAG = PlaceListActivity.class.getSimpleName();

    private static ArrayList<Place> findPlaces;
    protected static ArrayList<HashMap<String, String>> placesListItems = new ArrayList<>();
    Boolean usingTextSearch = false;
    AlertDialogManager alert = new AlertDialogManager();
    TextView btnLoadMore;
    String pagetoken;
    private String searchTextValue = null;
    private static int current_page = 0; // Flag for current page
    // private static final int earthRadius = 6371;
    LazyAdapter adapter;
    ListView lv;
    TextView defaultText;
    View footerView;
    Place currentPlace;
    ImageView defaultIV;
    Double mLastKnownLat, mLastKnownLng;

    // KEY Strings
    public static final String KEY_REFERENCE = "reference"; // id of the place
    public static final String KEY_NAME = "name"; // name of the place
    public static final String KEY_RATING = "rating"; // rating of the place
    public static final String KEY_ADDRESS = "formatted_address"; // Place area name
    public static final String KEY_DISTANCE = "distance"; // distance
    public static final String KEY_LATITUDE = "latitude"; // latitude
    public static final String KEY_LONGITUDE = "longitude"; // longitude
    public static final String KEY_TIME = "time"; //travel time
    public static final String KEY_PHONE = "formatted_phone_number"; // Place phone


    private static final String KEY_LIST = "key_list_of_places";
    private static boolean shouldHideLoadMoreButton;
//    public static final String TEXT_SEARCH = "text_search";
//    public static final String PLACE_SEARCH = "place_search";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_list);
        //new AppManager(this).setStatusBarColorForKitKat(getResources().getColor(R.color.colorPrimaryDark));

        initToolBar();
        initialize();
        receiveIntents();

        if (!checkConnections())
            return;

        if (searchTextValue == null) {
            //  go();
            usingTextSearch = false;
        } else {
            // performTextSearch();
            usingTextSearch = true;
            if (getSupportActionBar() != null)
                getSupportActionBar().setTitle(searchTextValue);
        }

        if (savedInstanceState == null) {
            // reset page token and ArrayList
            // Load places from network
            shouldHideLoadMoreButton = false;
            resetPageTokenAndPlacesList();
            new LoadPlaces().execute();
        }

    }

    private void receiveIntents() {
        searchTextValue = getIntent().getStringExtra("searchQuery");

        GPSUtil gps = new GPSUtil(getApplicationContext());
        mLastKnownLat = MainActivity.LAST_KNOWN_LAT != null ? MainActivity.LAST_KNOWN_LAT : gps.getLastKnownLatitude();
        mLastKnownLng = MainActivity.LAST_KNOWN_LON != null ? MainActivity.LAST_KNOWN_LON : gps.getLastKnownLongitude();

    }

    private boolean checkConnections() {

        if (!Utils.isNetworkConnected(this)) {
            // Internet Connection is not present
            alert.showAlertDialog(this, getString(R.string.internet_error_title), getString(R.string.internet_error_message), false);
            return false;
        }


        GPSUtil gpsUtil = new GPSUtil(this);
        if (!gpsUtil.canGetLocation()) {
            gpsUtil.showSettingsAlert();
            return false;
        }

        return true;

    }


    private void initToolBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_NextActivity);
        if (toolbar != null) {
            toolbar.setTitle(MainActivity.clickedName);
            setSupportActionBar(toolbar);
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(false);
        }
    }

    @SuppressLint("InflateParams")
    private void initialize() {

        defaultIV = (ImageView) findViewById(R.id.defaultIV);
        defaultIV.setVisibility(View.GONE);

        defaultText = (TextView) findViewById(R.id.defaultText);
        defaultText.setVisibility(View.GONE);

        lv = (ListView) findViewById(R.id.list);

        footerView = getLayoutInflater().inflate(R.layout.load_more_row, null, false);
        lv.addFooterView(footerView);
        // (footerView.findViewById(R.id.powered_by_google_loadmore)).setVisibility(View.VISIBLE);
        //footerView.setVisibility(View.GONE);
        btnLoadMore = (TextView) footerView.findViewById(R.id.loadMore);

        /**
         * Listening to Load More button click event
         * */
        btnLoadMore.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                // Starting a new async task
                new LoadPlaces().execute();

                // Reset spinner to initial position
                Spinner spinner = (Spinner) findViewById(R.id.spinner_nav);
                spinner.setSelection(0);
            }
        });


        lv.setOnScrollListener(new OnScrollListener() {
            private int mLastFirstVisibleItem;

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

                if (mLastFirstVisibleItem < firstVisibleItem) {
                    Log.i("SCROLLING DOWN", "TRUE");
                }
                if (mLastFirstVisibleItem > firstVisibleItem) {
                    Log.i("SCROLLING UP", "TRUE");
                }
                mLastFirstVisibleItem = firstVisibleItem;

            }
        });
    }

    private void performTextSearch() {


    }

    private void go() {

        if (!Utils.isNetworkConnected(this)) {
            // if Internet is not present
            alert.showAlertDialog(PlaceListActivity.this, getString(R.string.internet_error_title), getString(R.string.internet_error_message), false);
        } else {

            GPSUtil gpsUtil = new GPSUtil(this);

            // check if GPS is enabled
            if (gpsUtil.canGetLocation()) {
                /*
                 * double latitude = gpsUtil.getLastKnownLatitude(); double longitude =
				 * gpsUtil.getLastKnownLongitude();
				 */
                usingTextSearch = false;

                // reset page token and list
                resetPageTokenAndPlacesList();

                btnLoadMore.setVisibility(View.VISIBLE);

                // call async search function
                new LoadPlaces().execute();

            } else {
                // can't get location
                // GPS or Network is not enabled
                // Ask user to enable GPS/network in settings
                gpsUtil.showSettingsAlert();
            }
        }

    }

    private void resetPageTokenAndPlacesList() {
        // reset page token and list
        SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", 0); // 0-for private mode
        Editor editor = pref.edit();
        editor.clear();
        editor.apply();
        placesListItems = new ArrayList<>();
    }

    public double calculateDistance(double lat1, double lon1, double lat2, double lon2) {

        float[] results = new float[3];
        Location.distanceBetween(lat1, lon1, lat2, lon2, results);

        BigDecimal bd = new BigDecimal(results[0]); // results in meters
        BigDecimal bdRounded = bd.setScale(2, RoundingMode.HALF_UP);

        double distance = bdRounded.doubleValue(); // distance in meters

        SharedPreferences spref = PreferenceManager.getDefaultSharedPreferences(PlaceListActivity.this);
        String str = spref.getString("distanceUnit", "NuLL");

        if (str.matches("milesValue")) {
            // Convert distance to Miles
            distance = (Double) (distance * 0.000621371f); // convert m to Miles
            bd = new BigDecimal(distance);
            bdRounded = bd.setScale(2, RoundingMode.HALF_UP);
            distance = bdRounded.doubleValue();

            return distance;

        } else if (str.matches("kmValue")) {
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

    private Double getLat() {
        return new GPSUtil(this).getAppLat();
    }

    private Double getLng() {
        return new GPSUtil(this).getAppLng();
    }

    private class LoadPlaces extends AsyncTask<String, String, String> {

        protected void onPreExecute() {
            super.onPreExecute();
            (findViewById(R.id.progressBarLayout)).setVisibility(View.VISIBLE);
        }

        /**
         * getting Places JSON
         */
        protected String doInBackground(String... args) {

            PlaceHelper placeHelper = new PlaceHelper(Configuration.getApiKey());

            //get the placeID directly no need for switch statement or loop
            String types = MainActivity.clickedPlaceID;

            try {

                if (current_page > 0) {
                    pagetoken = getPageToken();
                }

                // Radius in meters - increase this value if you don't find any places
                double radius = 1000; // radius in meters
                if (types != null)
                    types = types.toLowerCase(Locale.ENGLISH);

//                if (usingTextSearch) { // if using searchBar

                findPlaces = placeHelper.findPlaces(getLat(), getLng(), types, radius, usingTextSearch, searchTextValue, pagetoken, getApplicationContext());

//                } else { // not using searchBar
//
//                    findPlaces = placeHelper.findPlaces(getLat(), getLng(), types, radius, usingTextSearch, searchTextValue, pagetoken, getApplicationContext());
//                }

                // increment current page
                current_page += 1;

            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        protected void onPostExecute(String file_url) {

            if (findPlaces != null)
                if (findPlaces.size() == 0 && !usingTextSearch && MainActivity.clickedName != null && current_page == 1) {

                    // If place search found no data nearby
                    // Expand search by using the textSearch with a wide radius
                    // Here we are simulating a searchQuery

                    //Eliminating double words for text search
                    if (MainActivity.clickedPlaceID.matches("grocery_or_supermarket")) {
                        searchTextValue = getString(R.string.supermarkets);
                    } else if (MainActivity.clickedPlaceID.matches("gym")) {
                        searchTextValue = getString(R.string.gym_fitness).substring(0, getString(R.string.gym_fitness).indexOf("/"));
                    } else if (MainActivity.clickedPlaceID == "train_station|subway_station") {
                        searchTextValue = getString(R.string.train_subway).substring(0, getString(R.string.train_subway).indexOf("/"));
                    } else {
                        searchTextValue = MainActivity.clickedName;
                    }

                    performTextSearch();

                    // Notify the user that this search is unusual
                    new Handler().postDelayed(new Runnable() {

                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), getString(R.string.unfiltered_search_result), Toast.LENGTH_LONG).show();
                        }
                    }, 1000);

                    return;
                }

            if (findPlaces != null) {

                if (pagetoken == "empty") {
                    shouldHideLoadMoreButton = true;
                    btnLoadMore.setVisibility(View.GONE);
                    Toast.makeText(getApplicationContext(), getString(R.string.no_more_data), Toast.LENGTH_SHORT).show();
                } else {

                    // Fetch more places and add to ArrayList

                    for (int i = 0; i < findPlaces.size(); i++) {
                        HashMap<String, String> map = new HashMap<String, String>();
                        Place placeDetail = findPlaces.get(i);

                        // Place reference is used to get "place full details"
                        map.put(KEY_REFERENCE, placeDetail.getReference());
                        // Place name
                        map.put(KEY_NAME, placeDetail.getName());
                        // Place rating
                        map.put(KEY_RATING, placeDetail.getRating());
                        // Place address
                        map.put(KEY_ADDRESS, placeDetail.getAddress());
                        // Place latitude
                        map.put(KEY_LATITUDE, placeDetail.getLatitude().toString());
                        // Place longitude
                        map.put(KEY_LONGITUDE, placeDetail.getLongitude().toString());
                        // Distance from current location
                        map.put(KEY_DISTANCE, calculateDistance(getLat(), getLng(), placeDetail.getLatitude(), placeDetail.getLongitude()) + "");

                        placesListItems.add(map);

                    }

                    // set adapter
                    adapter = new LazyAdapter(getApplicationContext(), lv, PlaceListActivity.this, placesListItems);
                    lv.setAdapter(adapter);

                    // Make FooterView visible
                    defaultText.setVisibility(View.GONE);
                    defaultIV.setVisibility(View.GONE);
                    footerView.setVisibility(View.VISIBLE);
                    (footerView.findViewById(R.id.powered_by_google_loadmore)).setVisibility(View.VISIBLE);

                }
            } else {
                //findPlaces is null
                // show the default text that says sorry no data found
                defaultText.setVisibility(View.VISIBLE);
                defaultIV.setVisibility(View.VISIBLE);
                footerView.setVisibility(View.GONE);
            }

            if (usingTextSearch) {
                autoSortByDistance();
            }

            //dismiss the progressBar at the end
            (findViewById(R.id.progressBarLayout)).setVisibility(View.GONE);

        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.next, menu);
        activateSpinner();

        return super.onCreateOptionsMenu(menu);
    }

    private void autoSortByDistance() {

        Collections.sort(placesListItems, new Comparator<HashMap<String, String>>() {

            @Override
            public int compare(HashMap<String, String> lhs, HashMap<String, String> rhs) {
                Double firstValue = Double.valueOf(lhs.get(KEY_DISTANCE));
                Double secondValue = Double.valueOf(rhs.get(KEY_DISTANCE));

                return firstValue.compareTo(secondValue);
            }

        });

        // Re-set sorted list view
        adapter = new LazyAdapter(getApplicationContext(), lv, PlaceListActivity.this, placesListItems);
        lv.setAdapter(adapter);

    }

    private String getPageToken() {
        SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", 0);
        return pref.getString("next_page_token", null);
    }

    private void activateSpinner() {
        //Done programmatically to getThemedContext.
        //Context mThemeContext = new ContextThemeWrapper(this, R.style.AppTheme);
        Context mThemeContext = getSupportActionBar().getThemedContext();

        String[] items = getResources().getStringArray(R.array.action_list);
        Spinner spinner = (Spinner) findViewById(R.id.spinner_nav);
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(mThemeContext, android.R.layout.simple_spinner_item, items); //selected item will look like a spinner set from XML
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerAdapter);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            spinner.setPopupBackgroundResource(R.color.colorAccent);
        }

        spinner.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // TODO Auto-generated method stub
                selectSpinnerItem(position);

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // TODO Auto-generated method stub

            }
        });
    }

    private boolean selectSpinnerItem(int itemPosition) {

        switch (itemPosition) {
            // case 0:
            // break;

            case 1:
                // Distance
                Collections.sort(placesListItems, new Comparator<HashMap<String, String>>() {

                    @Override
                    public int compare(HashMap<String, String> lhs, HashMap<String, String> rhs) {
                        Double firstValue = Double.valueOf(lhs.get(KEY_DISTANCE));
                        Double secondValue = Double.valueOf(rhs.get(KEY_DISTANCE));

                        return firstValue.compareTo(secondValue);
                    }

                });
                break;

            case 2:
                // Name
                Collections.sort(placesListItems, new Comparator<HashMap<String, String>>() {

                    @Override
                    public int compare(HashMap<String, String> lhs, HashMap<String, String> rhs) {
                        String firstValue = String.valueOf(lhs.get(KEY_NAME));
                        String secondValue = String.valueOf(rhs.get(KEY_NAME));

                        return firstValue.compareToIgnoreCase(secondValue);
                    }
                });
                break;

            case 3:
                // Rating
                Collections.sort(placesListItems, new Comparator<HashMap<String, String>>() {

                    @Override
                    public int compare(HashMap<String, String> lhs, HashMap<String, String> rhs) {
                        Double firstValue = Double.valueOf(lhs.get(KEY_RATING));
                        Double secondValue = Double.valueOf(rhs.get(KEY_RATING));

                        return secondValue.compareTo(firstValue);
                    }
                });
                break;
        }

        // Re-set sorted list view
        adapter = new LazyAdapter(getApplicationContext(), lv, PlaceListActivity.this, placesListItems);
        lv.setAdapter(adapter);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case android.R.id.home:
                onBackPressed();
                finish();
                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right); // exit animation
                break;

            case R.id.action_mapview:

                Intent i = new Intent(PlaceListActivity.this, MapActivity.class);
                i.putExtra("actionTitle", getSupportActionBar().getTitle().toString());
                i.putExtra("key", GoogleMap.MAP_TYPE_NORMAL);
                startActivity(i);
                overridePendingTransition(R.anim.right_slide_in, R.anim.left_slide_out); // for starting activity only

                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        // TODO Auto-generated method stub
        super.onBackPressed();
        finish();
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(KEY_LIST, placesListItems);
    }


    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        ArrayList<HashMap<String, String>> restoredList = (ArrayList<HashMap<String, String>>) savedInstanceState.getSerializable(KEY_LIST);
        if (restoredList != null) {
//            Log.d(TAG, "restored finally: " + restoredList.toString());
            placesListItems = restoredList;
            adapter = new LazyAdapter(getApplicationContext(), lv, PlaceListActivity.this, placesListItems);
            lv.setAdapter(adapter);

            // Be sure to keep the Load more button hidden if we're done with it
            if (shouldHideLoadMoreButton) {
                btnLoadMore.setVisibility(View.GONE);
            }
        }

    }
}