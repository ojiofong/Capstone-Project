package com.ojiofong.arounda.ui;

import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.ojiofong.arounda.R;

public class SettingsActivity extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_container);
		initToolBar();
		initFragment();

	}

	private void initFragment() {
		// Display the fragment as the main content.
		getFragmentManager().beginTransaction().replace(R.id.container, new QuickPrefsFragment()).commit();

	}

	private void initToolBar() {
		// initialize ToolBar		
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		if (getSupportActionBar()!=null){
			getSupportActionBar().setTitle(getString(R.string.app_name));
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		}

	}

	public boolean onPreferenceClick(Preference preference) {

		if (preference.getKey().matches("open_source")) {

			Intent i = new Intent(getApplicationContext(), WebViewActivity.class);
			String url = "http://sovancegroup.com/android/arounda/opensource/";
			i.putExtra("url", url);
			i.putExtra("actionBarTitle", "Open Source Attribution");
			startActivity(i);
			overridePendingTransition(R.anim.right_slide_in, R.anim.left_slide_out); // for starting activity only

		}

		return false;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {

		case android.R.id.home:
			finish();
			break;
		}

		return super.onOptionsItemSelected(item);
	}

	public class QuickPrefsFragment extends PreferenceFragment {

		public QuickPrefsFragment(){
		}

		@Override
		public void onCreate(Bundle savedInstanceState) {
			// TODO Auto-generated method stub
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.preferences);
		}

		@Override
		public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
			// TODO Auto-generated method stub
			onPreferenceClick(preference);
			return super.onPreferenceTreeClick(preferenceScreen, preference);
		}

	}
}
