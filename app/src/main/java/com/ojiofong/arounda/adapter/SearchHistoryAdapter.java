package com.ojiofong.arounda.adapter;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.ojiofong.arounda.R;
import com.ojiofong.arounda.db.SearchHistory;

/**
 * Created by ojiofong on 12/17/15.
 * .
 */
public class SearchHistoryAdapter extends CursorAdapter {

    Context context;

    public SearchHistoryAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
        this.context = context;
    }


    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.listitem_changelocation, parent, false);
        view.setTag(new ViewHolder(view));
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder holder = (ViewHolder) view.getTag();

        String name = cursor.getString(cursor.getColumnIndexOrThrow(SearchHistory.COLUMN_NAME));

        holder.displayText.setText(name);
    }

    public static class ViewHolder {
        public TextView displayText;

        public ViewHolder(View view) {
            displayText = (TextView) view.findViewById(R.id.changeLocation_tv);
        }
    }
}
