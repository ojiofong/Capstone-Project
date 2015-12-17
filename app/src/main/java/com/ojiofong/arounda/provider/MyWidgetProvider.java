package com.ojiofong.arounda.provider;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.widget.RemoteViews;

import com.ojiofong.arounda.PlaceHelper;
import com.ojiofong.arounda.R;
import com.ojiofong.arounda.data.Place;
import com.ojiofong.arounda.ui.MainActivity;
import com.ojiofong.arounda.utils.Configuration;
import com.ojiofong.arounda.utils.GPSUtil;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by ojiofong on 12/17/15.
 * .
 */
public class MyWidgetProvider extends AppWidgetProvider {

    private static final String TAG = MyWidgetProvider.class.getSimpleName();

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);

        //  final int N = appWidgetIds.length;
        for (int appWidgetId : appWidgetIds) {
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
            //updateWigetView(context, views);
            getTopRestaurantsTask(context, appWidgetManager, appWidgetId, views);

            Intent clickItent = new Intent(context, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, clickItent, 0);
            views.setOnClickPendingIntent(R.id.widget_main, pendingIntent);

            appWidgetManager.updateAppWidget(appWidgetId, views);

        }

    }


    private void updateWigetView(Context context, AppWidgetManager appWidgetManager, int appWidgetId, RemoteViews views, ArrayList<Place> places) {

        // Randomly select a restaurant
        int random = new Random().nextInt(places.size() - 1);
        String name = places.get(random).getName();
        String address = places.get(random).getAddress();
        String title = context.getString(R.string.restaurant_nearby);

        views.setTextViewText(R.id.title_widget, title);
        views.setTextViewText(R.id.name, name != null ? name : "");
        views.setTextViewText(R.id.address, address != null ? address : "");

        appWidgetManager.updateAppWidget(appWidgetId, views);

    }

    private void getTopRestaurantsTask(final Context context, final AppWidgetManager appWidgetManager, final int appWidgetId, final RemoteViews views) {
        new AsyncTask<Void, Void, ArrayList<Place>>() {

            @Override
            protected ArrayList<Place> doInBackground(Void... params) {

                PlaceHelper placeHelper = new PlaceHelper(Configuration.getApiKey());
                GPSUtil gpsUtil = new GPSUtil(context);

                return placeHelper.findPlaces(gpsUtil.getLastKnownLatitude(),
                        gpsUtil.getLastKnownLongitude(), "restaurant", 50.0, false, null, null, context);
            }

            @Override
            protected void onPostExecute(ArrayList<Place> places) {
                super.onPostExecute(places);
                updateWigetView(context, appWidgetManager, appWidgetId, views, places);
            }
        }.execute();
    }

//    private void update_scores(Context context) {
//        Intent service_start = new Intent(context, myFetchService.class);
//        context.startService(service_start);
//    }
}