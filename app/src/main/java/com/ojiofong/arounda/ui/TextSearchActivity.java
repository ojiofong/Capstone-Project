package com.ojiofong.arounda.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.SearchView.OnQueryTextListener;
import android.support.v7.widget.Toolbar;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.ContextThemeWrapper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.ojiofong.arounda.R;
import com.ojiofong.arounda.Wrapper;

import java.util.ArrayList;
import java.util.Locale;

public class TextSearchActivity extends AppCompatActivity {

	ArrayAdapter<String> adapter;
	ArrayList<String> myList = new ArrayList<String>();
	ListView listview;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub 
		super.onCreate(savedInstanceState);
		setContentView(R.layout.changelocation);
		initialize();
		restoreSearchHistory();
		populateListView();

	}

	private void initialize() {
		// initialize ToolBar		
		Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);	

		if (getSupportActionBar()!=null){
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
			getSupportActionBar().setElevation((float)5);
		}


		// Sharing xml Layout so Hide unwanted view for this activity
		((View)findViewById(R.id.autoCompleteTextView1)).setVisibility(View.GONE);
		((View)findViewById(R.id.toolbar2_changelocation)).setVisibility(View.GONE);
		((Button)findViewById(R.id.streetview_b)).setVisibility(View.GONE);

		listview = (ListView) findViewById(R.id.listView1);
	}

	private void populateListView() {
		// initialize ArrayAdapter

		// R.layout.listitem_autocomplete is a TextView only xml and can replace 
		// android simpleListitem
		adapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.listitem_textview, myList);
		adapter.notifyDataSetChanged();

		listview.setAdapter(adapter);

		listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				// TODO Auto-generated method stub
				String s = myList.get(position);

				// Perform search
				Intent i = new Intent(getApplicationContext(), PlaceListActivity.class);
				i.putExtra("searchQuery", s);
				startActivity(i);
				overridePendingTransition(R.anim.right_slide_in, R.anim.left_slide_out); // for starting activity only

			}
		});

		//Register listView for contextMenu
		registerForContextMenu(listview);

	}

	private void updateSearchHistory(String input) {
		// Called on SearchQuery submitted

		// Add search to List History and save ArrayList to Wrapper Class
		myList.add(input);
		adapter.notifyDataSetChanged();

		// save myList to Wrapper class, GSON, and SharedPrefs
		Wrapper wrapper = new Wrapper();
		wrapper.setSimpleList(myList);

		Gson gson = new Gson();
		String str = gson.toJson(wrapper);

		// save to SharedPrefs
		SharedPreferences.Editor editor = getSharedPreferences("settings", Context.MODE_PRIVATE).edit();
		editor.putString("gsonSearchHistory", str);
		editor.apply();

	}

	private void restoreSearchHistory() {
		// Called in OnCreateBundle to restore History if needed
		// get sharedPref first
		SharedPreferences pref = getSharedPreferences("settings", Context.MODE_PRIVATE);
		String str = pref.getString("gsonSearchHistory", null);

		if (str != null) {

			Gson gson = new Gson();
			Wrapper wrapper = gson.fromJson(str, Wrapper.class);
			ArrayList<String> retrievedList = wrapper.getSimpleList();
			myList = retrievedList;

		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		getMenuInflater().inflate(R.menu.main, menu);

		// Hiding unwanted menuItems
		MenuItem settingsItem = menu.findItem(R.id.action_settings);
		settingsItem.setVisible(false);
		MenuItem changeLocItem = menu.findItem(R.id.action_changeLocation);
		changeLocItem.setVisible(false);
		MenuItem searchNullItem = menu.findItem(R.id.action_search_null);
		searchNullItem.setVisible(false);

		// ensure SearchView is V7 Widget to avoid headaches
		MenuItem searchItem = menu.findItem(R.id.action_search);
		MenuItemCompat.expandActionView(searchItem);
		MenuItemCompat.setOnActionExpandListener(searchItem, new MenuItemCompat.OnActionExpandListener() {

			@Override
			public boolean onMenuItemActionExpand(MenuItem arg0) {
				// TODO Auto-generated method stub
				return true;
			}

			@Override
			public boolean onMenuItemActionCollapse(MenuItem arg0) {
				// finish activity and exit with animation
				getSupportActionBar().setTitle(null); //cleaner exit
				finish();
				overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
				return true;
			}
		});

		SearchView searchView = (SearchView) searchItem.getActionView();
		searchView.setOnQueryTextListener(new OnQueryTextListener() {

			@Override
			public boolean onQueryTextSubmit(String s) {
				// TODO Auto-generated method stub
				String input = s.toLowerCase(Locale.getDefault()).trim();

				// Perform search
				Intent i = new Intent(getApplicationContext(), PlaceListActivity.class);
				i.putExtra("searchQuery", input);
				startActivity(i);
				overridePendingTransition(R.anim.right_slide_in, R.anim.left_slide_out); // for starting activity only

				// update after starting activity for efficiency if possible
				updateSearchHistory(input);
				return true;
			}

			@Override
			public boolean onQueryTextChange(String arg0) {
				// TODO Auto-generated method stub
				return true;
			}
		});

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {

		case android.R.id.home:
			finish();
			overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right); // exit animation
			break;

		case R.id.action_DeleteHistory:
			
			Context mContext = new ContextThemeWrapper(this, R.style.AppTheme);

			new AlertDialog.Builder(mContext).setTitle(getString(R.string.delete_history)).setMessage(getString(R.string.delete_history_message))
					.setCancelable(true).setNegativeButton(getString(R.string.cancel), null).setPositiveButton(getString(R.string.delete), new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							// clear list adapter
							adapter.clear();
							adapter.notifyDataSetChanged();

							// update sharedPrefs to default null
							SharedPreferences.Editor editor = getSharedPreferences("settings", Context.MODE_PRIVATE).edit();
							editor.putString("gsonSearchHistory", null);
							editor.apply();

							Toast.makeText(getApplicationContext(), getString(R.string.history_deleted), Toast.LENGTH_SHORT).show();

							// Finish Activity
							finish();
							overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right); // exit
							// animation

						}
					}).show();

			break;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		if (v.getId() == R.id.listView1 && listview.getCount() != 0) {
			AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
			String headerTitle = this.adapter.getItem(info.position);
			menu.setHeaderTitle(headerTitle);
			String[] MENU_ITEMS = { getString(R.string.action_search), getString(R.string.delete), getString(R.string.cancel) };
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
		case 0: //search

			String s = myList.get(info.position);
			// Perform search
			Intent i = new Intent(getApplicationContext(), PlaceListActivity.class);
			i.putExtra("searchQuery", s);
			startActivity(i);
			overridePendingTransition(R.anim.right_slide_in, R.anim.left_slide_out); // for starting activity only

			break;

		case 1: //delete

			myList.remove(info.position);
			//adapter.remove(adapter.getItem(info.position));
			adapter.notifyDataSetChanged();

			//update myList
			// save myList to Wrapper class, GSON, and SharedPrefs
			Wrapper wrapper = new Wrapper();
			wrapper.setSimpleList(myList);

			Gson gson = new Gson();
			String str = gson.toJson(wrapper);

			// save to SharedPrefs
			SharedPreferences.Editor editor = getSharedPreferences("settings", Context.MODE_PRIVATE).edit();
			editor.putString("gsonSearchHistory", str);
			editor.apply();

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
		finish();
		overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right); // exit animation
	}

}
