package com.ojiofong.arounda.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ojiofong.arounda.utils.AppManager;
import com.ojiofong.arounda.R;
import com.ojiofong.arounda.utils.DirectionManager;

public class SingleDealActivity extends ActionBarActivity implements OnClickListener {
	private TextView phone_tv, web_tv, web2_tv, review_tv, contactDetails_tv, descriptionText_tv, statusSingle_tv;
	private TextView ratingNum_tv, menuHeader_tv, menuInfo_tv, loadMoreReviews, streetView_tv, mapView_tv;
	private TextView distance_tv, driving_tv, walking_tv;
	private TextView openingHoursHeader_tv, day0, day1, day2, day3, day4, day5, day6;
	private ImageView phone_iv, web_iv, web2_iv, direction_iv, person_iv, rating_iv, share_iv, poweredby_iv;
	private String title, drivingTime, walkingTime, desc, price;
	private Double appLat, appLng, dLat, dLng;
	private String placeName, placePhone, placeWeb, placeWeb2, distance, dealUrl, placeAddress, placeReviewCombo, placeRating;
	private String reviewContent;
	private RatingBar ratingbar;
	private Typeface tf;
	private ArrayList<HashMap<String, String>> dealsList = new ArrayList<>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_single);
		new AppManager(this).setStatusBarColorForKitKat(getResources().getColor(R.color.colorPrimaryDark));
		receiveIntents();
		initToolBar();
		initializeAndHide();
		new GetDetailsTask().execute();
	}

	@SuppressWarnings("unchecked")
	private void receiveIntents() {
		DirectionManager dm = new DirectionManager(SingleDealActivity.this);
		appLat = dm.getAppLat();
		appLng = dm.getAppLng();
		dealsList = (ArrayList<HashMap<String, String>>) getIntent().getSerializableExtra("dealsList");
		int position = getIntent().getIntExtra("position", 0);
		placeName = dealsList.get(position).get(DealListActivity.KEY_MERCHANTNAME);
		placeAddress = dealsList.get(position).get(DealListActivity.KEY_COMPLETE_ADDRESS);
		placePhone = dealsList.get(position).get(DealListActivity.KEY_PHONE);
		placeWeb = dealsList.get(position).get(DealListActivity.KEY_MERCHANT_WEBSITE);
		distance = dealsList.get(position).get(DealListActivity.KEY_DISTANCE);
		dealUrl = dealsList.get(position).get(DealListActivity.KEY_DEALURL);
		desc = dealsList.get(position).get(DealListActivity.KEY_DESCRIPTION);
		title = dealsList.get(position).get(DealListActivity.KEY_TITLE);
		price = dealsList.get(position).get(DealListActivity.KEY_PRICE);
		dLat = Double.parseDouble(dealsList.get(position).get(DealListActivity.KEY_FINAL_LAT));
		dLng = Double.parseDouble(dealsList.get(position).get(DealListActivity.KEY_FINAL_LNG));

	}

	private void initToolBar() {
		// initialize ToolBar		
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setTitle(placeName);
		getSupportActionBar().setSubtitle(placeAddress);
	}

	private void initializeAndHide() {

		tf = Typeface.createFromAsset(getAssets(), "fonts/roboto_regular.ttf");

		poweredby_iv = (ImageView) findViewById(R.id.powered_by_google_singleact);
		poweredby_iv.setVisibility(View.GONE);

		distance_tv = (TextView) findViewById(R.id.textDistance);
		distance_tv.setVisibility(View.GONE);
		driving_tv = (TextView) findViewById(R.id.textDriving);
		driving_tv.setVisibility(View.GONE);
		driving_tv.setOnClickListener(this);
		walking_tv = (TextView) findViewById(R.id.textWalking);
		walking_tv.setVisibility(View.GONE);
		walking_tv.setOnClickListener(this);
		streetView_tv = (TextView) findViewById(R.id.streetView_tv);
		streetView_tv.setOnClickListener(this);
		mapView_tv = (TextView) findViewById(R.id.mapView_tv);
		mapView_tv.setOnClickListener(this);
		share_iv = (ImageView) findViewById(R.id.shareImageView1);
		share_iv.setOnClickListener(this);
		statusSingle_tv = (TextView) findViewById(R.id.statusSingle);
		statusSingle_tv.setTypeface(tf);

		// initialize and hide views and buttons
		contactDetails_tv = (TextView) findViewById(R.id.contactDetails1);
		contactDetails_tv.setTypeface(tf);
		contactDetails_tv.setText(getString(R.string.contact_details).toUpperCase(Locale.getDefault()));

		phone_tv = (TextView) findViewById(R.id.phone1);
		// phone_tv.setOnClickListener(this);
		phone_tv.setVisibility(View.GONE);

		web_tv = (TextView) findViewById(R.id.website1);
		web_tv.setVisibility(View.GONE);
		web2_tv = (TextView) findViewById(R.id.website2);
		web2_tv.setVisibility(View.GONE);

		review_tv = (TextView) findViewById(R.id.reviews1);
		review_tv.setText(getString(R.string.reviews).toUpperCase(Locale.getDefault()));
		review_tv.setVisibility(View.GONE);

		phone_iv = (ImageView) findViewById(R.id.imagePhone1);
		phone_iv.setVisibility(View.GONE);

		web_iv = (ImageView) findViewById(R.id.imageWeb1);
		web_iv.setVisibility(View.GONE);
		web2_iv = (ImageView) findViewById(R.id.imageWeb2);
		web2_iv.setVisibility(View.GONE);

		rating_iv = (ImageView) findViewById(R.id.imageRating);
		rating_iv.setVisibility(View.GONE);

		person_iv = (ImageView) findViewById(R.id.person1);
		person_iv.setVisibility(View.GONE);

		descriptionText_tv = (TextView) findViewById(R.id.description_text);
		descriptionText_tv.setOnClickListener(this);
		descriptionText_tv.setVisibility(View.GONE);

		direction_iv = (ImageView) findViewById(R.id.directionimageView1);
		direction_iv.setOnClickListener(this);

		ratingNum_tv = (TextView) findViewById(R.id.ratingNumberTV);
		ratingNum_tv.setVisibility(View.GONE);

		ratingbar = (RatingBar) findViewById(R.id.ratingbarSingle);
		ratingbar.setVisibility(View.GONE);

		// Not using menu for now
//		menuHeader_tv = (TextView) findViewById(R.id.menuHeader);
//		menuInfo_tv = (TextView) findViewById(R.id.menuInfo);
//		menuHeader_tv.setVisibility(View.GONE);
//		menuInfo_tv.setVisibility(View.GONE);

		// Opening Hours
		openingHoursHeader_tv = (TextView) findViewById(R.id.openingHours1);
		openingHoursHeader_tv.setText("DEAL");
		openingHoursHeader_tv.setVisibility(View.GONE);
		day0 = (TextView) findViewById(R.id.sunday);
		day0.setTypeface(tf, Typeface.BOLD);
		day0.setTextSize(22f);
		day0.setVisibility(View.GONE);
		day1 = (TextView) findViewById(R.id.monday);
		day1.setTypeface(tf);
		day1.setTextSize(14f);
		day1.setVisibility(View.GONE);
		day2 = (TextView) findViewById(R.id.tuesday);
		day2.setTypeface(tf);
		day2.setTextSize(14f);
		day2.setVisibility(View.GONE);
		day3 = (TextView) findViewById(R.id.wednesday);
		day3.setVisibility(View.GONE);
		day4 = (TextView) findViewById(R.id.thursday);
		day4.setVisibility(View.GONE);
		day5 = (TextView) findViewById(R.id.friday);
		day5.setVisibility(View.GONE);
		day6 = (TextView) findViewById(R.id.saturday);
		day6.setVisibility(View.GONE);
		loadMoreReviews = (TextView) findViewById(R.id.loadMoreReviews);
		loadMoreReviews.setVisibility(View.GONE);

	}

	private void updateAndShowViews() {

		poweredby_iv.setImageResource(R.drawable.powered_by_groupon);
		poweredby_iv.setVisibility(View.VISIBLE);
		//poweredby_iv.getLayoutParams().height=50;
		//poweredby_iv.getLayoutParams().width=100;

		SharedPreferences spref = PreferenceManager.getDefaultSharedPreferences(SingleDealActivity.this);
		String str = spref.getString("distanceUnit", "NuLL");
		String unit;

		if (str.matches("kmValue")) {
			unit = getString(R.string.km);
		} else {
			unit = getString(R.string.mi);
		}

		// called onPostExecute AsyncTask to ensure we have values to work with
		if (distance != null) {
			distance_tv.setVisibility(View.VISIBLE);
			distance_tv.setText(distance + " " + unit);
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

		if (placePhone == null && placeWeb == null) {
			statusSingle_tv.setText(getString(R.string.further_details_unavailable));
			statusSingle_tv.setTypeface(tf);
		} else {
			statusSingle_tv.setVisibility(View.GONE);
			statusSingle_tv.setTypeface(tf);
		}

		if (placePhone.trim().length() > 0) {
			phone_tv.setText(placePhone);
			phone_tv.setVisibility(View.VISIBLE);
			phone_tv.setOnClickListener(this);
			phone_iv.setVisibility(View.VISIBLE);
			phone_tv.setTypeface(tf);
		}

		if (placeWeb != null) {
			web_tv.setText(placeWeb);
			web_tv.setVisibility(View.VISIBLE);
			web_tv.setOnClickListener(this);
			web_iv.setVisibility(View.VISIBLE);
			web_tv.setTypeface(tf);
		}

		if (placeWeb2 != null) {
			web2_tv.setText(placeWeb2);
			web2_tv.setVisibility(View.VISIBLE);
			web2_tv.setOnClickListener(this);
			web2_iv.setVisibility(View.VISIBLE);
			web2_tv.setTypeface(tf);
		}

		if (reviewContent != null) {
			// show review TextView divider
			review_tv.setVisibility(View.VISIBLE);
			review_tv.setTypeface(tf);
			// update and show descriptionText_tv TextView
			descriptionText_tv.setText(placeReviewCombo.toString());
			descriptionText_tv.setVisibility(View.VISIBLE);
			descriptionText_tv.setTypeface(tf);

			person_iv.setVisibility(View.VISIBLE);

			//rePositionPoweredByGoogle();

		}

		if (desc != null) {

			desc = desc.replace("<a>", "").replace("</a>", "").replace("<a/>", "");

			day0.setText("Get This Deal!" + " " + price);
			day0.setOnClickListener(this);
			day1.setText(title);
			day1.setPadding(3, 0, 3, 0);
			day2.setText(desc);
			day2.setPadding(3, 0, 3, 3);
			loadMoreReviews.setText("Learn More");
			loadMoreReviews.setVisibility(View.VISIBLE);
			loadMoreReviews.setTypeface(tf, Typeface.BOLD);
			loadMoreReviews.setOnClickListener(this);

			openingHoursHeader_tv.setVisibility(View.VISIBLE);
			day0.setVisibility(View.VISIBLE);
			day1.setVisibility(View.VISIBLE);
			day2.setVisibility(View.VISIBLE);

			rePositionLoadMore();

		}

		if (placeRating != null && reviewContent != null) {
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

	}

	private void rePositionLoadMore() {

		//ImageView iv = (ImageView) findViewById(R.id.powered_by_google_singleact);
		RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		layoutParams.addRule(RelativeLayout.BELOW, R.id.tuesday);
		layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
		loadMoreReviews.setLayoutParams(layoutParams);

	}

	private class GetDetailsTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {

			DirectionManager dm = new DirectionManager(SingleDealActivity.this);
			drivingTime = dm.getTravelTime(appLat, appLng, dLat, dLng, "driving");
			walkingTime = dm.getTravelTime(appLat, appLng, dLat, dLng, "walking");

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

	private String getDealToShare() {

		StringBuilder sb = new StringBuilder();
		if (placeName != null) {
			sb.append(getString(R.string.name) + ": " + placeName);
		}
		if (placeAddress != null) {
			sb.append("\n" + getString(R.string.address) + ": " + placeAddress);
		}
		if (placePhone.trim().length() > 0) {
			sb.append("\n" + getString(R.string.phone) + ": " + placePhone);
		}
		if (placeWeb != null) {
			sb.append("\n" + getString(R.string.website) + ": " + placeWeb);
		}
		if (price != null) {
			sb.append("\n" + getString(R.string.price) + ": " + price);
		}

		if (title != null) {
			sb.append("\n" + getString(R.string.deals) + ": " + title);
		}

		if (dealUrl != null) {
			sb.append("\n" + getString(R.string.deals) + " " + getString(R.string.url) + ": " + dealUrl);
		}

		if (desc != null) {
			sb.append("\n" + getString(R.string.about) + ": " + desc);
		}

		sb.append("\n...\n" + getString(R.string.shared_from) + " Arounda Android App");
		sb.append("\n" + "http://play.google.com/store/apps/details?id=" + getPackageName());

		return sb.toString();

	}

	@Override
	public void onClick(View v) {

		DirectionManager dm = new DirectionManager(SingleDealActivity.this);

		switch (v.getId()) {

		case R.id.phone1:
			Intent dialIntent = new Intent("android.intent.action.DIAL");
			dialIntent.setData(Uri.parse("tel:" + phone_tv.getText().toString().trim()));
			startActivity(dialIntent);
			overridePendingTransition(R.anim.right_slide_in, R.anim.left_slide_out); // for starting activity only
			break;

		case R.id.website1:
			String url = web_tv.getText().toString().trim();
			Intent webIntent = new Intent(Intent.ACTION_VIEW);
			webIntent.setData(Uri.parse(url));
			startActivity(webIntent);
			overridePendingTransition(R.anim.right_slide_in, R.anim.left_slide_out); // for starting activity only
			break;

		case R.id.sunday:
			Intent i = new Intent(SingleDealActivity.this, WebViewActivity.class);
			i.putExtra("actionBarTitle", placeName);
			i.putExtra("url", dealUrl);
			startActivity(i);
			overridePendingTransition(R.anim.right_slide_in, R.anim.left_slide_out); // for starting activity only
			break;

		case R.id.loadMoreReviews:

			Intent i2 = new Intent(SingleDealActivity.this, WebViewActivity.class);
			i2.putExtra("actionBarTitle", placeName);
			i2.putExtra("url", dealUrl);
			startActivity(i2);
			overridePendingTransition(R.anim.right_slide_in, R.anim.left_slide_out); // for starting activity only
			break;

		case R.id.shareImageView1:

			if (placeName != null) {
				// if there's something to display,continue 
				String str = getDealToShare();

				Intent intent = new Intent(Intent.ACTION_SEND);
				intent.setType("text/plain");
				intent.putExtra(Intent.EXTRA_TEXT, str);

				// best practice to check if activity to handle text Action_Send intent is present 
				PackageManager packageManager = getPackageManager();
				List<ResolveInfo> activities = packageManager.queryIntentActivities(intent, 0);
				boolean isIntentSafe = activities.size() > 0;

				if (isIntentSafe) {
					// if safe continue 
					startActivity(intent);

				}

			}

			break;

		case R.id.directionimageView1:
			DirectionManager dmd = new DirectionManager(SingleDealActivity.this);
			dmd.performAction(R.drawable.direction_btn, "d", dLat, dLng, dm.getAppLat(), dm.getAppLng());

			break;

		case R.id.streetView_tv:

			Intent si = new Intent(SingleDealActivity.this, StreetViewActivity.class);
			si.putExtra("key_name", placeName);
			si.putExtra("key_addy", placeAddress);
			si.putExtra("key_lat", String.valueOf(dLat));
			si.putExtra("key_lon", String.valueOf(dLng));
			startActivity(si);
			overridePendingTransition(R.anim.right_slide_in, R.anim.left_slide_out); // for starting activity only
			break;

		case R.id.mapView_tv:

			Intent mi = new Intent(SingleDealActivity.this, MapActivitySingle.class);
			mi.putExtra("key_name", placeName);
			mi.putExtra("key_addy", placeAddress);
			mi.putExtra("key_lat", String.valueOf(dLat));
			mi.putExtra("key_lon", String.valueOf(dLng));
			mi.putExtra("getLat", dm.getAppLat());
			mi.putExtra("getLng", dm.getAppLng());
			startActivity(mi);
			overridePendingTransition(R.anim.right_slide_in, R.anim.left_slide_out); // for starting activity only
			break;

		case R.id.textDriving:

			DirectionManager dm1 = new DirectionManager(SingleDealActivity.this);
			dm1.performAction(R.drawable.ic_map_car, "d", dLat, dLng, dm.getAppLat(), dm.getAppLng());

			break;

		case R.id.textWalking:

			DirectionManager dm2 = new DirectionManager(SingleDealActivity.this);
			dm2.performAction(R.drawable.ic_map_walking, "w", dLat, dLng, dm.getAppLat(), dm.getAppLng());
			break;
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
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
}
