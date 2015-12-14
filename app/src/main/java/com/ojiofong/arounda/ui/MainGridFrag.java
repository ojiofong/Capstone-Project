package com.ojiofong.arounda.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.GridView;

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
public class MainGridFrag extends Fragment {

    View rootView;

    public MainGridFrag(){}

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_grid_main, container, false);
        //populateGridView(((MainActivity)getActivity()).myPopularPlaces);
        setUpRecyclerView(((MainActivity) getActivity()).myPopularPlaces);


        return rootView;
    }

    private void populateGridView(final ArrayList<PopularPlace> myPopularPlaces) {

        CarAdapter adapter = new CarAdapter(getActivity(), R.layout.single_item_grid, myPopularPlaces);

        GridView gridView = (GridView) rootView.findViewById(R.id.gridView1);
        gridView.setAdapter(adapter);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View viewClicked, int position, long id) {

                ((MainActivity) getActivity()).goToNextActivity(position);

            }

        });
    }

    private void setUpRecyclerView(final ArrayList<PopularPlace> myPopularPlaces) {
        final RecyclerView mRecyclerView;
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true);
        final GridLayoutManager mLayoutManager = new GridLayoutManager(getActivity(), 3); // will auto-fit so 3 is a placeholder
        mRecyclerView.setLayoutManager(mLayoutManager);

        PopularPlacesAdapter adapter = new PopularPlacesAdapter(getActivity(), R.layout.single_item_grid, myPopularPlaces);
        mRecyclerView.setAdapter(adapter);
        mRecyclerView.addOnItemTouchListener(new RecyclerItemClickListener(getActivity(), new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                ((MainActivity) getActivity()).goToNextActivity(position);
            }
        }));

        // Auto-fit gridLayoutManger
        // source - http://stackoverflow.com/a/27000759/3144836
        mRecyclerView.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        mRecyclerView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        int viewWidth = mRecyclerView.getMeasuredWidth();
                        float cardViewWidth = getActivity().getResources().getDimension(R.dimen.grid_width);
                        int newSpanCount = (int) Math.floor(viewWidth / cardViewWidth);
                        mLayoutManager.setSpanCount(newSpanCount);
                        mLayoutManager.requestLayout();
                    }
                });

    }
}
