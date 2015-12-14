package com.ojiofong.arounda.ui;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.ojiofong.arounda.utils.AppManager;
import com.ojiofong.arounda.DealCategoryFragmentDialog;
import com.ojiofong.arounda.DealCategoryFragmentDialog.DealCategoryListener;
import com.ojiofong.arounda.R;
import com.ojiofong.arounda.adapter.DealsAdapter;
import com.ojiofong.arounda.utils.DirectionManager;

public class DealListActivity extends ActionBarActivity implements OnClickListener, DealCategoryListener {

	private Double appLat, appLng;
	protected static ArrayList<HashMap<String, String>> dealsList = new ArrayList<HashMap<String, String>>();
	private ListView listview;
	private View footerView;
	private ImageView poweredby_iv;
	private TextView loadmore;
	private DealsAdapter adapter;
	private DirectionManager dm;
	private int offset = 0;
	private int limit = 30;
	private String actionBarTitle, filterCategory;

	// KEY Strings
	public static String KEY_MERCHANTNAME = "merchant_name";
	public static String KEY_MERCHANT_WEBSITE = "merchant_website";
	public static String KEY_DEALURL = "deal_url";
	public static String KEY_TITLE = "title";
	public static String KEY_PRICE = "price";
	public static String KEY_FINAL_LAT = "final_lat";
	public static String KEY_FINAL_LNG = "final_lng";
	public static String KEY_REDEMPTION_LAT = "redemption_lat";
	public static String KEY_REDEMPTION_LNG = "redemption_lng";
	public static String KEY_DESCRIPTION = "description";
	public static String KEY_COMPLETE_ADDRESS = "complete_address";
	public static String KEY_DISTANCE = "distance";
	public static String KEY_PHONE = "phone";
	public static String KEY_NUMBER_OF_LOCATIONS = "number_of_locations";

	@Override
	protected void onCreate(Bundle arg0) {
		// TODO Auto-generated method stub
		super.onCreate(arg0);
		setContentView(R.layout.activity_place_list);
		new AppManager(this).setStatusBarColorForKitKat(getResources().getColor(R.color.colorPrimaryDark));
		initToolBar();
		initialize();
		new GetDealsTask().execute(filterCategory);
	}

	private void initToolBar() {

		// initialize ToolBar		
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_NextActivity);
		setSupportActionBar(toolbar);

		actionBarTitle = getString(R.string.deals);
		getSupportActionBar().setTitle(actionBarTitle);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

	}

	private void initialize() {
		dealsList = new ArrayList<HashMap<String, String>>();
		filterCategory = "no_filter";

		((ImageView) findViewById(R.id.defaultIV)).setVisibility(View.GONE);
		((TextView) findViewById(R.id.defaultText)).setVisibility(View.GONE);

		dm = new DirectionManager(DealListActivity.this);
		appLat = dm.getAppLat();
		appLng = dm.getAppLng();

		listview = (ListView) findViewById(R.id.list);

		footerView = getLayoutInflater().inflate(R.layout.load_more_row, listview, false);
		poweredby_iv = (ImageView) footerView.findViewById(R.id.powered_by_google_loadmore);
		poweredby_iv.setImageResource(R.drawable.powered_by_groupon);
		poweredby_iv.setVisibility(View.INVISIBLE);
		poweredby_iv.setClickable(false);
		loadmore = (TextView) footerView.findViewById(R.id.loadMore);
		loadmore.setVisibility(View.INVISIBLE);
		loadmore.setOnClickListener(this);
		footerView.setClickable(false);
		listview.addFooterView(footerView);
		//footerView.setVisibility(View.GONE);
	}

	private void populateList() {

		loadmore.setClickable(true); //re-enable click
		loadmore.setVisibility(View.VISIBLE);
		poweredby_iv.setVisibility(View.VISIBLE);

		adapter = new DealsAdapter(DealListActivity.this, R.layout.listitem_deal, dealsList);
		listview.setAdapter(adapter);
		listview.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				// TODO Auto-generated method stub
				if (position == dealsList.size()) {
					//Catching uncaught IndexOutOfBoundsException
					return;
				}
				try {
					Intent i = new Intent(DealListActivity.this, SingleDealActivity.class);
					i.putExtra("position", position);
					i.putExtra("dealsList", dealsList);
					startActivity(i);
					overridePendingTransition(R.anim.right_slide_in, R.anim.left_slide_out); // for starting activity only
				} catch (IndexOutOfBoundsException e) {
					// TODO: handle exception
				}
			}
		});

		if (dealsList.isEmpty()) {
			Toast.makeText(DealListActivity.this, getString(R.string.no_more_data), Toast.LENGTH_SHORT).show();
			loadmore.setVisibility(View.GONE);
		}

		getSupportActionBar().setTitle(actionBarTitle);

	}

	private String GetUrlContent(String category, double lat, double lng) {

		String str = "https://partner-api.groupon.com/deals.json?tsToken=US_AFF_0_202728_212556_0";

		StringBuilder content = new StringBuilder();
		StringBuilder sb = new StringBuilder(str);
		sb.append("&offset=" + offset);
		sb.append("&limit=" + limit);
		sb.append("&lat=" + lat);
		sb.append("&lng=" + lng);

		if (!category.matches("no_filter")) {

			try {
				sb.append("&filters=category:" + java.net.URLEncoder.encode(category, "utf-8"));
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}

		try {
			URL url = new URL(sb.toString());
			URLConnection con = url.openConnection();
			BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"));
			String line;
			while ((line = reader.readLine()) != null) {
				content.append(line + "\n");
				reader.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return content.toString();

	}

	private class GetDealsTask extends AsyncTask<String, Void, Void> {

		String phone, merchantName, merchantWebsite, title, price, divisionLat, divisionLng, redemptionLat, redemptionLng, finalLat, finalLng, desc, city, state, address,
				completeAddress, dealUrl;
		int numberOfLocations = 0;
		JSONArray dealsArray;

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			loadmore.setClickable(false); //disable click to prevent double clicking

			((View) findViewById(R.id.progressBarLayout)).setVisibility(View.VISIBLE);
		}

		@Override
		protected Void doInBackground(String... params) {
			// TODO Auto-generated method stub
			String s = GetUrlContent(params[0], appLat, appLng);

			try {
				JSONObject jsonobject = new JSONObject(s);
				dealsArray = jsonobject.getJSONArray("deals");
			} catch (JSONException e) {
				e.printStackTrace();
			}

			if (dealsArray.length() > 0) {

				for (int i = 0; i < dealsArray.length(); i++) {
					try {
						merchantName = dealsArray.getJSONObject(i).getJSONObject("merchant").optString("name", null);
						merchantWebsite = dealsArray.getJSONObject(i).getJSONObject("merchant").optString("websiteUrl", null);
						title = dealsArray.getJSONObject(i).getJSONArray("options").getJSONObject(0).optString("title", null);
						price = dealsArray.getJSONObject(i).getJSONArray("options").getJSONObject(0).getJSONObject("price").optString("formattedAmount", null);

						divisionLat = dealsArray.getJSONObject(i).getJSONObject("division").optString("lat", null);
						divisionLng = dealsArray.getJSONObject(i).getJSONObject("division").optString("lng", null);
						dealUrl = dealsArray.getJSONObject(i).optString("dealUrl", null);

						phone = dealsArray.getJSONObject(i).getJSONArray("options").getJSONObject(0).getJSONArray("redemptionLocations").getJSONObject(0)
								.optString("phoneNumber", null);
						city = dealsArray.getJSONObject(i).getJSONArray("options").getJSONObject(0).getJSONArray("redemptionLocations").getJSONObject(0).optString("city", null);
						state = dealsArray.getJSONObject(i).getJSONArray("options").getJSONObject(0).getJSONArray("redemptionLocations").getJSONObject(0).optString("state", null);
						address = dealsArray.getJSONObject(i).getJSONArray("options").getJSONObject(0).getJSONArray("redemptionLocations").getJSONObject(0)
								.optString("streetAddress1", null);
						redemptionLat = dealsArray.getJSONObject(i).getJSONArray("options").getJSONObject(0).getJSONArray("redemptionLocations").getJSONObject(0)
								.optString("lat", null);
						redemptionLng = dealsArray.getJSONObject(i).getJSONArray("options").getJSONObject(0).getJSONArray("redemptionLocations").getJSONObject(0)
								.optString("lng", null);
						numberOfLocations = dealsArray.getJSONObject(i).getJSONArray("options").getJSONObject(0).getJSONArray("redemptionLocations").length();
						completeAddress = address + ", " + city + ", " + state;
						desc = dealsArray.getJSONObject(i).getJSONArray("options").getJSONObject(1).getJSONArray("details").getJSONObject(0).optString("description", null);

						if (redemptionLat != null || redemptionLng != null) {
							finalLat = redemptionLat;
							finalLng = redemptionLng;
						} else {
							finalLat = divisionLat;
							finalLng = divisionLng;
						}

						HashMap<String, String> map = new HashMap<String, String>();
						map.put(KEY_MERCHANTNAME, merchantName);
						map.put(KEY_MERCHANT_WEBSITE, merchantWebsite);
						map.put(KEY_TITLE, title);
						map.put(KEY_PRICE, price);
						map.put(KEY_FINAL_LAT, finalLat);
						map.put(KEY_FINAL_LNG, finalLng);
						map.put(KEY_DEALURL, dealUrl);
						map.put(KEY_PHONE, phone);
						map.put(KEY_COMPLETE_ADDRESS, completeAddress);
						map.put(KEY_DESCRIPTION, desc);
						map.put(KEY_DISTANCE, dm.calculateDistance(appLat, appLng, Double.parseDouble(finalLat), Double.parseDouble(finalLng)) + "");
						map.put(KEY_NUMBER_OF_LOCATIONS, numberOfLocations + "");
						dealsList.add(map);

					} catch (JSONException e) {
						e.printStackTrace();
					}

				}

			}

			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			populateList();

			//dismiss the progressBar at the end
			((View)findViewById(R.id.progressBarLayout)).setVisibility(View.GONE);

		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.next, menu);
		MenuItem item = menu.findItem(R.id.action_dealCategory);
		item.setVisible(true);
		activateSpinner();
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
			break;

		case R.id.action_mapview:

			Intent i = new Intent(DealListActivity.this, MapActivityDeal.class);
			i.putExtra("actionTitle", actionBarTitle);
			i.putExtra("key", GoogleMap.MAP_TYPE_NORMAL);
			startActivity(i);
			overridePendingTransition(R.anim.right_slide_in, R.anim.left_slide_out); // for starting activity only

			break;

		case R.id.action_dealCategory:
			//show change password DialogFragment
			FragmentManager fm = getSupportFragmentManager();
			DealCategoryFragmentDialog dialogFragment = new DealCategoryFragmentDialog();
			dialogFragment.show(fm, "DealCategoryFragmentDialog");

			//pass username to DialogFragment
			//Bundle args = new Bundle();
			//args.putString("username", usernameFromintent);
			//dialogFragment.setArguments(args);

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
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.loadMore:
			offset += 30;
			limit += 30;
			if (limit <= 90) {
				new GetDealsTask().execute(filterCategory);
				// Reset spinner to initial position
				Spinner spinner = (Spinner) findViewById(R.id.spinner_nav);
				spinner.setSelection(0);
			} else {
				Toast.makeText(DealListActivity.this, getString(R.string.no_more_data), Toast.LENGTH_SHORT).show();
				loadmore.setVisibility(View.GONE);
			}
			break;
		}

	}

	private void activateSpinner() {

		String[] items = { getString(R.string.sortby), getString(R.string.distance), getString(R.string.name), getString(R.string.price) };

		Spinner spinner = (Spinner) findViewById(R.id.spinner_nav);
		ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(getSupportActionBar().getThemedContext(), android.R.layout.simple_spinner_item, items); //selected item will look like a spinner set from XML
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
		// TODO Auto-generated method stub
		sortList(itemPosition);
		return true;
	}

	private void sortList(int itemPosition) {
		if (itemPosition == 1) {
			//Distance
			Collections.sort(dealsList, new Comparator<HashMap<String, String>>() {

				@Override
				public int compare(HashMap<String, String> lhs, HashMap<String, String> rhs) {
					Double first = Double.valueOf(lhs.get(KEY_DISTANCE));
					Double second = Double.valueOf(rhs.get(KEY_DISTANCE));
					return first.compareTo(second);
				}
			});

		} else if (itemPosition == 2) {
			//Name
			Collections.sort(dealsList, new Comparator<HashMap<String, String>>() {

				@Override
				public int compare(HashMap<String, String> lhs, HashMap<String, String> rhs) {
					String first = lhs.get(KEY_MERCHANTNAME);
					String second = rhs.get(KEY_MERCHANTNAME);
					return first.compareToIgnoreCase(second);
				}
			});
		} else if (itemPosition == 3) {
			//Price
			Collections.sort(dealsList, new Comparator<HashMap<String, String>>() {

				@Override
				public int compare(HashMap<String, String> lhs, HashMap<String, String> rhs) {

					String sLHS = lhs.get(KEY_PRICE);
					String sRHS = rhs.get(KEY_PRICE);

					Double first = null;
					Double second = null;

					//validation to remove $, C$ sign from price

					Matcher m = Pattern.compile("(?!=\\d\\.\\d\\.)([\\d.]+)").matcher(sLHS);
					while (m.find()) {
						first = Double.valueOf(m.group(1));
					}

					Matcher m2 = Pattern.compile("(?!=\\d\\.\\d\\.)([\\d.]+)").matcher(sRHS);
					while (m2.find()) {
						second = Double.valueOf(m2.group(1));
					}

					return first.compareTo(second);
				}
			});
		}

		adapter = new DealsAdapter(DealListActivity.this, R.layout.listitem_deal, dealsList);
		listview.setAdapter(adapter);

	}

	@Override
	public void onCategoryListClicked(String categoryID, String categoryName) {
		// TODO Auto-generated method stub
		if (categoryName.matches(getString(R.string.all))) {
			actionBarTitle = getString(R.string.deals);
		} else {
			actionBarTitle = getString(R.string.deals) + " - " + categoryName;
		}
		loadmore.setVisibility(View.INVISIBLE);
		poweredby_iv.setVisibility(View.INVISIBLE);
		filterCategory = categoryID;
		dealsList.clear();
		//adapter.notifyDataSetChanged();
		new GetDealsTask().execute(filterCategory);

	}
}
