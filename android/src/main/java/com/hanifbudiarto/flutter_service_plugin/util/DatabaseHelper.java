package com.hanifbudiarto.flutter_service_plugin.util;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.hanifbudiarto.flutter_service_plugin.model.AppSettings;
import com.hanifbudiarto.flutter_service_plugin.model.DeviceNotification;
import com.hanifbudiarto.flutter_service_plugin.model.MqttNotification;
import com.hanifbudiarto.flutter_service_plugin.model.MqttOption;
import com.hanifbudiarto.flutter_service_plugin.model.User;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    private Gson gson = new Gson();
    private static final int DATABASE_VERSION = 4;
    private static final String DATABASE_NAME = "samelement.db";
    public static DatabaseHelper instance;

    public static synchronized DatabaseHelper getHelper(Context context)
    {
        if (instance == null)
            instance = new DatabaseHelper(context);

        return instance;
    }

    public DatabaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public List<MqttNotification> getAllNotificationsByTopic(String topic) {
        List<MqttNotification> notifications = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "select analytic_id, analytic_resource_id, topic, options, device_name, " +
                "analytic_title, analytic_model from notification where topic = '" + topic + "'";

        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                MqttOption option = gson.fromJson(cursor.getString(3), MqttOption.class);

                MqttNotification notification = new MqttNotification(
                        cursor.getString(0),
                        cursor.getString(1),
                        cursor.getString(2),
                        option,
                        cursor.getString(4),
                        cursor.getString(5),
                        cursor.getString(6)
                );

                notifications.add(notification);

            } while (cursor.moveToNext());
        }

        return notifications;
    }

    public User getUser() {
        User user = null;

        try {
            SQLiteDatabase db = this.getReadableDatabase();
            String selectQuery = "select api_username, api_password, broker from user limit 1";

            Cursor cursor = db.rawQuery(selectQuery, null);

            // looping through all rows and adding to list
            if (cursor.moveToFirst()) {
                do {
                    user = new User(cursor.getString(0),
                            cursor.getString(1),
                            cursor.getString(2));

                } while (cursor.moveToNext());
            }

            // close cursor
            cursor.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return user;
    }

    public List<MqttNotification> getNotifications() {
        List<MqttNotification> notifications = new ArrayList<>();

        try {
            SQLiteDatabase db = this.getReadableDatabase();
            String selectQuery = "select analytic_id, analytic_resource_id, topic, options, " +
                    "device_name, analytic_title, analytic_model from notification";

            Cursor cursor = db.rawQuery(selectQuery, null);

            // looping through all rows and adding to list
            if (cursor.moveToFirst()) {
                do {
                    MqttOption option = gson.fromJson(cursor.getString(3), MqttOption.class);

                    MqttNotification notification = new MqttNotification(
                            cursor.getString(0),
                            cursor.getString(1),
                            cursor.getString(2),
                            option,
                            cursor.getString(4),
                            cursor.getString(5),
                            cursor.getString(6)
                    );

                    notifications.add(notification);
                } while (cursor.moveToNext());
            }

            // close cursor
            cursor.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return notifications;
    }

    public AppSettings getAppSettings() {
        AppSettings appSettings = null;

        try {
            SQLiteDatabase db = this.getReadableDatabase();
            String selectQuery = "select app_theme from app_settings";

            Cursor cursor = db.rawQuery(selectQuery, null);

            // looping through all rows and adding to list
            if (cursor.moveToFirst()) {
                do {
                    appSettings = new AppSettings(cursor.getString(0));
                } while (cursor.moveToNext());
            }

            // close cursor
            cursor.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return appSettings;
    }

    public void updateDeviceState(String deviceId, String state) {
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "update device_notification set last_state ='"+state+"' where device_id='"+deviceId+"'";

        db.rawQuery(query, null);
    }

    public List<DeviceNotification> getDeviceNotificationByTopic(String topic) {
        List<DeviceNotification> notifications = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "select device_id, device_name, device_sn, developer_id, " +
                "device_notify, device_alarm, topic, last_state, notify_checked, alarm_checked " +
                "from device_notification where topic='" + topic + "'";

        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                DeviceNotification notification = new DeviceNotification(
                        cursor.getString(0),
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getString(3),
                        cursor.getInt(4),
                        cursor.getInt(5),
                        cursor.getString(6),
                        cursor.getString(7),
                        cursor.getString(8),
                        cursor.getString(9)
                );

                notifications.add(notification);

            } while (cursor.moveToNext());
        }

        return notifications;
    }

    public List<DeviceNotification> getDeviceNotifications() {
        List<DeviceNotification> notifications = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "select device_id, device_name, device_sn, developer_id, " +
                "device_notify, device_alarm, topic, last_state, notify_checked, alarm_checked " +
                "from device_notification";

        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                DeviceNotification notification = new DeviceNotification(
                        cursor.getString(0),
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getString(3),
                        cursor.getInt(4),
                        cursor.getInt(5),
                        cursor.getString(6),
                        cursor.getString(7),
                        cursor.getString(8),
                        cursor.getString(9)
                );

                notifications.add(notification);

            } while (cursor.moveToNext());
        }

        return notifications;
    }
}
