package com.ojiofong.arounda.ui;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.ojiofong.arounda.R;
import com.ojiofong.arounda.utils.AppManager;

@SuppressLint("SetJavaScriptEnabled")
public class WebViewActivity extends AppCompatActivity {

	WebView webview;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		//webview = new WebView(this);
		setContentView(R.layout.webview);
		new AppManager(this).setStatusBarColorForKitKat(getResources().getColor(R.color.colorPrimaryDark));
		initToolBar();
		
		webview = (WebView)findViewById(R.id.webview);
		
		final String website = getIntent().getStringExtra("url");

		webview.getSettings().setJavaScriptEnabled(true);

		//final Activity activity = this;

		final ProgressBar progressBar = (ProgressBar)findViewById(R.id.ProgressBar);

		webview.setWebChromeClient(new WebChromeClient() {
			public void onProgressChanged(WebView view, int progress) {
				//progressDialog.show();
				progressBar.setVisibility(View.VISIBLE);
				progressBar.setProgress(0);
				//activity.setProgress(progress * 1000);
				progressBar.incrementProgressBy(progress);

				if (progress == 100 && progressBar.isShown())
					progressBar.setVisibility(View.GONE);
			}

		});

		webview.setWebViewClient(new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				return false;
			}
		});

		webview.loadUrl(website);

	}
	
	private void initToolBar() {
		// initialize ToolBar		
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setTitle(getIntent().getStringExtra("actionBarTitle"));
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
