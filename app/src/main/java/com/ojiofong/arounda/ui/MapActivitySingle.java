package com.ojiofong.arounda.ui;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
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
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.ojiofong.arounda.R;
import com.ojiofong.arounda.utils.AppManager;
import com.ojiofong.arounda.utils.DirectionManager;
import com.ojiofong.arounda.utils.SegmentedRadioGroup;

public class MapActivitySingle extends AppCompatActivity implements OnClickListener {

	GoogleMap googlemap;
	SupportMapFragment mf;
	String referenceFromIntent, nameFromIntent, addyFromIntent, openingHoursStr;
	Double latFromIntent, lonFromIntent, originLat, originLng, getLat, getLng;
	private static int lastChecked;
	RadioButton r;
	private ImageView car_iv, transit_iv, cyclist_iv, walking_iv;
	// Dialog iconDialog;

	@Override
	protected void onCreate(Bundle arg0) {
		// TODO Auto-generated method stub
		super.onCreate(arg0);
		setContentView(R.layout.activity_mapfrag);
		new AppManager(this).setStatusBarColorForKitKat(getResources().getColor(R.color.colorPrimaryDark));
		receiveIntents();
		initialize();
		initToolBar();
		initmap();

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

	private void receiveIntents() {
		nameFromIntent = getIntent().getStringExtra("key_name");
		addyFromIntent = getIntent().getStringExtra("key_addy");
		latFromIntent = Double.parseDouble(getIntent().getStringExtra("key_lat"));
		lonFromIntent = Double.parseDouble(getIntent().getStringExtra("key_lon"));
		getLat = getIntent().getDoubleExtra("getLat", 0);
		getLng = getIntent().getDoubleExtra("getLng", 0);

	}

	private void initToolBar() {

		// initialize ToolBar		
		Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);	
		
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setTitle(nameFromIntent);
		getSupportActionBar().setSubtitle(addyFromIntent);
	}

	private void initmap() {

		LatLng markerLatLng = new LatLng(latFromIntent, lonFromIntent);

		mf = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
		googlemap = mf.getMap();
		googlemap.setMyLocationEnabled(true);
		// googlemap.setMapType(mapTypeFromIntent);
		googlemap.moveCamera(CameraUpdateFactory.newLatLngZoom(markerLatLng, 9));

		googlemap.addMarker(new MarkerOptions().title(nameFromIntent).snippet(addyFromIntent).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_map_marker))
				.position(new LatLng(latFromIntent, lonFromIntent)));
		// .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))

		detectMarkerTouch();
		makeMapBodyClickable();
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

	private void detectMarkerTouch() {
		googlemap.setOnMarkerClickListener(new OnMarkerClickListener() {

			@Override
			public boolean onMarkerClick(Marker marker) {

				// show direction icons here
				car_iv.setVisibility(View.VISIBLE);
				transit_iv.setVisibility(View.VISIBLE);
				cyclist_iv.setVisibility(View.VISIBLE);
				walking_iv.setVisibility(View.VISIBLE);

				return false;
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

				// car_iv.animate().translationX(100).start();

			}
		});
	}

	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.map, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {

		case android.R.id.home:
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
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		lastChecked = 0;
	}

	@Override
	public void onClick(View v) {

		DirectionManager dm = new DirectionManager(MapActivitySingle.this);

		switch (v.getId()) {
		case R.id.map_car_iv:

			dm.performAction(R.drawable.ic_map_car, "d", latFromIntent, lonFromIntent, getLat, getLng);

			break;

		case R.id.map_transit_iv:

			dm.performAction(R.drawable.ic_place_bus, "r", latFromIntent, lonFromIntent, getLat, getLng);

			break;

		case R.id.map_cyclist_iv:

			dm.performAction(R.drawable.ic_map_cyclist, "b", latFromIntent, lonFromIntent, getLat, getLng);
			
			break;

		case R.id.map_walking_iv:
			
			dm.performAction(R.drawable.ic_map_walking, "w", latFromIntent, lonFromIntent, getLat, getLng);
			
			break;
		}

	}

}