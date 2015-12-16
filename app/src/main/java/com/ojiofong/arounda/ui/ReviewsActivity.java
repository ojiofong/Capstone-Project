package com.ojiofong.arounda.ui;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.ojiofong.arounda.R;
import com.ojiofong.arounda.ViewHolder;
import com.ojiofong.arounda.data.Review;
import com.ojiofong.arounda.utils.AppManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ReviewsActivity extends AppCompatActivity {

	List<Review> myReviews = new ArrayList<Review>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_singlenext);
		new AppManager(this).setStatusBarColorForKitKat(getResources().getColor(R.color.colorPrimaryDark));

		initToolBar();
		populateList();
		populateListView();

	}

	private void initToolBar() {
		// initialize ToolBar		
		Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);	

		if (getSupportActionBar()!=null){
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);

			//set Title and subTitle with values from intent
			getSupportActionBar().setTitle(getIntent().getStringExtra("placeName"));
			getSupportActionBar().setSubtitle(getIntent().getStringExtra("placeAddress"));

		}
		
		
	}

	@SuppressWarnings("unchecked")
	private void populateList() {

		// get arrayList via intent from parent activity
		myReviews = (ArrayList<Review>) getIntent().getSerializableExtra("arrayListFromIntent");

		/*
		 * myReviews.add(new Review("Dog", "This is a domestic animal")); myReviews.add(new Review("Lion", "This is a wild animal"));
		 * myReviews.add(new Review("Honey Badger", "This is a wild animal")); myReviews.add(new Review("Apple", "This is a fruit"));
		 */

	}

	@SuppressLint("InflateParams")
	private void populateListView() {
		
		Typeface tf = Typeface.createFromAsset(getAssets(), "fonts/roboto_regular.ttf");
		
		View view = getLayoutInflater().inflate(R.layout.listitem_textview, null);
		TextView tv = (TextView) view.findViewById(R.id.tvalone);
		tv.setText(getString(R.string.reviews).toUpperCase(Locale.getDefault()));
		tv.setPadding(0, 5, 0, 5);
		tv.setTextColor(Color.BLACK);
		tv.setTypeface(tf);
		tv.setTextSize(16f);
		
		//tv.setTypeface(null, Typeface.BOLD);
		tv.setBackgroundColor(getResources().getColor(R.color.blue_singleborder_a));
		tv.setGravity(Gravity.CENTER);

		ArrayAdapter<Review> adapter = new MyOwnAdapter();
		ListView listview = (ListView) findViewById(R.id.listViewSingleNext);
		listview.addHeaderView(view);
		listview.setAdapter(adapter);

	}

	public class MyOwnAdapter extends ArrayAdapter<Review> {

		public MyOwnAdapter() {
			super(ReviewsActivity.this, R.layout.single2row, myReviews);
			// TODO Auto-generated constructor stub
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;

			if (convertView == null) {

				convertView = getLayoutInflater().inflate(R.layout.single2row, parent, false);

				holder = new ViewHolder();
				holder.tv1 = (TextView) convertView.findViewById(R.id.textView1_Single2Row);
				holder.tv2 = (TextView) convertView.findViewById(R.id.textView2_Single2Row);				
				holder.ratingbar = (RatingBar) convertView.findViewById(R.id.ratingbar_Single2Row);
				
				Typeface tf = Typeface.createFromAsset(getAssets(), "fonts/roboto_regular.ttf");
				holder.tv1.setTypeface(tf);
				holder.tv2.setTypeface(tf);	

				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			Review currentReview = myReviews.get(position);

			holder.tv1.setText(currentReview.getAuthor());
			holder.tv2.setText(currentReview.getComment());
			holder.ratingbar.setRating(Float.parseFloat(currentReview.getRating()));

			return convertView;
		}

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

}
