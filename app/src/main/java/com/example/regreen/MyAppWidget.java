package com.example.regreen;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.RemoteViews;

import com.example.regreen.myapplication.EditProfile;
import com.example.regreen.myapplication.Login;
import com.example.regreen.myapplication.RecivingPoint;
import com.example.regreen.myapplication.User.UserBooking;
import com.example.regreen.myapplication.WasteIdentification;


public class MyAppWidget extends AppWidgetProvider {

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.my_app_widget);

            // Tạo PendingIntent cho các button và gán vào RemoteViews
            views.setOnClickPendingIntent(R.id.openApp, createPendingIntent(context, Login.class, "com.example.regreen.openApp"));
            views.setOnClickPendingIntent(R.id.btnBooking1, createPendingIntent(context, UserBooking.class, "com.example.regreen.btnBooking1"));
            views.setOnClickPendingIntent(R.id.btnScan1, createPendingIntent(context, WasteIdentification.class, "com.example.regreen.btnScan1"));
            views.setOnClickPendingIntent(R.id.btnMap1, createPendingIntent(context, RecivingPoint.class, "com.example.regreen.btnMap1"));
            views.setOnClickPendingIntent(R.id.btnProfile1, createPendingIntent(context, EditProfile.class, "com.example.regreen.btnProfile1"));

            // Cập nhật widget
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }

    private PendingIntent createPendingIntent(Context context, Class<?> activityClass, String action) {
        Intent intent = new Intent(context, activityClass);
        intent.setAction(action);  // Set unique action for each button
        return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }


    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        Log.d("MyAppWidget", "onReceive called");

        String action = intent.getAction();
        Log.d("MyAppWidget", "Action: " + action);

        if (action != null) {
            switch (action) {
                case "com.example.regreen.openApp":
                    Log.d("MyAppWidget", "Login button clicked");
                    startActivity(context, Login.class);
                    break;

                case "com.example.regreen.btnBooking1":
                    Log.d("MyAppWidget", "Booking button clicked");
                    startActivity(context, UserBooking.class);
                    break;

                case "com.example.regreen.btnScan1":
                    Log.d("MyAppWidget", "Scan button clicked");
                    startActivity(context, WasteIdentification.class);
                    break;

                case "com.example.regreen.btnMap1":
                    Log.d("MyAppWidget", "Map button clicked");
                    startActivity(context, RecivingPoint.class);
                    break;

                case "com.example.regreen.btnProfile1":
                    Log.d("MyAppWidget", "Profile button clicked");
                    startActivity(context, EditProfile.class);
                    break;

                default:
                    break;
            }
        }
    }

    // Helper method to start activity from onReceive
    private void startActivity(Context context, Class<?> activityClass) {
        Intent launchIntent = new Intent(context, activityClass);
        launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(launchIntent);
    }
}

