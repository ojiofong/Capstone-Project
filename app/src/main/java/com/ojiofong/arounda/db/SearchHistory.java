package com.ojiofong.arounda.db;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by ojiofong on 12/17/15.
 * .
 */
public class SearchHistory {

    public static final String TABLE_NAME = "search_history_table";
    public static final Uri CONTENT_URI = Uri.parse(MyContentProvider.CONTENT_URI_BASE + "/" + TABLE_NAME);

    public static final String _ID = BaseColumns._ID;
    public static final String COLUMN_ID = "search_history_id";
    public static final String COLUMN_NAME = "search_history_name";
    public static final String COLUMN_TIMESTAMP = "search_history_time_stamp";
    public static final String DEFAULT_ORDER = COLUMN_TIMESTAMP + " DESC";
    public static final String ATOZ_ORDER = COLUMN_NAME + " COLLATE NOCASE";

}
