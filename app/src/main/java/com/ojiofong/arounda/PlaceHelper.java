package com.ojiofong.arounda;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.ojiofong.arounda.data.Place;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

public class PlaceHelper {

	private String API_KEY;

	public PlaceHelper(String apikey) {
		this.API_KEY = apikey;
	}

	public void setApiKey(String apikey) {
		this.API_KEY = apikey;
	}

	public ArrayList<Place> findPlaces(Double latitude, Double longitude, String placeSpecification, Double radius, Boolean usingSearchBar,
			String searchBarValue, String pagetoken, Context context) {

		String urlString = makeUrl(latitude, longitude, placeSpecification, radius, usingSearchBar, searchBarValue, pagetoken);

		try {
			String json = getUrlContents(urlString);

			//System.out.println(json);
			JSONObject object = new JSONObject(json);

			try {
				String next_page_token = object.getString("next_page_token");

				SharedPreferences pref = context.getSharedPreferences("MyPref", 0);
				Editor editor = pref.edit();
				editor.putString("next_page_token", next_page_token); // Storing
																		// string
				editor.commit(); // commit changes

			} catch (JSONException e) {

				SharedPreferences pref = context.getSharedPreferences("MyPref", 0); // 
				Editor editor = pref.edit();
				editor.putString("next_page_token", "empty"); // Storing string
				editor.commit(); // commit changes
			}

			JSONArray jsonArray = object.getJSONArray("results");

			ArrayList<Place> arrayList = new ArrayList<Place>();
			for (int i = 0; i < jsonArray.length(); i++) {
				try {
					Place place = Place.buildPlace((JSONObject) jsonArray.get(i));
					arrayList.add(place);
				} catch (Exception e) {
				}
			}
			return arrayList;
		} catch (JSONException ex) {
		}
		return null;
	}

	// https://maps.googleapis.com/maps/api/place/search/json?location=28.632808,77.218276&radius=500&types=atm&sensor=false&key=apikey
	private String makeUrl(Double latitude, Double longitude, String place, Double radius, Boolean usingSearchBar, String searchBarValue, String pagetoken) {
		StringBuilder urlString;

		if (usingSearchBar) {
			urlString = new StringBuilder("https://maps.googleapis.com/maps/api/place/textsearch/json?");
			urlString.append("location=");
			urlString.append(Double.toString(latitude));
			urlString.append(",");
			urlString.append(Double.toString(longitude));
			urlString.append("&query=");
			try {
				urlString.append(java.net.URLEncoder.encode(searchBarValue, "utf-8"));
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			urlString.append("&radius=");
			urlString.append(Double.toString(radius));
			urlString.append("&sensor=false&key=").append(API_KEY);
		} else {
			urlString = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
			urlString.append("location=");
			urlString.append(Double.toString(latitude));
			urlString.append(",");
			urlString.append(Double.toString(longitude));
			// urlString.append("&radius=");
			// urlString.append(Double.toString(radius));
			// urlString.append("&radius=1000");
			urlString.append("&types=").append(place);
			urlString.append("&rankby=distance");
			urlString.append("&sensor=false&key=").append(API_KEY);
		}

		if (pagetoken != null && pagetoken != "") {
			urlString.append("&pagetoken=" + pagetoken);
		}
		return urlString.toString();
	}


	private String getUrlContents(String theUrl) {
		StringBuilder content = new StringBuilder();
		try {
			URL url = new URL(theUrl);
			URLConnection urlConnection = url.openConnection();
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()), 8);
			String line;
			while ((line = bufferedReader.readLine()) != null) {
				content.append(line).append("\n");
			}
			bufferedReader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return content.toString();
	}

}
