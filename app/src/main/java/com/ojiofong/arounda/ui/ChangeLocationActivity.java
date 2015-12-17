package com.ojiofong.arounda.ui;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
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

import com.ojiofong.arounda.R;
import com.ojiofong.arounda.adapter.LocationHistoryAdapter;
import com.ojiofong.arounda.db.LocationHistory;
import com.ojiofong.arounda.utils.Configuration;
import com.ojiofong.arounda.utils.Const;

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


public class ChangeLocationActivity extends AppCompatActivity implements OnClickListener, LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = ChangeLocationActivity.class.getSimpleName();
    ImageButton searchButton;
    Button streetView_b;
    TextView tvUseCurrentLocation;
    ArrayAdapter<String> adapter;
    AutoCompleteTextView acTextView;
    JSONArray myJsonArray;
    View headerView;
    private static final int LOADER_ID = 7;
    LocationHistoryAdapter locationHistoryAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.changelocation);
        //  new AppManager(this).setStatusBarColorForKitKat(getResources().getColor(R.color.colorPrimaryDark));

        initToolBar();
        initialize();
        setUpListView();
        adapterToAutoTV();

        getSupportLoaderManager().initLoader(LOADER_ID, null, this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getSupportLoaderManager().restartLoader(LOADER_ID, null, this);
    }

    private void initToolBar() {

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setElevation(0);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(getString(R.string.change_location));

        }


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

    private void saveToDbAndChangeLocation(String vicinity, String lat, String lng) {
        insertLocationItem(vicinity, lat, lng);

        // Let's change the Location Finally to this latitude and longitude
        // Holding selected the values in sharedPreferences
        SharedPreferences.Editor editor = getSharedPreferences("settings", Context.MODE_PRIVATE).edit();
        editor.putBoolean("usecurrentloc_Pref", false);
        editor.putString("lat_Pref", lat);
        editor.putString("lng_Pref", lng);
        editor.putString("item_desc", vicinity);
        editor.apply();

        Toast.makeText(getApplicationContext(), getString(R.string.new_location_updated), Toast.LENGTH_SHORT).show();

        finish();
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right); //exit animation

    }


    private void insertLocationItem(String vicinity, String latitude, String longitude) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(LocationHistory.LOCATIONHISTORY_VICINITY, vicinity);
        contentValues.put(LocationHistory.LOCATIONHISTORY_LATITUDE, latitude);
        contentValues.put(LocationHistory.LOCATIONHISTORY_LONGITUDE, longitude);
        getContentResolver().insert(LocationHistory.CONTENT_URI, contentValues);
    }

    private void deleteLocationItem(long id) {
        Uri uri = Uri.parse(LocationHistory.CONTENT_URI + "/" + id);
        getContentResolver().delete(uri, null, null);
        getSupportLoaderManager().restartLoader(LOADER_ID, null, this);
    }

    private void setUpListView() {

        ListView listview = (ListView) findViewById(R.id.listView1);
        listview.addHeaderView(headerView); //must call before set adapter

        Cursor cursor = null;

        try {
            cursor = getContentResolver().query(
                    LocationHistory.CONTENT_URI,
                    null, // leaving "columns" null returns all the columns.
                    null, // cols for "where" clause
                    null, // values for "where" clause
                    null  // sort order
            );

        } catch (Exception e) {
            e.printStackTrace();
        }

        locationHistoryAdapter = new LocationHistoryAdapter(this, cursor, Const.FLAG_LOCATION_HISTORY);
        listview.setAdapter(locationHistoryAdapter);

        listview.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // minus one because header is added.
                changeToSelectedLocation(position - 1);
            }
        });

        registerForContextMenu(listview);

    }

    private void adapterToAutoTV() {
        adapter = new ArrayAdapter<>(this, R.layout.listitem_autocomplete);
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
                String description;
                String reference;

                // TODO Auto-generated method stub
                try {

                    description = myJsonArray.getJSONObject(position).getString("description");
                    reference = myJsonArray.getJSONObject(position).getString("reference");

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

        // Holding selected  values in sharedPreferences
        SharedPreferences.Editor editor = getSharedPreferences("settings", Context.MODE_PRIVATE).edit();
        editor.putBoolean("usecurrentloc_Pref", false);
        editor.putString("lat_Pref", getSelectedLatitude(pos));
        editor.putString("lng_Pref", getSelectedLongitude(pos));
        editor.putString("item_desc", getSelectedVicinity(pos));
        editor.apply();

        Toast.makeText(getApplicationContext(), getString(R.string.new_location_updated), Toast.LENGTH_SHORT).show();
        finish();
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right); //exit animation
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        return new CursorLoader(this, LocationHistory.CONTENT_URI, null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        locationHistoryAdapter.changeCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        locationHistoryAdapter.changeCursor(null);
    }

    private class GetPlaces extends AsyncTask<String, Void, ArrayList<String>> {

        @Override
        protected ArrayList<String> doInBackground(String... params) {

            ArrayList<String> predictionsArr = new ArrayList<>();

            try {
                URL googlePlaces = new URL(
                        // URLEncoder.encode(url,"UTF-8");
                        "https://maps.googleapis.com/maps/api/place/autocomplete/json?input=" + URLEncoder.encode(params[0], "UTF-8")
                                + "&types=geocode&language=en&sensor=false&key=" + Configuration.getApiKey());

                URLConnection conn = googlePlaces.openConnection();
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                String line;
                StringBuilder sb = new StringBuilder();
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

            } catch (IOException | JSONException e) {
                e.printStackTrace();

            }

            return predictionsArr;
        }

        @Override
        protected void onPostExecute(ArrayList<String> result) {
            super.onPostExecute(result);
            // update the adapter
            adapter = new ArrayAdapter<>(getBaseContext(), R.layout.listitem_autocomplete);
            adapter.setNotifyOnChange(true);
            // attach the adapter to textView
            acTextView.setAdapter(adapter);

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
        urlString.append("&sensor=false&key=").append(Configuration.getApiKey());

        StringBuilder content = new StringBuilder();
        try {
            URL url = new URL(urlString.toString());
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

    private class GetFurtherDetailsTask extends AsyncTask<String, Void, Void> {
        String jsonResult;
        String myReference;
        String vicinity;
        String latitude;
        String longitude;
        JSONObject jsonobject;

        @Override
        protected Void doInBackground(String... params) {

            myReference = params[0];
            vicinity = params[1];
            jsonResult = getUrlContents(myReference);

            try {
                jsonobject = new JSONObject(jsonResult).getJSONObject("result").getJSONObject("geometry").getJSONObject("location");

                latitude = jsonobject.getString("lat");
                longitude = jsonobject.getString("lng");

            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            saveToDbAndChangeLocation(vicinity, latitude, longitude);
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

                        // Empty db table
                        getContentResolver().delete(LocationHistory.CONTENT_URI, null, null);

                        //  Restore Default Current Location
                        SharedPreferences.Editor editor = getSharedPreferences("settings", Context.MODE_PRIVATE).edit();
                        editor.putBoolean("usecurrentloc_Pref", true);
                        editor.apply();

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
                editor.apply();
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
                boolean usingCurrentLocation = getPref.getBoolean("usecurrentloc_Pref", true); // defaults to true
                String addyDesc, latString, lngString;

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

            //  long id = info.id;
            int position = info.position - 1; // minus one because of header
            String headerTitle = getSelectedVicinity(position);
            //  Log.d(TAG, "id " + id + " header: " + headerTitle);
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

                changeToSelectedLocation(pos);

                break;

            case 1: //delete

                deleteLocationItem(info.id);

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

    private String getSelectedVicinity(int position) {
        String vicinity = "";

        Cursor cursor = getContentResolver().query(LocationHistory.CONTENT_URI, null, null, null, null);
        if (cursor != null) {
            cursor.moveToPosition(position);
            vicinity = cursor.getString(cursor.getColumnIndexOrThrow(LocationHistory.LOCATIONHISTORY_VICINITY));
            cursor.close();
        }
        return vicinity;
    }

    private String getSelectedLatitude(int position) {
        String latitude = "";
        Cursor cursor = getContentResolver().query(LocationHistory.CONTENT_URI, null, null, null, null);
        if (cursor != null) {
            cursor.moveToPosition(position);
            latitude = cursor.getString(cursor.getColumnIndexOrThrow(LocationHistory.LOCATIONHISTORY_LATITUDE));
            cursor.close();
        }
        return latitude;
    }

    private String getSelectedLongitude(int position) {
        String longitude = "";
        Cursor cursor = getContentResolver().query(LocationHistory.CONTENT_URI, null, null, null, null);
        if (cursor != null) {
            cursor.moveToPosition(position);
            longitude = cursor.getString(cursor.getColumnIndexOrThrow(LocationHistory.LOCATIONHISTORY_LONGITUDE));
            cursor.close();
        }
        return longitude;
    }
}
