package com.example.SerraDomotica.widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.SharedPreferences;
import android.widget.RemoteViews;

import com.example.SerraDomotica.BaseActivity;
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


    public static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        SharedPreferences prefs = context.getSharedPreferences(GreenhouseWidgetConfigActivity.PREFS_NAME, Context.MODE_PRIVATE);
        String greenhouseId = prefs.getString(GreenhouseWidgetConfigActivity.PREF_PREFIX_KEY + appWidgetId, null);
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_greenhouse_layout);

        if (greenhouseId != null) {
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
            views.setTextViewText(R.id.widgetHumidity, "");
            views.setTextViewText(R.id.widgetLuminosity, "");
            views.setTextViewText(R.id.widgetSoilHumidity, "");
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
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
