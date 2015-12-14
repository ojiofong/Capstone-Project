package com.ojiofong.arounda.ui;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.ContextThemeWrapper;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.google.gson.Gson;
import com.ojiofong.arounda.R;
import com.ojiofong.arounda.Wrapper;
import com.ojiofong.arounda.adapter.TheAdapter;
import com.ojiofong.arounda.utils.Configuration;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;


public class ChangeLocationActivity extends ActionBarActivity implements OnClickListener {

    ImageButton searchButton;
    Button streetView_b;
    TextView tvUseCurrentLocation;
    ArrayAdapter<String> adapter;
    AutoCompleteTextView acTextView;
    ArrayList<HashMap<String, String>> arrayListOfMap = new ArrayList<HashMap<String, String>>();
    ArrayAdapter<HashMap<String, String>> listadapter;
    String str;
    JSONArray myJsonArray;
    View headerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.changelocation);
      //  new AppManager(this).setStatusBarColorForKitKat(getResources().getColor(R.color.colorPrimaryDark));

        initToolBar();
        initialize();
        retrieveArrayList();
        listadapter = new TheAdapter(getApplicationContext(), arrayListOfMap);
        populateListView();
        adapterToAutoTV();
    }


    private void initToolBar() {

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setElevation(0);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.change_location));

    }

    @SuppressLint("InflateParams")
    private void initialize() {

        headerView = getLayoutInflater().inflate(R.layout.listitem_textview, null);

        searchButton = (ImageButton) findViewById(R.id.searchAutoComp_b);
        searchButton.setOnClickListener(this);

        streetView_b = (Button) findViewById(R.id.streetview_b);
        streetView_b.setOnClickListener(this);

        tvUseCurrentLocation = (TextView) headerView.findViewById(R.id.tvalone);
        tvUseCurrentLocation.setText(getString(R.string.use_current_location));
        tvUseCurrentLocation.setTypeface(null, Typeface.BOLD);
        tvUseCurrentLocation.setTextSize(17.0f);
        tvUseCurrentLocation.setBackgroundResource(R.drawable.background_states);
        tvUseCurrentLocation.setOnClickListener(this);

        // Initialize AutoCompleteTextView
        acTextView = (AutoCompleteTextView) findViewById(R.id.autoCompleteTextView_real);
        acTextView.setHint(getString(R.string.enter_few_letters));

        // Overriding Enter key for autoCompleteTextView
        acTextView.setOnEditorActionListener(new OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                if (event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {

                    //Hide keyboard (No need so user can keep typing)
                    //	InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    //	in.hideSoftInputFromWindow(acTextView.getWindowToken(), 0);

                    //perform button click programatically
                    searchButton.performClick();

                    //Trying to press back or Delete Button since for some reason is the way to show autocompListView from this hack
                    //		KeyEvent myevent = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL);
                    //		Activity a = ChangeLocationActivity.this;
                    //		a.dispatchKeyEvent(myevent);

                    //Better work around to show autoComp dropView than hacking keyboard Event
                    //Using "setSelection" instead of "setText" to position cursor at end
                    acTextView.setSelection(acTextView.getText().length());

                    return true;
                }

                return false;
            }
        });

    }

    private void populateListAndChangeLocation(String desc, String lat, String lng) {
        // called on post execute of TheTask class
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("description", desc);
        map.put("latitude", lat);
        map.put("longitude", lng);

        arrayListOfMap.add(map);
        listadapter.notifyDataSetChanged();
        //acTextView.setText("");

        saveArrayList();

        // Let's change the Location Finally to this latitude and longitude
        // Holding selected the values in sharedPreferences
        SharedPreferences.Editor editor = getSharedPreferences("settings", Context.MODE_PRIVATE).edit();
        editor.putBoolean("usecurrentloc_Pref", false);
        editor.putString("lat_Pref", lat);
        editor.putString("lng_Pref", lng);
        editor.putString("item_desc", desc);
        editor.commit();

        Toast.makeText(getApplicationContext(), getString(R.string.new_location_updated), Toast.LENGTH_SHORT).show();

        finish();
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right); //exit animation

    }

    private void saveArrayList() {
        // called in populateList method
        // convert ArrayList with Gson to json and save as string in sharedPreferences
        Wrapper wrapper = new Wrapper();
        wrapper.setWrapperList(arrayListOfMap);

        Gson gson = new Gson();
        String s = gson.toJson(wrapper);

        //	System.out.println("GSON OUTPUT: " + s);

        SharedPreferences.Editor editor = getSharedPreferences("settings", Context.MODE_PRIVATE).edit();
        editor.putString("gsonList_Pref", s);
        editor.commit();

    }

    private void retrieveArrayList() {
        // called on onCreate Bundle to retrieve ArrayList if present
        SharedPreferences getPref = getSharedPreferences("settings", Context.MODE_PRIVATE);
        String str = getPref.getString("gsonList_Pref", null);

        if (str != null) { // do only if not null

            Gson gson = new Gson();
            Wrapper wrapper = gson.fromJson(str, Wrapper.class);
            ArrayList<HashMap<String, String>> retrievedList = wrapper.getWrapperList();
            arrayListOfMap = retrievedList;

        }

    }

    private void populateListView() {

        ListView listview = (ListView) findViewById(R.id.listView1);
        listview.addHeaderView(headerView); //must call before set adapter
        listview.setAdapter(listadapter);

        listview.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // plus one because header is now position 0.
                changeToSelectedLocation(position - 1);

            }
        });

        registerForContextMenu(listview);
    }

    private void adapterToAutoTV() {
        adapter = new ArrayAdapter<String>(this, R.layout.listitem_autocomplete);
        adapter.setNotifyOnChange(true);
        acTextView.setAdapter(adapter);

        acTextView.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                /*
				 * Right below that, we setup our text watcher. This will run
				 * every time the text changes. This is pretty crude on my
				 * part,but to limit the amount of calls to the webService, I'm
				 * checking every 3 characters. We call GetPlaces and run it as
				 * an asynchronous task. if (count % 3 == 1)
				 */
                // if (count % 4 == 1) {
                // we don't want to make an insanely large array, so we clear it each time
                // adapter.clear();
                // create the task

                // GetPlaces task = new GetPlaces();
                // now pass the argument in the autoCompleteTextView to the task
                // task.execute(s.toString());

                // Toast.makeText(getApplicationContext(), ""+str, Toast.LENGTH_SHORT).show();

                // }

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // TODO Auto-generated method stub

            }

            @Override
            public void afterTextChanged(Editable s) {
                // TODO Auto-generated method stub

            }
        });

        acTextView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String description = new String();
                String reference = new String();

                // TODO Auto-generated method stub
                try {

                    description = myJsonArray.getJSONObject(position).getString("description").toString();
                    reference = myJsonArray.getJSONObject(position).getString("reference").toString();

                    // Toast.makeText(getApplicationContext(), "" + description, Toast.LENGTH_SHORT).show();

                    String[] strArray = {reference, description};

                    GetFurtherDetailsTask furtherTask = new GetFurtherDetailsTask();
                    furtherTask.execute(strArray);

                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }
        });

    }

    private void changeToSelectedLocation(int pos) {

        // String itemDescr = arrayListOfMap.get(pos).get("description");
        String itemLat = arrayListOfMap.get(pos).get("latitude");
        String itemLng = arrayListOfMap.get(pos).get("longitude");
        String itemDesc = arrayListOfMap.get(pos).get("description");

        // Holding selected map values in sharedPreferences
        SharedPreferences.Editor editor = getSharedPreferences("settings", Context.MODE_PRIVATE).edit();
        editor.putBoolean("usecurrentloc_Pref", false);
        editor.putString("lat_Pref", itemLat);
        editor.putString("lng_Pref", itemLng);
        editor.putString("item_desc", itemDesc);
        editor.commit();

        Toast.makeText(getApplicationContext(), getString(R.string.new_location_updated), Toast.LENGTH_SHORT).show();
        finish();
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right); //exit animation
    }

    private class GetPlaces extends AsyncTask<String, Void, ArrayList<String>> {

        @Override
        protected ArrayList<String> doInBackground(String... params) {

            ArrayList<String> predictionsArr = new ArrayList<String>();

            try {
                URL googlePlaces = new URL(
                        // URLEncoder.encode(url,"UTF-8");
                        "https://maps.googleapis.com/maps/api/place/autocomplete/json?input=" + URLEncoder.encode(params[0].toString(), "UTF-8")
                                + "&types=geocode&language=en&sensor=false&key=" + Configuration.getApiKey());

                URLConnection conn = googlePlaces.openConnection();
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                String line;
                StringBuffer sb = new StringBuffer();
                // take Google's legible JSON and turn it into one big string.
                while ((line = in.readLine()) != null) {
                    sb.append(line);
                }

                // turn that string into a JSON object
                JSONObject predictions = new JSONObject(sb.toString());
                // now get the JSON array that's inside that object
                JSONArray ja = new JSONArray(predictions.getString("predictions"));

                myJsonArray = ja; // to hold array globally

                for (int i = 0; i < ja.length(); i++) {
                    JSONObject jo = (JSONObject) ja.get(i);
                    // add each entry to our array
                    predictionsArr.add(jo.getString("description"));
                }

            } catch (IOException e) {

            } catch (JSONException e) {

                e.printStackTrace();
            }

            return predictionsArr;
        }

        @Override
        protected void onPostExecute(ArrayList<String> result) {
            // TODO Auto-generated method stub

            //	for (int i = 0; i < result.size(); i++) {
            //		System.out.println("description" + i + ": " + result.get(i));
            //	}

            // update the adapter
            adapter = new ArrayAdapter<String>(getBaseContext(), R.layout.listitem_autocomplete);
            adapter.setNotifyOnChange(true);
            // attach the adapter to textView
            acTextView.setAdapter(adapter);

            super.onPostExecute(result);

            for (String string : result) {

                adapter.add(string);
                adapter.notifyDataSetChanged();

            }
        }

    }

    // Additional methods to retrieve lat and lng starts here ---------
    private String getUrlContents(String reference) {

        StringBuilder urlString;

        urlString = new StringBuilder("https://maps.googleapis.com/maps/api/place/details/json?");
        urlString.append("reference=");
        urlString.append(reference);
        urlString.append("&sensor=false&key=" + Configuration.getApiKey());

        StringBuilder content = new StringBuilder();
        try {
            URL url = new URL(urlString.toString());
            URLConnection urlConnection = url.openConnection();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()), 8);
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

    private class GetFurtherDetailsTask extends AsyncTask<String, Void, Void> {
        String jsonResult;
        String myReference;
        String myDescription;
        String latitude;
        String longitude;
        JSONObject jsonobject;

        @Override
        protected Void doInBackground(String... params) {

            myReference = params[0];
            myDescription = params[1];
            jsonResult = getUrlContents(myReference);

            try {
                jsonobject = new JSONObject(jsonResult).getJSONObject("result").getJSONObject("geometry").getJSONObject("location");

                latitude = jsonobject.getString("lat").toString();
                longitude = jsonobject.getString("lng").toString();

            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {

            populateListAndChangeLocation(myDescription, latitude, longitude);

            super.onPostExecute(result);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // TODO Auto-generated method stub
        getMenuInflater().inflate(R.menu.main, menu);

        // disable and hide unwanted MenuItems
        MenuItem changeLocItem = menu.findItem(R.id.action_changeLocation);
        changeLocItem.setEnabled(false).setVisible(false);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        searchItem.setVisible(false);
        MenuItem settingsItem = menu.findItem(R.id.action_settings);
        settingsItem.setVisible(false);
        MenuItem searchItemNull = menu.findItem(R.id.action_search_null);
        searchItemNull.setVisible(false);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // TODO Auto-generated method stub
        switch (item.getItemId()) {

            case android.R.id.home:
                finish();
                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right); //exit animation
                break;

            case R.id.action_DeleteHistory:


                Context mContext = new ContextThemeWrapper(this, R.style.AppTheme);

                // Make AlertDialog to confirm
                new AlertDialog.Builder(mContext).setTitle(getString(R.string.delete_history)).setMessage(getString(R.string.delete_history_message)).setCancelable(true)
                        .setNegativeButton(getString(R.string.cancel), null).setPositiveButton(getString(R.string.delete), new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // When Delete is Clicked
                        // clear list adapter
                        listadapter.clear();
                        listadapter.notifyDataSetChanged();

                        // Now null sharedPreferences to avoid retrieving list
                        // And Restore Default Current Location
                        SharedPreferences.Editor editor = getSharedPreferences("settings", Context.MODE_PRIVATE).edit();
                        editor.putString("gsonList_Pref", null);
                        editor.putBoolean("usecurrentloc_Pref", true);
                        editor.commit();

                        Toast.makeText(getApplicationContext(), getString(R.string.history_deleted), Toast.LENGTH_SHORT).show();

                        // finish the activity to kill it off the stack and return to mainActivity
                        finish();
                        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right); //exit animation

                    }
                }).show();

                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {
            case R.id.tvalone:

                // Restore Default Current Location
                SharedPreferences.Editor editor = getSharedPreferences("settings", Context.MODE_PRIVATE).edit();
                editor.putBoolean("usecurrentloc_Pref", true);
                editor.commit();
                // then create Toast and return to home screen
                Toast.makeText(getApplicationContext(), getString(R.string.now_using_current_location), Toast.LENGTH_SHORT).show();
                finish();
                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right); //exit animation

                break;

            case R.id.searchAutoComp_b:

                // using search button instead of on text change to reduce calls to Google Api

                String s = acTextView.getText().toString();

                if (!s.matches("")) {
                    // we don't want to make an insanely large array, so we clear it each time
                    adapter.clear();

                    GetPlaces task = new GetPlaces();
                    // now pass the argument in the autoCompleteTextView to the task
                    task.execute(s);
                }

                break;

            case R.id.streetview_b:

                SharedPreferences getPref = getSharedPreferences("settings", 0);
                boolean usingCurrentLocation = getPref.getBoolean("usecurrentloc_Pref", true); // default to true if nothing
                String addyDesc = getPref.getString("item_desc", null);

                String latString,
                        lngString;

                if (usingCurrentLocation) {

                    latString = getIntent().getStringExtra("latString");
                    lngString = getIntent().getStringExtra("lngString");
                    addyDesc = getString(R.string.current_location);

                } else {

                    latString = getPref.getString("lat_Pref", null);
                    lngString = getPref.getString("lng_Pref", null);
                    addyDesc = getPref.getString("item_desc", null);
                }

                Intent si = new Intent(getApplicationContext(), StreetViewActivity.class);
                si.putExtra("key_name", getString(R.string.street_view));
                si.putExtra("key_addy", addyDesc);
                si.putExtra("key_lat", latString);
                si.putExtra("key_lon", lngString);
                startActivity(si);
                overridePendingTransition(R.anim.right_slide_in, R.anim.left_slide_out); // for starting activity only

                break;
        }

    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        if (v.getId() == R.id.listView1) {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
            String headerTitle = this.listadapter.getItem(info.position - 1).get("description"); //-1 bCos of headerView
            menu.setHeaderTitle(headerTitle);
            String[] MENU_ITEMS = {getString(R.string.change_location), getString(R.string.delete), getString(R.string.cancel)};
            for (int i = 0; i < MENU_ITEMS.length; i++) {
                menu.add(Menu.NONE, i, i, MENU_ITEMS[i]);
            }
        }
        super.onCreateContextMenu(menu, v, menuInfo);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {

        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

        switch (item.getItemId()) {
            case 0: //change location

                int pos = info.position - 1; //-1 bCos of headerView
                String itemLat = arrayListOfMap.get(pos).get("latitude");
                String itemLng = arrayListOfMap.get(pos).get("longitude");
                String itemDesc = arrayListOfMap.get(pos).get("description");

                // Holding selected map values in sharedPreferences
                SharedPreferences.Editor editor = getSharedPreferences("settings", Context.MODE_PRIVATE).edit();
                editor.putBoolean("usecurrentloc_Pref", false);
                editor.putString("lat_Pref", itemLat);
                editor.putString("lng_Pref", itemLng);
                editor.putString("item_desc", itemDesc);
                editor.commit();

                Toast.makeText(getApplicationContext(), getString(R.string.new_location_updated), Toast.LENGTH_SHORT).show();
                finish();
                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right); //exit animation

                break;

            case 1: //delete

                arrayListOfMap.remove(info.position - 1);
                listadapter.notifyDataSetChanged();
                saveArrayList();

                break;

            case 2: //cancel
                //do nothing
                break;
        }
        return super.onContextItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        // TODO Auto-generated method stub
        super.onBackPressed();
        this.finish();
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right); //exit animation

    }
}
