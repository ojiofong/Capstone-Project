package com.ojiofong.arounda.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.ojiofong.arounda.R;
import com.ojiofong.arounda.adapter.CarAdapter;
import com.ojiofong.arounda.adapter.PopularPlacesAdapter;
import com.ojiofong.arounda.data.PopularPlace;
import com.ojiofong.arounda.data.RecyclerItemClickListener;

import java.util.ArrayList;

/**
 * Created by oofong25 on 12/11/15.
 * .
 */
public class MainListFrag extends Fragment {

    View rootView;

    public MainListFrag() {
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_list_main, container, false);
        // populateListView(((MainActivity) getActivity()).myPopularPlaces);
        setUpRecyclerView(((MainActivity) getActivity()).myPopularPlaces);

        return rootView;
    }

    private void populateListView(final ArrayList<PopularPlace> myPopularPlaces) {

        CarAdapter adapter = new CarAdapter(getActivity(), R.layout.single_item_view, myPopularPlaces);

        ListView list = (ListView) rootView.findViewById(R.id.listView1);
        list.setAdapter(adapter);

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View viewClicked, int position, long id) {

                ((MainActivity) getActivity()).goToNextActivity(position);

            }

        });
    }

    private void setUpRecyclerView(final ArrayList<PopularPlace> myPopularPlaces) {
        RecyclerView mRecyclerView;
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);

        PopularPlacesAdapter adapter = new PopularPlacesAdapter(getActivity(), R.layout.single_item_view, myPopularPlaces);
        mRecyclerView.setAdapter(adapter);
        mRecyclerView.addOnItemTouchListener(new RecyclerItemClickListener(getActivity(), new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                ((MainActivity) getActivity()).goToNextActivity(position);
            }
        }));

    }

}
