package com.ojiofong.arounda.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by ojiofong on 12/17/15.
 * .
 */
public class MyDbHelper extends SQLiteOpenHelper {
    private static final String TAG = MyDbHelper.class.getSimpleName();
    private static MyDbHelper mDBHelper;

    private static final String SQL_CREATE_SEARCH_HISTORY_TABLE = "CREATE TABLE IF NOT EXISTS "
            + SearchHistory.TABLE_NAME + " ( "
            + SearchHistory._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + SearchHistory.COLUMN_ID + " TEXT, "
            + SearchHistory.COLUMN_NAME + " TEXT, "
            + SearchHistory.COLUMN_TIMESTAMP + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL "
            + " );";

    private static final String SQL_CREATE_LOCATION_HISTORY_TABLE = "CREATE TABLE IF NOT EXISTS "
            + LocationHistory.TABLE_NAME + " ( "
            + LocationHistory._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + LocationHistory.LOCATIONHISTORY_ID + " TEXT, "
            + LocationHistory.LOCATIONHISTORY_VICINITY + " TEXT, "
            + LocationHistory.LOCATIONHISTORY_LATITUDE + " TEXT, "
            + LocationHistory.LOCATIONHISTORY_LONGITUDE + " INTEGER, "
            + LocationHistory.LOCATIONHISTORY_TIMESTAMP + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL "
            + " );";


    public MyDbHelper(Context context, String dbName, int dbVersion) {
        super(context, dbName, null, dbVersion);
        mDBHelper = this;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "onCreate");
        db.execSQL(SQL_CREATE_SEARCH_HISTORY_TABLE);
        db.execSQL(SQL_CREATE_LOCATION_HISTORY_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    	Log.d(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion);
        switch(oldVersion) {
            default:
                db.execSQL("DROP TABLE IF EXISTS " + SearchHistory.TABLE_NAME);
                db.execSQL("DROP TABLE IF EXISTS " + LocationHistory.TABLE_NAME);
                onCreate(db);
                break;
        }
    }

    public static SQLiteDatabase getReadableDB(){
        return mDBHelper.getReadableDatabase();
    }

    public static SQLiteDatabase getWritableDB(){
        return mDBHelper.getWritableDatabase();
    }

}
