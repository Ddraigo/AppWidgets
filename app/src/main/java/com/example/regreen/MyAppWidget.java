package com.example.regreen;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.RemoteViews;

import com.example.regreen.myapplication.EditProfile;
import com.example.regreen.myapplication.LearnMore;
import com.example.regreen.myapplication.Login;
import com.example.regreen.myapplication.RecivingPoint;
import com.example.regreen.myapplication.User.UserBooking;
import com.example.regreen.myapplication.WasteIdentification;


public class MyAppWidget extends AppWidgetProvider {

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
        // Đăng ký nhận broadcast khi có thay đổi về user profile
        IntentFilter filter = new IntentFilter("com.example.regreen.USER_PROFILE_UPDATED");

        // Đăng ký BroadcastReceiver để nhận thông báo khi thông tin người dùng thay đổi
        context.registerReceiver(userProfileReceiver, filter);
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
        // Hủy đăng ký BroadcastReceiver khi widget bị vô hiệu hóa
        // Không cần phải làm gì ở đây, vì không có hành động hủy đăng ký trong yêu cầu này
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // Cập nhật widget khi có sự thay đổi
        for (int appWidgetId : appWidgetIds) {
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.my_app_widget);

            // Lấy lại tên người dùng từ SharedPreferences
            SharedPreferences sharedPreferences = context.getSharedPreferences(Login.PREF_NAME, Context.MODE_PRIVATE);
            String userName = sharedPreferences.getString("user_name", "Guest"); // Lấy tên người dùng từ SharedPreferences (mặc định là "Guest")

            // Cập nhật TextView với tên người dùng mới
            views.setTextViewText(R.id.tvHelloUser, "Xin chào " + userName); // Hiển thị tên người dùng trên widget

            // Tạo PendingIntent cho các nút trong widget để mở các activity tương ứng
            views.setOnClickPendingIntent(R.id.openApp, createPendingIntent(context, Login.class, "com.example.regreen.openApp"));
            views.setOnClickPendingIntent(R.id.btnBooking1, createPendingIntent(context, UserBooking.class, "com.example.regreen.btnBooking1"));
            views.setOnClickPendingIntent(R.id.btnLearnMore1, createPendingIntent(context, LearnMore.class, "com.example.regreen.btnLearnMore1"));
            views.setOnClickPendingIntent(R.id.btnScan1, createPendingIntent(context, WasteIdentification.class, "com.example.regreen.btnScan1"));
            views.setOnClickPendingIntent(R.id.btnMap1, createPendingIntent(context, RecivingPoint.class, "com.example.regreen.btnMap1"));
            views.setOnClickPendingIntent(R.id.btnProfile1, createPendingIntent(context, EditProfile.class, "com.example.regreen.btnProfile1"));

            // Cập nhật widget
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }

    // Phương thức tạo PendingIntent cho các nút trong widget
    private PendingIntent createPendingIntent(Context context, Class<?> activityClass, String action) {
        Intent intent = new Intent(context, activityClass);
        intent.setAction(action);  // Set unique action cho từng button
        return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT
                | PendingIntent.FLAG_IMMUTABLE);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        String action = intent.getAction();
        // Kiểm tra xem có phải broadcast thông báo cập nhật thông tin người dùng không
        if ("com.example.regreen.USER_PROFILE_UPDATED".equals(action)) {
            // Lấy đối tượng AppWidgetManager để quản lý widget
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            // Lấy tất cả các ID của widget
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, MyAppWidget.class));
            // Cập nhật lại widget với các ID đã lấy
            onUpdate(context, appWidgetManager, appWidgetIds); // Cập nhật lại widget khi thông tin người dùng thay đổi
        }

        if (action != null) {
            SharedPreferences sharedPreferences = context.getSharedPreferences(Login.PREF_NAME, Context.MODE_PRIVATE);
            boolean isLoggedIn = sharedPreferences.contains("user_name"); // Kiểm tra xem người dùng đã đăng nhập chưa
            switch (action) {
                case "com.example.regreen.openApp":
                    Log.d("MyAppWidget", "Login button clicked");
                    startActivity(context, Login.class); // Mở activity Login khi nút "Login" được nhấn
                    break;

                case "com.example.regreen.btnBooking1":
                    Log.d("MyAppWidget", "Booking button clicked");
                    startActivity(context, UserBooking.class); // Mở activity UserBooking khi nút "Booking" được nhấn
                    break;

                case "com.example.regreen.btnLearnMore1":
                    Log.d("MyAppWidget", "LearnMore button clicked");
                    startActivity(context, LearnMore.class); // Mở activity LearnMore khi nút "LearnMore" được nhấn
                    break;

                case "com.example.regreen.btnScan1":
                    Log.d("MyAppWidget", "Scan button clicked");
                    startActivity(context, WasteIdentification.class); // Mở activity WasteIdentification khi nút "Scan" được nhấn
                    break;

                case "com.example.regreen.btnMap1":
                    Log.d("MyAppWidget", "Map button clicked");
                    startActivity(context, RecivingPoint.class); // Mở activity RecivingPoint khi nút "Map" được nhấn
                    break;

                case "com.example.regreen.btnProfile1":
                    Log.d("MyAppWidget", "Profile button clicked");
                    startActivity(context, EditProfile.class); // Mở activity EditProfile khi nút "Profile" được nhấn
                    break;

                default:
                    break;
            }
        }
    }

    // Helper method để mở activity từ onReceive
    private void startActivity(Context context, Class<?> activityClass) {
        Intent launchIntent = new Intent(context, activityClass);
        launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // Đảm bảo mở activity trong context của widget
        context.startActivity(launchIntent); // Bắt đầu activity
    }

    // BroadcastReceiver để nhận thông báo khi có sự thay đổi thông tin người dùng
    private final BroadcastReceiver userProfileReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if ("com.example.regreen.USER_PROFILE_UPDATED".equals(intent.getAction())) {
                // Gọi onUpdate để làm mới widget với tên người dùng mới khi có thay đổi
                AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
                int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, MyAppWidget.class));
                onUpdate(context, appWidgetManager, appWidgetIds); // Cập nhật lại widget khi thông tin người dùng thay đổi
            }
        }
    };
}


