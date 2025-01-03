package com.example.SerraDomotica.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.RemoteViews;

import com.example.SerraDomotica.BaseActivity;
import com.example.SerraDomotica.LoginActivity;
import com.example.SerraDomotica.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;

public class GreenhouseWidgetProvider extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    private static boolean isNetworkAvailable2(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        SharedPreferences prefs = context.getSharedPreferences(GreenhouseWidgetConfigActivity.PREFS_NAME, Context.MODE_PRIVATE);
        String greenhouseId = prefs.getString(GreenhouseWidgetConfigActivity.PREF_PREFIX_KEY + appWidgetId, null);
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_greenhouse_layout);

        if (greenhouseId != null) {
            if (!isNetworkAvailable2(context)) {
                views.setTextViewText(R.id.widgetTemperature, "No Internet available");

                views.setTextViewText(R.id.widgetHumidity, "No Internet available");
                views.setTextViewText(R.id.widgetLuminosity, "No Internet available");
                views.setTextViewText(R.id.widgetSoilHumidity, "No Internet available");
                appWidgetManager.updateAppWidget(appWidgetId, views);
                return;
            }
            FirebaseDatabase.getInstance().getReference("devices").child(greenhouseId).child("history").limitToLast(1).get()
                    .addOnSuccessListener(snapshot -> {
                        if (snapshot.exists()) {
                            for (DataSnapshot dateSnapshot : snapshot.getChildren()) {
                                Float airTemperature = dateSnapshot.child("air_temperature").getValue(Float.class);
                                Integer airHumidity = dateSnapshot.child("air_humidity").getValue(Integer.class);
                                Integer luminosity = dateSnapshot.child("luminosity").getValue(Integer.class);
                                Integer soilHumidity = dateSnapshot.child("soil_humidity").getValue(Integer.class);

                                if (airTemperature != null) {
                                    views.setTextViewText(R.id.widgetTemperature, airTemperature + "°C");
                                }
                                if (airHumidity != null) {
                                    views.setTextViewText(R.id.widgetHumidity, airHumidity + "%");
                                }
                                if (luminosity != null) {
                                    views.setTextViewText(R.id.widgetLuminosity, luminosity + "%");
                                }
                                if (soilHumidity != null) {
                                    views.setTextViewText(R.id.widgetSoilHumidity, soilHumidity + "%");
                                }

                                appWidgetManager.updateAppWidget(appWidgetId, views);
                            }
                        } else {
                            views.setTextViewText(R.id.widgetTemperature, "No Data");
                            appWidgetManager.updateAppWidget(appWidgetId, views);
                        }
                    });

            BaseActivity.getGreenhouseName(greenhouseId, greenhouseName -> {
                views.setTextViewText(R.id.widgetGreenhouseName, greenhouseName);
                appWidgetManager.updateAppWidget(appWidgetId, views);
            });
        } else {
            views.setTextViewText(R.id.widgetTemperature, "Not Configured");
            views.setTextViewText(R.id.widgetHumidity, "Not Configured");
            views.setTextViewText(R.id.widgetLuminosity, "Not Configured");
            views.setTextViewText(R.id.widgetSoilHumidity, "Not Configured");
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }

        /* Set up the refresh button click handler
        Intent intent = new Intent(context, GreenhouseWidgetProvider.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, new int[]{appWidgetId});
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, appWidgetId, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        views.setOnClickPendingIntent(R.id.buttonRefresh, pendingIntent);
        appWidgetManager.updateAppWidget(appWidgetId, views);*/

        Intent appIntent = new Intent(context, LoginActivity.class);
        PendingIntent appPendingIntent = PendingIntent.getActivity(context, 0, appIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        views.setOnClickPendingIntent(R.id.widgetGreenhouseLayout, appPendingIntent);

        appWidgetManager.updateAppWidget(appWidgetId, views);
    }


    @Override
    public void onEnabled(Context context) {
        // Chiamato quando il primo widget viene aggiunto
    }

    @Override
    public void onDisabled(Context context) {
        // Chiamato quando l'ultimo widget viene rimosso
    }
}
