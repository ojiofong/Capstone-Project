package com.ojiofong.arounda.adapter;

import java.util.ArrayList;
import java.util.HashMap;

import com.ojiofong.arounda.ui.DealListActivity;
import com.ojiofong.arounda.R.id;
import com.ojiofong.arounda.ViewHolder;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class DealsAdapter extends ArrayAdapter<HashMap<String, String>> {

	Context context;
	int resource;
	LayoutInflater inflater;
	ArrayList<HashMap<String, String>> objects;

	public DealsAdapter(Context context, int resource, ArrayList<HashMap<String, String>> objects) {
		super(context, resource, objects);
		this.context = context;
		this.resource = resource;
		this.inflater = LayoutInflater.from(context);
		this.objects = objects;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			convertView = inflater.inflate(resource, parent, false);
			holder = new ViewHolder();
			holder.placeName = (TextView) convertView.findViewById(id.merchantName);
			holder.title = (TextView) convertView.findViewById(id.title);
			holder.distance = (TextView) convertView.findViewById(id.distance);
			holder.price = (TextView) convertView.findViewById(id.price);
			holder.placeAddress = (TextView) convertView.findViewById(id.completeAddress);
			holder.otherLocations = (TextView)convertView.findViewById(id.otherLocations);
			holder.otherLocations.setVisibility(View.GONE);
			

			convertView.setTag(holder);

		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		HashMap<String, String> currentP = new HashMap<>();
		currentP = objects.get(position);
		
		holder.placeName.setText(currentP.get(DealListActivity.KEY_MERCHANTNAME));
		holder.title.setText(currentP.get(DealListActivity.KEY_TITLE));
		holder.price.setText(currentP.get(DealListActivity.KEY_PRICE));
		holder.placeAddress.setText(currentP.get(DealListActivity.KEY_COMPLETE_ADDRESS));
		
		SharedPreferences defaultSharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
		String str = defaultSharedPrefs.getString("distanceUnit", "NULL");

		 if (str.matches("kmValue")) {
			holder.distance.setText(currentP.get(DealListActivity.KEY_DISTANCE) + " km");
		} else {
			//use default in miles
			holder.distance.setText(currentP.get(DealListActivity.KEY_DISTANCE) + " mi");
		}
		 int numOfLocations = Integer.valueOf(currentP.get(DealListActivity.KEY_NUMBER_OF_LOCATIONS));
		 if (numOfLocations > 1) {
			holder.otherLocations.setText("Redemption Locations: " + numOfLocations );
			holder.otherLocations.setVisibility(View.VISIBLE);
		}

		return convertView;
	}
}
