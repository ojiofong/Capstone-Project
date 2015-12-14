package com.ojiofong.arounda.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ojiofong.arounda.R;
import com.ojiofong.arounda.data.PopularPlace;

import java.util.ArrayList;

public class PopularPlacesAdapter extends RecyclerView.Adapter<PopularPlacesAdapter.ViewHolder> {
    Context context;
    private ArrayList<PopularPlace> data;
    ;
    private int itemLayout;

    public PopularPlacesAdapter(Context context, int resource, ArrayList<PopularPlace> items) {
        this.data = items;
        this.itemLayout = resource;
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(itemLayout, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {

        PopularPlace currentPlace = data.get(position);

        // fill the view
        holder.iv1.setImageResource(currentPlace.getIconID());
        holder.tv1.setText(currentPlace.getPlaceName());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }



    @Override
    public int getItemCount() {
        return data.size();
    }


//    public boolean isDualPane() {
//        return context.getResources().getBoolean(R.bool.screen_land_tablet);
//    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView tv1;
        public ImageView iv1;

        public ViewHolder(View convertView) {
            super(convertView);

            tv1 = (TextView) convertView.findViewById(R.id.textView1);
            iv1 = (ImageView) convertView.findViewById(R.id.imageView1);
        }
    }
}
