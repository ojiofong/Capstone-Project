package com.ojiofong.arounda.adapter;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.ojiofong.arounda.R;
import com.ojiofong.arounda.R.id;
import com.ojiofong.arounda.R.layout;


public class TheAdapter extends ArrayAdapter<HashMap<String, String>> {
	
	LayoutInflater inflater;
	ArrayList<HashMap<String, String>> arraylist;

	public TheAdapter(Context context, ArrayList<HashMap<String, String>> arraylist) {
		super(context, layout.listitem_changelocation, arraylist);
		// TODO Auto-generated constructor stub
		this.inflater = LayoutInflater.from(context);
		this.arraylist = arraylist;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;

		if (convertView == null) {

			convertView = inflater.inflate(layout.listitem_changelocation, parent, false);

			holder = new ViewHolder();
			holder.tv1 = (TextView) convertView.findViewById(id.changeLocation_tv);

			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		
		HashMap<String, String> currentMap = new HashMap<String, String>();
		currentMap = arraylist.get(position);
		

		holder.tv1.setText(currentMap.get("description"));
		//holder.tv2.setText(currentBike.getComment());

		return convertView;
	}
	
	private class ViewHolder {
		public TextView tv1;

	}

}


