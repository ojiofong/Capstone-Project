package com.ojiofong.arounda.db;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by ojiofong on 12/17/15.
 * .
 */
public class LocationHistory {

    public static final String TABLE_NAME = "location_history_table";
    public static final Uri CONTENT_URI = Uri.parse(MyContentProvider.CONTENT_URI_BASE + "/" + TABLE_NAME);

    public static final String _ID = BaseColumns._ID;
    public static final String LOCATIONHISTORY_ID = "location_history_ID";
    public static final String LOCATIONHISTORY_VICINITY = "location_history_vicinity";
    public static final String LOCATIONHISTORY_LATITUDE = "location_history_latitude";
    public static final String LOCATIONHISTORY_LONGITUDE = "location_history_longitude";
    public static final String LOCATIONHISTORY_TIMESTAMP = "location_history_time_stamp";
    public static final String DEFAULT_ORDER = LOCATIONHISTORY_TIMESTAMP + " DESC";
    public static final String ATOZ_ORDER = LOCATIONHISTORY_VICINITY + " COLLATE NOCASE";
}
