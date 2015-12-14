package com.ojiofong.arounda.ui;

import java.util.HashMap;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.ojiofong.arounda.utils.AppManager;
import com.ojiofong.arounda.R;
import com.ojiofong.arounda.utils.DirectionManager;
import com.ojiofong.arounda.utils.SegmentedRadioGroup;

public class MapActivityDeal extends ActionBarActivity implements LocationListener, OnClickListener {

	SupportMapFragment mf;
	GoogleMap googlemap;
	Location location;
	int mapTypeFromIntent;
	String actionTitleFromIntent;
	ImageView car_iv, transit_iv, cyclist_iv, walking_iv;
	Double markerLat, markerLng, getLat, getLng;
	private static int lastChecked;
	RadioButton r;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_mapfrag);
		new AppManager(this).setStatusBarColorForKitKat(getResources().getColor(R.color.colorPrimaryDark));

		initialize();
		receiveIntents();
		initToolBar();

		mf = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

		if (savedInstanceState == null) {
			mf.setRetainInstance(true);

		} else {
			googlemap = mf.getMap();
		}

		setupMapIfNeeded();

	}

	private void initToolBar() {
		// initialize ToolBar		
		Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);	

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setTitle(actionTitleFromIntent);
	}

	private void receiveIntents() {
		mapTypeFromIntent = getIntent().getIntExtra("key", GoogleMap.MAP_TYPE_NONE);
		actionTitleFromIntent = getIntent().getStringExtra("actionTitle");
		DirectionManager dm = new DirectionManager(MapActivityDeal.this);
		getLat = dm.getAppLat();
		getLng = dm.getAppLng();

	}

	private void initialize() {

		car_iv = (ImageView) findViewById(R.id.map_car_iv);
		transit_iv = (ImageView) findViewById(R.id.map_transit_iv);
		cyclist_iv = (ImageView) findViewById(R.id.map_cyclist_iv);
		walking_iv = (ImageView) findViewById(R.id.map_walking_iv);

		car_iv.setOnClickListener(this);
		transit_iv.setOnClickListener(this);
		cyclist_iv.setOnClickListener(this);
		walking_iv.setOnClickListener(this);
	}

	private void setupMapIfNeeded() {
		// TODO Auto-generated method stub
		// googlemap =null;
		if (googlemap == null) {
			googlemap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();

			if (googlemap != null) {

				initmap();
				addMarkertoMap();
				makeMarkerInfoWindowClickable();
				detectMarkerTouch();
				makeMapBodyClickable();
			}
		}
	}

	private void initmap() {

		googlemap.setMyLocationEnabled(true);
		googlemap.setMapType(mapTypeFromIntent);
		googlemap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(getLat, getLng), 11));

	}

	private void addMarkertoMap() {

		// Adding the ArrayList of HashMap
		for (int i = 0; i < DealListActivity.dealsList.size(); i++) {

			HashMap<String, String> currentP = new HashMap<String, String>();
			currentP = DealListActivity.dealsList.get(i);
			String placeName = currentP.get(DealListActivity.KEY_MERCHANTNAME);
			String placeAddy = currentP.get(DealListActivity.KEY_COMPLETE_ADDRESS);
			String lat = currentP.get(DealListActivity.KEY_FINAL_LAT);
			String lon = currentP.get(DealListActivity.KEY_FINAL_LNG);
			Double latDouble = Double.parseDouble(lat); // converting string to double
			Double lonDouble = Double.parseDouble(lon); // converting string to double
				

			googlemap.addMarker(new MarkerOptions().title(placeName).snippet(placeAddy).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_map_marker))
					.position(new LatLng(latDouble, lonDouble)));
		}

	}

	private void makeMarkerInfoWindowClickable() {
		googlemap.setOnInfoWindowClickListener(new OnInfoWindowClickListener() {

			String nameAndAdress, markerStrings;

			@Override
			public void onInfoWindowClick(Marker marker) {
				// TODO Auto-generated method stub

				for (int i = 0; i < DealListActivity.dealsList.size(); i++) {

					HashMap<String, String> currentP = new HashMap<String, String>();
					currentP = DealListActivity.dealsList.get(i);
					String placeName = currentP.get(DealListActivity.KEY_MERCHANTNAME);
					String placeAddy = currentP.get(DealListActivity.KEY_COMPLETE_ADDRESS);

					// Using name and address to uniquely identify
					// marker
					nameAndAdress = (placeName + placeAddy).replaceAll("[^A-Za-z0-9]", "");
					markerStrings = (marker.getTitle() + marker.getSnippet()).replaceAll("[^A-Za-z0-9]", "");

					if ((nameAndAdress).matches(markerStrings)) {

						Intent intent = new Intent(getApplicationContext(), SingleDealActivity.class);
						intent.putExtra("position", i);
						intent.putExtra("dealsList", DealListActivity.dealsList);
						
						startActivity(intent);
						overridePendingTransition(R.anim.right_slide_in, R.anim.left_slide_out); // for starting activity only

						return;
					}
				}

			}
		});
	}

	private void makeMapBodyClickable() {
		googlemap.setOnMapClickListener(new OnMapClickListener() {

			@Override
			public void onMapClick(LatLng arg0) {
				// hide map direction icons
				car_iv.setVisibility(View.INVISIBLE);
				transit_iv.setVisibility(View.INVISIBLE);
				cyclist_iv.setVisibility(View.INVISIBLE);
				walking_iv.setVisibility(View.INVISIBLE);

			}
		});
	}

	private void detectMarkerTouch() {
		googlemap.setOnMarkerClickListener(new OnMarkerClickListener() {

			@Override
			public boolean onMarkerClick(Marker marker) {

				String nameAndAdress, markerStrings;

				for (int i = 0; i < DealListActivity.dealsList.size(); i++) {

					HashMap<String, String> currentP = new HashMap<String, String>();
					currentP = DealListActivity.dealsList.get(i);
					String placeName = currentP.get(DealListActivity.KEY_MERCHANTNAME);
					String placeAddy = currentP.get(DealListActivity.KEY_COMPLETE_ADDRESS);
					String lat = currentP.get(DealListActivity.KEY_FINAL_LAT);
					String lon = currentP.get(DealListActivity.KEY_FINAL_LNG);

					// Using name and address to uniquely identify marker
					nameAndAdress = (placeName + placeAddy).replaceAll("[^A-Za-z0-9]", "");
					markerStrings = (marker.getTitle() + marker.getSnippet()).replaceAll("[^A-Za-z0-9]", "");

					if ((nameAndAdress).matches(markerStrings)) {

						markerLat = Double.parseDouble(lat);
						markerLng = Double.parseDouble(lon);

						// show map direction icons here
						// BCos I'm 100% sure I have the values needed
						car_iv.setVisibility(View.VISIBLE);
						transit_iv.setVisibility(View.VISIBLE);
						cyclist_iv.setVisibility(View.VISIBLE);
						walking_iv.setVisibility(View.VISIBLE);

						return false;
					}
				}
				return false;
			}
		});
	}

	@Override
	public void onLocationChanged(Location location) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(getString(R.string.gps_location_settings));
		builder.setCancelable(false);
		builder.setPositiveButton(getString(R.string.enable_gps_message), new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				Intent startGps = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
				startActivity(startGps);

			}
		});

		// to set when the negative button is clicked
		builder.setNegativeButton(getString(R.string.leave_gps_off), new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				dialog.cancel();

			}
		});

		// Now let's call our alert Dialog
		AlertDialog alert = builder.create();
		alert.show();

	}

	private void fakeDialog() {

		final Dialog dialog = new Dialog(this, R.style.DialogSlideAnim);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.getWindow().getAttributes().gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
		dialog.setContentView(R.layout.activity_segmented);
		dialog.show();

		if (lastChecked == 0) {
			r = (RadioButton) dialog.findViewById(R.id.button_normalMap);
			r.setChecked(true);
			r.setBackgroundColor(getResources().getColor(R.color.colorAccent));
		} else {
			r = (RadioButton) dialog.findViewById(lastChecked);
			r.setChecked(true);
			r.setBackgroundColor(getResources().getColor(R.color.colorAccent));
		}

		final SegmentedRadioGroup segmentText;

		segmentText = (SegmentedRadioGroup) dialog.findViewById(R.id.segment_text);

		segmentText.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {

				if (group == segmentText) {

					switch (checkedId) {
					case R.id.button_normalMap:
						googlemap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
						dialog.dismiss();
						break;
					case R.id.button_hybridMap:
						googlemap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
						dialog.dismiss();
						break;
					case R.id.button_satelliteMap:
						googlemap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
						dialog.dismiss();
						break;
					case R.id.button_terrainMap:
						googlemap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
						dialog.dismiss();
						break;

					}
					if (checkedId != 0) {
						lastChecked = checkedId;
					}

				}
			}
		});

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.map, menu);
		// MenuItem mapItem = menu.findItem(R.id.action_grid);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {

		case android.R.id.home:
			// Overing goToParentActivity
			// Avoiding multiple calls to places api and retaining last list
			// state
			this.finish();
			overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
			break;

		case R.id.action_mapview:

			fakeDialog();
			break;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		this.finish();
		overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
	}

	@Override
	public void onClick(View v) {

		DirectionManager dm = new DirectionManager(MapActivityDeal.this);

		switch (v.getId()) {
		case R.id.map_car_iv:

			dm.performAction(R.drawable.ic_map_car, "d", markerLat, markerLng, getLat, getLng);

			break;

		case R.id.map_transit_iv:

			dm.performAction(R.drawable.ic_place_bus, "r", markerLat, markerLng, getLat, getLng);

			break;

		case R.id.map_cyclist_iv:

			dm.performAction(R.drawable.ic_map_cyclist, "b", markerLat, markerLng, getLat, getLng);

			break;

		case R.id.map_walking_iv:

			dm.performAction(R.drawable.ic_map_walking, "w", markerLat, markerLng, getLat, getLng);

			break;
		}

	}

	// ---
}