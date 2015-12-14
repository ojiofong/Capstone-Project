package com.ojiofong.arounda.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.view.ContextThemeWrapper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.ojiofong.arounda.R;
import com.ojiofong.arounda.adapter.CarAdapter;
import com.ojiofong.arounda.data.PopularPlace;
import com.ojiofong.arounda.utils.AlertDialogManager;
import com.ojiofong.arounda.utils.DirectionManager;
import com.ojiofong.arounda.utils.GPSUtil;
import com.ojiofong.arounda.utils.Utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class MainActivityBack extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {


    private ArrayList<PopularPlace> myPopularPlaces = new ArrayList<PopularPlace>();
    protected static String clickedName, clickedPlaceID;
    protected static int clickedPosition;
    public static Double LAST_KNOWN_LAT, LAST_KNOWN_LON;
    private ShareActionProvider mShareActionProvider;
    private boolean doubleBackToExitPressedOnce = false;
    CarAdapter adapter, adapter2;

    //new location supports
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    //LocationClient mLocationClient;
    Location mCurrentLocation;
    //LocationRequest mLocationRequest;
    boolean mUpdatesRequested;
    SharedPreferences mPrefs;
    SharedPreferences.Editor mEditor;
    ViewSwitcher viewSwitcher;
    View myFirstView;
    View mySecondView;
    PopularPlace dealObject;

    GoogleApiClient mGoogleApiClient;
    LocationRequest mLocationRequest;
    boolean mRequestingLocationUpdates;
    boolean isLocationPermissionGranted;

    private static final int PERMISSION_REQ_LOCATION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //new AppManager(this).setStatusBarColorForKitKat(getResources().getColor(R.color.colorPrimaryDark));
        initToolBar();
        //checkConnections();
        //checkForGooglePlayService();
        initialize();
        populatePlaceList();
        sortList();
        populateListView();
        populateGridView();
        buildGoogleApiClient();
        createLocationRequest();
        //checkFirstInstallation();

//		if (okay()) {
//			initFusedLocation();
//		}

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

                        ActivityCompat.requestPermissions(MainActivityBack.this,
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

    private boolean okay() {
        GPSUtil gps = new GPSUtil(getApplicationContext());
        if (checkForGooglePlayService() && gps.canGetLocation() && (gps.isNetworkEnabled() || gps.isGPSEnabled())) {
            return true;
        }
        return false;
    }

    private void checkConnections() {

        SharedPreferences defaultPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String gpsAlert = defaultPref.getString("gpsAlert", "ON");

        if (!Utils.isNetworkConnected(this)) {
            Toast.makeText(getApplicationContext(), getString(R.string.internet_error_message), Toast.LENGTH_LONG).show();
        }

        GPSUtil gps = new GPSUtil(MainActivityBack.this);
        if (gpsAlert.matches("ON") && (!gps.isGPSEnabled() || !gps.canGetLocation())) {

            Context mThemeContext = new ContextThemeWrapper(this, R.style.AppTheme);
            AlertDialogManager alert = new AlertDialogManager();
            alert.showGPSWarningAlert(mThemeContext);
        }

    }

    @SuppressLint("InflateParams")
    private void initToolBar() {

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //	getActionBar().setDisplayHomeAsUpEnabled(true);

        View v = this.getLayoutInflater().inflate(R.layout.myactionbar, null);

        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setCustomView(v);
        getSupportActionBar().setIcon(R.drawable.ic_launcher);
        getSupportActionBar().setElevation((float) 5);

    }

    private void initialize() {

        dealObject = new PopularPlace(getString(R.string.deals), R.drawable.ic_place_dollar, "groupon_deal");

        // Get both SharedPreferences and Editor
        mPrefs = getSharedPreferences("SharedPreferences", Context.MODE_PRIVATE);
        mEditor = mPrefs.edit();

        viewSwitcher = (ViewSwitcher) findViewById(R.id.viewSwitcher1);
        myFirstView = findViewById(R.id.view1_list);
        mySecondView = findViewById(R.id.view2_grid);

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

//	private void initFusedLocation() {
//
//		// Create a new global location parameters object
//		mLocationRequest = LocationRequest.create();
//		mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
//		// Set the update interval to 5 seconds
//		mLocationRequest.setInterval(5000);
//		// Set the interval ceiling to one minute
//		mLocationRequest.setFastestInterval(1000);
//		// Note that location updates are off until the user turns them on
//		mUpdatesRequested = false;
//		// Create a new location client, using the enclosing class to handle callbacks.
//		//mLocationClient = new LocationClient(this, this, this);
//	}

    private void populatePlaceList() {
        myPopularPlaces.add(dealObject);
        //myPopularPlaces.add(new PopularPlace("Deal", R.drawable.ic_place_dollar, "groupon_deal"));
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
        //myPopularPlaces.add(new PopularPlace(getString(R.string.grocery), R.drawable.ic_place_grocery, "grocery_or_supermarket"));
        myPopularPlaces.add(new PopularPlace(getString(R.string.gym_fitness), R.drawable.ic_place_gym, "gym"));
        myPopularPlaces.add(new PopularPlace(getString(R.string.hindu_temple), R.drawable.ic_place_pagoda, "hindu_temple"));
        myPopularPlaces.add(new PopularPlace(getString(R.string.hospitals), R.drawable.ic_place_hospital, "hospital"));
        myPopularPlaces.add(new PopularPlace(getString(R.string.hotels), R.drawable.ic_place_hotel, "lodging"));
        myPopularPlaces.add(new PopularPlace(getString(R.string.mosque), R.drawable.ic_place_islam, "mosque"));
        myPopularPlaces.add(new PopularPlace(getString(R.string.movie_theaters), R.drawable.ic_place_movie, "movie_theater"));
        myPopularPlaces.add(new PopularPlace(getString(R.string.parking), R.drawable.ic_place_parking, "parking"));
        myPopularPlaces.add(new PopularPlace(getString(R.string.pharmacy), R.drawable.ic_place_pharmacy, "pharmacy"));
        myPopularPlaces.add(new PopularPlace(getString(R.string.post_office), R.drawable.ic_place_post, "post_office"));
        myPopularPlaces.add(new PopularPlace(getString(R.string.restaurants), R.drawable.ic_place_restaurant, "restaurant"));
        myPopularPlaces.add(new PopularPlace(getString(R.string.shopping_malls), R.drawable.ic_place_shoppingbag, "shopping_mall"));
        myPopularPlaces.add(new PopularPlace(getString(R.string.supermarkets) + "/" + getString(R.string.grocery), R.drawable.ic_place_shoppingcart, "grocery_or_supermarket"));
        myPopularPlaces.add(new PopularPlace(getString(R.string.synagogue), R.drawable.ic_place_synagogue, "synagogue"));
        myPopularPlaces.add(new PopularPlace(getString(R.string.train_subway), R.drawable.ic_place_train, "train_station|subway_station"));
        myPopularPlaces.add(new PopularPlace(getString(R.string.pet_store), R.drawable.ic_place_pet, "pet_store"));
    }

    private void goToNextActivity(int position) {

        PopularPlace clickedPopularPlace = myPopularPlaces.get(position);
        clickedName = clickedPopularPlace.getPlaceName();
        clickedPosition = position;
        clickedPlaceID = clickedPopularPlace.getPlaceID();

        GPSUtil gps = new GPSUtil(MainActivityBack.this);
        if (okay()) {
            if (clickedPlaceID != null) {
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
                }

                Intent i = new Intent(MainActivityBack.this, PlaceListActivity.class);
                startActivity(i);
                overridePendingTransition(R.anim.right_slide_in, R.anim.left_slide_out); // for starting activity only
            }
        } else {
            gps.showGoogleLocationSettingsAlert();
        }

    }

    private void populateListView() {
        // first create MyListAdapter and extend ArrayAdapter of the PopularPlace class

        adapter = new CarAdapter(MainActivityBack.this, R.layout.single_item_view, myPopularPlaces);

        ListView list = (ListView) findViewById(R.id.listView1);
        list.setAdapter(adapter);

        list.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View viewClicked, int position, long id) {

                if (myPopularPlaces.get(position).getPlaceID().matches("groupon_deal")) {
                    startActivity(new Intent(MainActivityBack.this, DealListActivity.class));
                    overridePendingTransition(R.anim.right_slide_in, R.anim.left_slide_out); // for starting activity only
                } else {
                    goToNextActivity(position);
                }

            }

        });
    }

    private void populateGridView() {

        adapter2 = new CarAdapter(MainActivityBack.this, R.layout.single_item_grid, myPopularPlaces);

        GridView gridView = (GridView) findViewById(R.id.gridView1);
        gridView.setAdapter(adapter2);
        gridView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {

                if (myPopularPlaces.get(position).getPlaceID().matches("groupon_deal")) {
                    startActivity(new Intent(MainActivityBack.this, DealListActivity.class));
                    overridePendingTransition(R.anim.right_slide_in, R.anim.left_slide_out); // for starting activity only
                } else {
                    goToNextActivity(position);
                }

            }

        });

    }

    private boolean checkForGooglePlayService() {
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);

        // Showing status
        if (status == ConnectionResult.SUCCESS) {
            return true;
        } else {
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(status, this, 0);
            dialog.show();
        }
        return false;

    }

    /*
     * private String addOrRemoveDeal() { //Not active now. I did this
     * Asynchronously instead
     *
     * if (!myPopularPlaces.isEmpty()) { DirectionManager dm = new
     * DirectionManager(MainActivity.this);
     *
     * double lat = dm.getAppLat(); double lng = dm.getAppLng();
     *
     * Geocoder gcd = new Geocoder(MainActivity.this, Locale.getDefault());
     * List<Address> addresses = null; try { addresses =
     * gcd.getFromLocation(lat, lng, 1);
     *
     * if (addresses.size() > 0) { String countryCode =
     * addresses.get(0).getCountryCode(); if (!countryCode.matches("US") &&
     * !countryCode.matches("CA") && myPopularPlaces.contains(dealObject)) { //if not in
     * US and Canada remove groupon deal myPopularPlaces.remove(dealObject); sortList();
     * adapter.notifyDataSetChanged(); adapter2.notifyDataSetChanged(); } else
     * if ((countryCode.matches("US") || countryCode.matches("CA")) &&
     * !myPopularPlaces.contains(dealObject)) { //if user is from US add the deal
     * myPopularPlaces.add(dealObject); sortList(); adapter.notifyDataSetChanged();
     * adapter2.notifyDataSetChanged(); } } } catch (IOException e) { // TODO
     * Auto-generated catch block e.printStackTrace(); }
     *
     * } return null;
     *
     * }
     */
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
                GPSUtil gps = new GPSUtil(MainActivityBack.this);
                if (okay()) {
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

                    startActivity(new Intent(getApplicationContext(), TextSearchActivity.class));
                    overridePendingTransition(R.anim.right_slide_in, R.anim.left_slide_out); // for starting activity only

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

    /**
     * In response to a request to start updates, send a request to Location
     * Services
     */
    private void startPeriodicUpdates() {

//		if (okay()) {
//			mLocationClient.requestLocationUpdates(mLocationRequest, this);
//			//	Toast.makeText(this, "started updates", Toast.LENGTH_SHORT).show();
//		}
    }

    /**
     * In response to a request to stop updates, send a request to Location
     * Services
     */
    private void stopPeriodicUpdates() {
//		if (okay())
//			mLocationClient.removeLocationUpdates(this);
        //	Toast.makeText(this, "stopped updates", Toast.LENGTH_SHORT).show();
    }

//	@Override
//	public void onLocationChanged(Location location) {
//		// TODO Auto-generated method stub
//
//		if (okay()) {
//			mCurrentLocation = location; //May be redundantly updating location but I don't care.
//			LAST_KNOWN_LAT = mCurrentLocation.getLastKnownLatitude();
//			LAST_KNOWN_LON = mCurrentLocation.getLastKnownLongitude();
//
//		}
//
//	}

//	@Override
//	public void onConnectionFailed(ConnectionResult connectionResult) {
//		/*
//		 * Google Play services can resolve some errors it detects. If the error
//		 * has a resolution, try sending an Intent to start a Google Play
//		 * services activity that can resolve error.
//		 */
//		if (connectionResult.hasResolution()) {
//			try {
//				// Start an Activity that tries to resolve the error
//				connectionResult.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
//				/*
//				 * Thrown if Google Play services canceled the original
//				 * PendingIntent
//				 */
//			} catch (IntentSender.SendIntentException e) {
//				// Log the error
//				e.printStackTrace();
//			}
//		} else {
//			/*
//			 * If no resolution is available, display a dialog to the user with
//			 * the error.
//			 */
//			//showErrorDialog(connectionResult.getErrorCode());
//			Toast.makeText(this, getString(R.string.unable_to_resolve_connection) + ": " + connectionResult.getErrorCode(), Toast.LENGTH_SHORT).show();
//		}
//
//	}

//	@Override
//	public void onConnected(Bundle arg0) {
//		//	Toast.makeText(this, "Connected", Toast.LENGTH_SHORT).show();
//
//		if (checkForGooglePlayService()) {
//			mCurrentLocation = mLocationClient.getLastLocation();
//			//to notify we have requested update
//			mUpdatesRequested = true;
//			startPeriodicUpdates();
//		}
//
//	}

//	@Override
//	public void onDisconnected() {
//		// TODO Auto-generated method stub
//		//	Toast.makeText(this, "Disconnected. Please re-connect.", Toast.LENGTH_SHORT).show();
//
//	}

    /*
     * This is useful in performing one time tasks for first time installers e.g
     * new features notifications
     */
    private boolean checkFirstInstallation() {
        String isFirstInstallation = "isFirstInstallation";

        SharedPreferences pref = getSharedPreferences("settings", Context.MODE_PRIVATE);
        boolean isUserFirstInstallation = pref.getBoolean(isFirstInstallation, true);

        if (isUserFirstInstallation) {
            pref.edit().putBoolean(isFirstInstallation, false).commit();
            // do something once only here
            preventSearchAndChangeLocCrash();
            return false;
        }

        return true;
    }

    private void preventSearchAndChangeLocCrash() {
        /*
         * This works and runs only once. Even if user clears app data it
		 * doesn't matter since they both nullify the objects. It prevents crash
		 * caused by proguard obfuscation of versions > 2.0.0
		 */
        SharedPreferences.Editor editor = getSharedPreferences("settings", Context.MODE_PRIVATE).edit();
        editor.putString("gsonSearchHistory", null);
        editor.putString("gsonList_Pref", null);
        editor.putBoolean("usecurrentloc_Pref", true);
        editor.commit();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
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

    }

    @Override
    public void onLocationChanged(Location location) {

    }

    private class addOrRemoveDealTask extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... params) {
            // TODO Auto-generated method stub
            if (!myPopularPlaces.isEmpty()) {
                DirectionManager dm = new DirectionManager(MainActivityBack.this);

                double lat = dm.getAppLat();
                double lng = dm.getAppLng();

                Geocoder gcd = new Geocoder(MainActivityBack.this, Locale.getDefault());
                List<Address> addresses = null;
                try {
                    addresses = gcd.getFromLocation(lat, lng, 1);

                    if (addresses.size() > 0) {
                        String countryCode = addresses.get(0).getCountryCode();
                        return countryCode;
                    }
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (result != null)
                if (!result.matches("US") && !result.matches("CA") && myPopularPlaces.contains(dealObject)) {
                    //if not in US and Canada remove groupon deal
                    myPopularPlaces.remove(dealObject);
                    sortList();
                    adapter.notifyDataSetChanged();
                    adapter2.notifyDataSetChanged();
                } else if ((result.matches("US") || result.matches("CA")) && !myPopularPlaces.contains(dealObject)) {
                    //if user is from US add the deal
                    myPopularPlaces.add(dealObject);
                    sortList();
                    adapter.notifyDataSetChanged();
                    adapter2.notifyDataSetChanged();
                }

        }

    }

//	@Override
//	protected void onPause() {
//		// Save the current setting for updates
//		mEditor.putBoolean("KEY_UPDATES_ON", mUpdatesRequested);
//		mEditor.commit();
//
//		super.onPause();
//	}

//	@Override
//	protected void onResume() {
//
//		//addOrRemoveDeal();
//
//		new addOrRemoveDealTask().execute();
//
//		super.onResume();
//		// .... other stuff in my onBackPressed ....
//		this.doubleBackToExitPressedOnce = false;
//
//		/*
//		 * Get any previous setting for location updates Gets "false" if an
//		 * error occurs
//		 */
//		if (okay()) { //crashes without this validation
//			if (mPrefs.contains("KEY_UPDATES_ON")) {
//				mUpdatesRequested = mPrefs.getBoolean("KEY_UPDATES_ON", false);
//			} else {
//				// Otherwise, turn off location updates
//				mEditor.putBoolean("KEY_UPDATES_ON", false);
//				mEditor.commit();
//
//			}
//		}
//
//	}

	/*
     * Called when the Activity becomes visible.
	 */
//	@Override
//	protected void onStart() {
//		//initialize();
//		SharedPreferences pref = getSharedPreferences("settings", Context.MODE_PRIVATE);
//		boolean b = pref.getBoolean("usingGridView", false);
//		if (viewSwitcher.getCurrentView() == myFirstView && (b == true)) {
//			viewSwitcher.showNext();
//		}
//
//		Context mThemeContext = new ContextThemeWrapper(this, R.style.AppTheme);
//		RateThisApp.onStart(mThemeContext);
//		RateThisApp.showRateDialogIfNeeded(mThemeContext);
//
//		super.onStart();
//		if (okay()) {
//			//mLocationClient.connect();
//		}
//
//	}

	/*
     * Called when the Activity is no longer visible. Stop updates and
	 * disconnect.
	 */
//	@Override
//	protected void onStop() {
//		if (okay()) {
//
//			// If the client is connected
//			if (mLocationClient.isConnected()) {
//
//				if (checkForGooglePlayService()) {
//					//notify request stopped or disabled
//					mUpdatesRequested = false;
//					stopPeriodicUpdates();
//				}
//				/*
//				 * After disconnect() is called, the client is considered
//				 * "dead".
//				 */
//				// Disconnecting the client invalidates it. ..(important to prevent caching previous location)
//				mLocationClient.disconnect();
//
//			}
//
//		}
//
//		super.onStop();
//	}

    @Override
    public void onBackPressed() {

        if (doubleBackToExitPressedOnce) {

            super.onBackPressed();
            System.exit(0);

            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, getString(R.string.back_again), Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 2000);
    }

}