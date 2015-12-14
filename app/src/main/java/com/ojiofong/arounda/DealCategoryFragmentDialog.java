package com.ojiofong.arounda;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;


import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.ojiofong.arounda.data.PopularPlace;
import com.ojiofong.arounda.ui.DealListActivity;

public class DealCategoryFragmentDialog extends DialogFragment {
	
	View rootView;
	ArrayList<PopularPlace> items;
	DealCategoryListener mCallBack;

	public DealCategoryFragmentDialog() {
		//empty constructor required for DialogFragment
	}


	@Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		super.onAttach(activity);
		try {
			mCallBack = (DealListActivity) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString() + " must implement DealCategoryListener");
		}
	}

	@Override
	public void onActivityCreated(Bundle arg0) {
		// TODO Auto-generated method stub
		super.onActivityCreated(arg0);
		getDialog().getWindow().getAttributes().windowAnimations = R.style.DialogUpDownAnimationConfirmed;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		getDialog().setTitle(getString(R.string.category));
		rootView = inflater.inflate(R.layout.changelocation, container, false);
		// Sharing xml Layout so Hide unwanted views
		((View) rootView.findViewById(R.id.toolbar)).setVisibility(View.GONE);
		((View)rootView.findViewById(R.id.autoCompleteTextView1)).setVisibility(View.GONE);
		((View)rootView.findViewById(R.id.toolbar2_changelocation)).setVisibility(View.GONE);
		((Button)rootView.findViewById(R.id.streetview_b)).setVisibility(View.GONE);

		populateList(rootView);

		return rootView;
	}

	private void populateList(View rootView) {
		// TODO Auto-generated method stub
		items = new ArrayList<PopularPlace>();
		items.add(new PopularPlace(getString(R.string.all), 0, "no_filter"));
		items.add(new PopularPlace(getString(R.string.automotive), 0, "automotive"));
		items.add(new PopularPlace(getString(R.string.beauty_and_spas), 0, "beauty-and-spas"));
		items.add(new PopularPlace(getString(R.string.food_and_drink), 0, "food-and-drink"));
		items.add(new PopularPlace(getString(R.string.health_and_fitness), 0, "health-and-fitness"));
		items.add(new PopularPlace(getString(R.string.local_services), 0, "local-services"));
		items.add(new PopularPlace(getString(R.string.shopping), 0, "shopping"));
		items.add(new PopularPlace(getString(R.string.things_to_do), 0, "things-to-do"));

		Collections.sort(items, new Comparator<PopularPlace>() {

			@Override
			public int compare(PopularPlace lhs, PopularPlace rhs) {
				// TODO Auto-generated method stub
				String first = lhs.getPlaceName();
				String second = rhs.getPlaceName();
				return first.compareTo(second);
			}
		});

		ArrayList<String> itemsHacked = new ArrayList<>();
		for (int i = 0; i < items.size(); i++) {
			itemsHacked.add(items.get(i).getPlaceName());
		}

		ListView listview = (ListView) rootView.findViewById(R.id.listView1);
		listview.setPadding(15, 5, 15, 5);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, itemsHacked);
		listview.setAdapter(adapter);
		listview.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				// TODO Auto-generated method stub
				mCallBack.onCategoryListClicked(items.get(position).getPlaceID(), items.get(position).getPlaceName());
				getDialog().cancel();

			}
		});

	}

	public interface DealCategoryListener {
		public void onCategoryListClicked(String categoryID, String categoryName);
	}

}
