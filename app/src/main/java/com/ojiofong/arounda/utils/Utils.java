package com.ojiofong.arounda.utils;

import android.content.Context;
import android.net.ConnectivityManager;

/**
 * Created by oofong25 on 12/12/15.
 * .
 */
public class Utils {

    public static boolean isNetworkConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return (cm.getActiveNetworkInfo() != null) && cm.getActiveNetworkInfo().isConnectedOrConnecting();
    }


}
