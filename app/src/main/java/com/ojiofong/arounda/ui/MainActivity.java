package com.ojiofong.arounda.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.ContextThemeWrapper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.ojiofong.arounda.R;
import com.ojiofong.arounda.RateThisApp;
import com.ojiofong.arounda.data.PopularPlace;
import com.ojiofong.arounda.utils.AlertDialogManager;
import com.ojiofong.arounda.utils.Const;
import com.ojiofong.arounda.utils.GPSUtil;
import com.ojiofong.arounda.utils.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {


    public static final String TAG = MainActivity.class.getSimpleName();
    public ArrayList<PopularPlace> myPopularPlaces = new ArrayList<PopularPlace>();
    protected static String clickedName, clickedPlaceID;
    protected static int clickedPosition;
    public static Double LAST_KNOWN_LAT, LAST_KNOWN_LON;

    //new location supports
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    Location mCurrentLocation;
    ViewPager viewPager;

    GoogleApiClient mGoogleApiClient;
    LocationRequest mLocationRequest;
    boolean mRequestingLocationUpdates;
    boolean isLocationPermissionGranted;

    private static final int PERMISSION_REQ_LOCATION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_main_viewpager);
     //   new AppManager(this).setStatusBarColorForKitKat(ContextCompat.getColor(this, R.color.colorPrimaryDark));
        initToolBar();
        setUpFab();
        checkConnections();
        checkForGooglePlayService();
        populatePlaceList();
        //sortList();
        buildGoogleApiClient();
        createLocationRequest();
        setupViewPager();

    }


    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    protected void startLocationUpdates() {

        if (mGoogleApiClient.isConnected() && !mRequestingLocationUpdates) {

            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
            mRequestingLocationUpdates = true;
        }
    }

    protected void stopLocationUpdates() {
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mRequestingLocationUpdates = false;
        }
    }

    private void checkPermissionForLocation() {
        // If permission is not granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            //Toast.makeText(this, "permission is not granted", Toast.LENGTH_SHORT).show();

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                Snackbar.make(findViewById(R.id.main_layout)
                        , "Location permission is required to proceed bro"
                        , Snackbar.LENGTH_INDEFINITE).setAction("ok", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        ActivityCompat.requestPermissions(MainActivity.this,
                                new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQ_LOCATION);

                    }
                }).show();


            } else {

                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQ_LOCATION);

            }

        } else {

            isLocationPermissionGranted = true;
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_REQ_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    isLocationPermissionGranted = true;

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {

                    isLocationPermissionGranted = false;

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //checkPermissionForLocation();
        startLocationUpdates();

    }

    @Override
    protected void onPause() {
        super.onPause();
        // if (isLocationPermissionGranted)
        stopLocationUpdates();
    }

    private void checkConnections() {

        SharedPreferences defaultPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String gpsAlert = defaultPref.getString("gpsAlert", "ON");

        if (!Utils.isNetworkConnected(this)) {
            Toast.makeText(getApplicationContext(), getString(R.string.internet_error_message), Toast.LENGTH_LONG).show();
        }

        GPSUtil gps = new GPSUtil(MainActivity.this);
        if (gpsAlert.matches("ON") && (!gps.canGetLocation())) {
            AlertDialogManager alert = new AlertDialogManager();
            alert.showGPSWarningAlert(this);
        }

    }

    @SuppressLint("InflateParams")
    private void initToolBar() {

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //toolbar.setLogo(R.drawable.ic_launcher);

    }

    private void sortList() {
        Collections.sort(myPopularPlaces, new Comparator<PopularPlace>() {

            @Override
            public int compare(PopularPlace lhs, PopularPlace rhs) {
                // TODO Auto-generated method stub
                String first = lhs.getPlaceName();
                String second = rhs.getPlaceName();

                return first.compareToIgnoreCase(second);
            }
        });

    }


    private void populatePlaceList() {
        // myPopularPlaces.add(dealObject);
        myPopularPlaces.add(new PopularPlace(getString(R.string.airports), R.drawable.ic_place_airport, "airport"));
        myPopularPlaces.add(new PopularPlace(getString(R.string.atm), R.drawable.ic_place_atm, "atm"));
        myPopularPlaces.add(new PopularPlace(getString(R.string.attractions), R.drawable.ic_place_attractions, "museum|aquarium|art_gallery|amusement_park|park"));
        myPopularPlaces.add(new PopularPlace(getString(R.string.banks), R.drawable.ic_place_bank, "bank"));
        myPopularPlaces.add(new PopularPlace(getString(R.string.barber_haircare), R.drawable.ic_place_beauty, "hair_care"));
        myPopularPlaces.add(new PopularPlace(getString(R.string.bars), R.drawable.ic_place_bar, "bar"));
        myPopularPlaces.add(new PopularPlace(getString(R.string.beauty_salon), R.drawable.ic_place_beautysalon, "beauty_salon"));
        myPopularPlaces.add(new PopularPlace(getString(R.string.bus_station), R.drawable.ic_place_bus, "bus_station"));
        myPopularPlaces.add(new PopularPlace(getString(R.string.car_rental), R.drawable.ic_place_car_cyan, "car_rental"));
        myPopularPlaces.add(new PopularPlace(getString(R.string.car_repair), R.drawable.ic_place_car, "car_repair"));
        myPopularPlaces.add(new PopularPlace(getString(R.string.car_wash), R.drawable.ic_place_car_green, "car_wash"));
        myPopularPlaces.add(new PopularPlace(getString(R.string.coffee_shops), R.drawable.ic_place_coffee, "cafe")); // 3
        myPopularPlaces.add(new PopularPlace(getString(R.string.church), R.drawable.ic_place_church, "church"));// 4
        myPopularPlaces.add(new PopularPlace(getString(R.string.gas_stations), R.drawable.ic_place_gas, "gas_station"));
        myPopularPlaces.add(new PopularPlace(getString(R.string.gym_fitness), R.drawable.ic_place_gym, "gym"));
        myPopularPlaces.add(new PopularPlace(getString(R.string.hindu_temple), R.drawable.ic_place_pagoda, "hindu_temple"));
        myPopularPlaces.add(new PopularPlace(getString(R.string.hospitals), R.drawable.ic_place_hospital, "hospital"));
        myPopularPlaces.add(new PopularPlace(getString(R.string.hotels), R.drawable.ic_place_hotel, "lodging"));
        myPopularPlaces.add(new PopularPlace(getString(R.string.mosque), R.drawable.ic_place_islam, "mosque"));
        myPopularPlaces.add(new PopularPlace(getString(R.string.movie_theaters), R.drawable.ic_place_movie, "movie_theater"));
        myPopularPlaces.add(new PopularPlace(getString(R.string.parking), R.drawable.ic_place_parking, "parking"));
        myPopularPlaces.add(new PopularPlace(getString(R.string.pet_store), R.drawable.ic_place_pet, "pet_store"));
        myPopularPlaces.add(new PopularPlace(getString(R.string.pharmacy), R.drawable.ic_place_pharmacy, "pharmacy"));
        myPopularPlaces.add(new PopularPlace(getString(R.string.post_office), R.drawable.ic_place_post, "post_office"));
        myPopularPlaces.add(new PopularPlace(getString(R.string.restaurants), R.drawable.ic_place_restaurant, "restaurant"));
        myPopularPlaces.add(new PopularPlace(getString(R.string.shopping_malls), R.drawable.ic_place_shoppingbag, "shopping_mall"));
        myPopularPlaces.add(new PopularPlace(getString(R.string.supermarkets) + "/" + getString(R.string.grocery), R.drawable.ic_place_shoppingcart, "grocery_or_supermarket"));
        myPopularPlaces.add(new PopularPlace(getString(R.string.synagogue), R.drawable.ic_place_synagogue, "synagogue"));
        myPopularPlaces.add(new PopularPlace(getString(R.string.train_subway), R.drawable.ic_place_train, "train_station|subway_station"));

    }

    public void goToNextActivity(int position) {

        PopularPlace clickedPopularPlace = myPopularPlaces.get(position);
        clickedName = clickedPopularPlace.getPlaceName();
        clickedPosition = position;
        clickedPlaceID = clickedPopularPlace.getPlaceID();

        GPSUtil gps = new GPSUtil(this);
        if (gps.canGetLocation()) {
            if (checkForGooglePlayService() && clickedPlaceID != null) {

                Intent i = new Intent(MainActivity.this, PlaceListActivity.class);
                startActivity(i);
                overridePendingTransition(R.anim.right_slide_in, R.anim.left_slide_out); // for starting activity only
            }
        } else {
            gps.showGoogleLocationSettingsAlert();
        }

    }

    private boolean checkForGooglePlayService() {
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (status == ConnectionResult.SUCCESS) {
            return true;
        } else {
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(status, this, 0);
            dialog.show();
        }
        return false;

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present
        getMenuInflater().inflate(R.menu.main, menu);

        // hide unwanted MenuItems
        MenuItem deleteItem = menu.findItem(R.id.action_DeleteHistory);
        deleteItem.setVisible(false);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        searchItem.setVisible(false); // hiding expandable search view


        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.action_search_null:
                GPSUtil gps = new GPSUtil(MainActivity.this);
                if (checkForGooglePlayService() && gps.canGetLocation()) {
                    //initialized here to avoid null pointer exception
                    if (gps.isNetworkEnabled()) {
                        try {
                            LAST_KNOWN_LAT = mCurrentLocation.getLatitude();
                            LAST_KNOWN_LON = mCurrentLocation.getLongitude();
                        } catch (NullPointerException e) {
                            // TODO: handle exception

                        } finally {
                            if (LAST_KNOWN_LAT == null || LAST_KNOWN_LON == null) {
                                LAST_KNOWN_LAT = gps.getLastKnownLatitude();
                                LAST_KNOWN_LON = gps.getLastKnownLongitude();
                            }
                        }
                    } else if (!gps.isNetworkEnabled()) {
                        LAST_KNOWN_LAT = gps.getLastKnownLatitude();
                        LAST_KNOWN_LON = gps.getLastKnownLongitude();
                    }

                    gotoSearchActivity();

                } else {
                    gps.showGoogleLocationSettingsAlert();
                }

                break;

            case R.id.action_settings:

                Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
                startActivity(intent);
                break;

            case R.id.action_changeLocation:

                Intent i = new Intent(getApplicationContext(), ChangeLocationActivity.class);
                i.putExtra("latString", String.valueOf(LAST_KNOWN_LAT));
                i.putExtra("lngString", String.valueOf(LAST_KNOWN_LON));
                startActivity(i);
                overridePendingTransition(R.anim.right_slide_in, R.anim.left_slide_out); // for starting activity only

                break;

        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
        goToLastPagerPosition();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onConnected(Bundle bundle) {

        //  checkPermissionForLocation();

        mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mCurrentLocation != null) {
            LAST_KNOWN_LAT = mCurrentLocation.getLatitude();
            LAST_KNOWN_LON = mCurrentLocation.getLongitude();

            // Save to sharedPref
            GPSUtil gpsUtil = new GPSUtil(getApplicationContext());
            gpsUtil.setLastKnownLatitude(LAST_KNOWN_LAT.toString());
            gpsUtil.setLastKnownLongitude(LAST_KNOWN_LON.toString());
        }

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        /*
         * Google Play services can resolve some errors it detects. If the error
		 * has a resolution, try sending an Intent to start a Google Play
		 * services activity that can resolve error.
		 */
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
                /*
                 * Thrown if Google Play services canceled the original
				 * PendingIntent
				 */
            } catch (IntentSender.SendIntentException e) {
                // Log the error
                e.printStackTrace();
            }
        } else {
			/*
			 * If no resolution is available, display a dialog to the user with
			 * the error.
			 */
            // showErrorDialog(connectionResult.getErrorCode());
            Toast.makeText(this, getString(R.string.unable_to_resolve_connection) + ": " + connectionResult.getErrorCode(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onLocationChanged(Location location) {

        LAST_KNOWN_LAT = location.getLatitude();
        LAST_KNOWN_LON = location.getLongitude();

        GPSUtil gpsUtil = new GPSUtil(getApplicationContext());
        gpsUtil.setLastKnownLatitude(String.valueOf(location.getLatitude()));
        gpsUtil.setLastKnownLongitude(String.valueOf(location.getLongitude()));
    }


    private void rateAppIfNeeded() {
        Context mThemeContext = new ContextThemeWrapper(this, R.style.AppTheme);
        RateThisApp.onStart(mThemeContext);
        RateThisApp.showRateDialogIfNeeded(mThemeContext);
    }


    private void goToLastPagerPosition() {
        if (viewPager != null) {
            int lastPageSelected = PreferenceManager.getDefaultSharedPreferences(this).getInt(Const.PREF_LAST_PAGER_POSITION, 0);
            viewPager.setCurrentItem(lastPageSelected);
        }
    }

    private void saveLastPagerPosition(int position) {
        PreferenceManager.getDefaultSharedPreferences(this).edit().putInt(Const.PREF_LAST_PAGER_POSITION, position).apply();
    }


    private void setupViewPager() {
        viewPager = (ViewPager) findViewById(R.id.viewpager);
        if (viewPager != null) {
            MPagerAdapter adapter = new MPagerAdapter(getSupportFragmentManager());
            adapter.addFragment(new MainListFrag(), "List");
            adapter.addFragment(new MainGridFrag(), "Grid");
            viewPager.setAdapter(adapter);
            viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                }

                @Override
                public void onPageSelected(int position) {
                    saveLastPagerPosition(position);
                }

                @Override
                public void onPageScrollStateChanged(int state) {

                }
            });

            setupTabLayout(viewPager);

        }
    }

    private void setupTabLayout(ViewPager viewPager) {
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
    }

    static class MPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragments = new ArrayList<>();
        private final List<String> mFragmentTitles = new ArrayList<>();

        public MPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        public void addFragment(Fragment fragment, String title) {
            mFragments.add(fragment);
            mFragmentTitles.add(title);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragments.get(position);
        }

        @Override
        public int getCount() {
            return mFragments.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitles.get(position);
        }
    }


    private void setUpFab() {
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gotoSearchActivity();
            }
        });
    }

    private void gotoSearchActivity() {

        startActivity(new Intent(getApplicationContext(), TextSearchActivity.class));
        overridePendingTransition(R.anim.right_slide_in, R.anim.left_slide_out); // for starting activity only
    }


}