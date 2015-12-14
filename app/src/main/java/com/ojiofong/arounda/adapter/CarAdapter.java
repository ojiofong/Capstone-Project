package com.ojiofong.arounda.adapter;

import java.util.ArrayList;
import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ojiofong.arounda.data.PopularPlace;
import com.ojiofong.arounda.R;
import com.ojiofong.arounda.ViewHolder;

public class CarAdapter extends ArrayAdapter<PopularPlace> {
	ArrayList<PopularPlace> myPopularPlaces;
	LayoutInflater inflater;
	int resource;
	Context context;
	
	//constructor
	public CarAdapter(Context context, int resource, ArrayList<PopularPlace> myPopularPlaces) {
		
		super(context, resource, myPopularPlaces);
		this.myPopularPlaces = myPopularPlaces;
		this.inflater = LayoutInflater.from(context);
		this.resource = resource;
		this.context = context;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		ViewHolder holder;

		if (convertView == null) {
			convertView = inflater.inflate(resource, parent, false);

			holder = new ViewHolder();
			holder.tv1 = (TextView) convertView.findViewById(R.id.textView1);
			holder.iv1 = (ImageView) convertView.findViewById(R.id.imageView1);
			
			//Doing it here so it is recycled
			Typeface tf = Typeface.createFromAsset(context.getAssets(), "fonts/roboto_regular.ttf");
			holder.tv1.setTypeface(tf);
			
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		// find the car to work with
		PopularPlace currentPlace = myPopularPlaces.get(position);

		// fill the view
		holder.iv1.setImageResource(currentPlace.getIconID());
		holder.tv1.setText(currentPlace.getPlaceName());
		
		

		return convertView;
	}

}
