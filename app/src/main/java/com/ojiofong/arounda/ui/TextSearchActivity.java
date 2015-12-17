package com.ojiofong.arounda.ui;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
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
import android.widget.ListView;
import android.widget.Toast;

import com.ojiofong.arounda.R;
import com.ojiofong.arounda.adapter.SearchHistoryAdapter;
import com.ojiofong.arounda.db.LocationHistory;
import com.ojiofong.arounda.db.SearchHistory;

import java.util.Locale;

public class TextSearchActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    ListView listview;
    SearchHistoryAdapter searchAdapter;
    private static final int LOADER_ID = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.changelocation);
        initialize();
        setUpListView();
        getSupportLoaderManager().initLoader(LOADER_ID, null, this);

    }

    @Override
    protected void onResume() {
        super.onResume();
        getSupportLoaderManager().restartLoader(LOADER_ID, null, this);
    }

    private void initialize() {
        // initialize ToolBar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setElevation((float) 5);
        }

        // Sharing xml Layout so Hide unwanted view for this activity
        (findViewById(R.id.autoCompleteTextView1)).setVisibility(View.GONE);
        (findViewById(R.id.toolbar2_changelocation)).setVisibility(View.GONE);
        (findViewById(R.id.streetview_b)).setVisibility(View.GONE);

    }

    private void setUpListView() {

        listview = (ListView) findViewById(R.id.listView1);

        Cursor cursor = null;

        try {
            // leaving "columns" null returns all the columns.
            cursor = getContentResolver().query(
                    SearchHistory.CONTENT_URI, null, null, null, null);

        } catch (Exception e) {
            e.printStackTrace();
        }

        searchAdapter = new SearchHistoryAdapter(this, cursor, 0);
        listview.setAdapter(searchAdapter);

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // TODO Auto-generated method stub
                String s = getSelectedName(position);
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

    private void insertKeywordToDb(String searchInput) {
        // Called on SearchQuery submitted
        ContentValues contentValues = new ContentValues();
        contentValues.put(SearchHistory.COLUMN_NAME, searchInput);
        getContentResolver().insert(SearchHistory.CONTENT_URI, contentValues);

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
                if (getSupportActionBar() != null)
                    getSupportActionBar().setTitle(""); //cleaner exit

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
                insertKeywordToDb(input);
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

                        // Empty db table
                        getContentResolver().delete(SearchHistory.CONTENT_URI, null, null);

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
            String headerTitle = getSelectedName(info.position);
            menu.setHeaderTitle(headerTitle);
            String[] MENU_ITEMS = {getString(R.string.action_search), getString(R.string.delete), getString(R.string.cancel)};
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

                String s = getSelectedName(info.position);
                // Perform search
                Intent i = new Intent(getApplicationContext(), PlaceListActivity.class);
                i.putExtra("searchQuery", s);
                startActivity(i);
                overridePendingTransition(R.anim.right_slide_in, R.anim.left_slide_out); // for starting activity only

                break;

            case 1: //delete item

                deleteSearchItem(info.id);

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

    private void deleteSearchItem(long id) {
        Uri uri = Uri.parse(SearchHistory.CONTENT_URI + "/" + id);
        getContentResolver().delete(uri, null, null);
        getSupportLoaderManager().restartLoader(LOADER_ID, null, this);
    }

    private String getSelectedName(int position) {
        String name = "";

        Cursor cursor = getContentResolver().query(SearchHistory.CONTENT_URI, null, null, null, null);
        if (cursor != null) {
            cursor.moveToPosition(position);
            name = cursor.getString(cursor.getColumnIndexOrThrow(SearchHistory.COLUMN_NAME));
            cursor.close();
        }
        return name;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        return new CursorLoader(this, SearchHistory.CONTENT_URI, null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        searchAdapter.changeCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        searchAdapter.changeCursor(null);
    }
}
