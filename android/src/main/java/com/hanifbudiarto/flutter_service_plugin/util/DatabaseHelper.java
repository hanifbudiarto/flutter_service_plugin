package com.hanifbudiarto.flutter_service_plugin.util;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hanifbudiarto.flutter_service_plugin.model.AppSettings;
import com.hanifbudiarto.flutter_service_plugin.model.MqttNotification;
import com.hanifbudiarto.flutter_service_plugin.model.MqttOption;
import com.hanifbudiarto.flutter_service_plugin.model.User;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;

public class DatabaseHelper extends SQLiteOpenHelper {
    Gson gson = new GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .create();

    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "samelement.db";

    public DatabaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {}

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {}

    public MqttNotification getNotificationsByTopic(String topic) {
        MqttNotification notification = null;

        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "select analytic_id, analytic_resource_id, topic, options, device_name, analytic_title from notification where topic = '" +topic+"' limit 1";

        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                MqttOption option = gson.fromJson(cursor.getString(3), MqttOption.class);

                MqttNotification result = new MqttNotification(
                        cursor.getString(0),
                        cursor.getString(1),
                        cursor.getString(2),
                        option,
                        cursor.getString(4),
                        cursor.getString(5)
                );

                notification = result;

            } while (cursor.moveToNext());
        }

        // close db connection
        db.close();

        return notification;
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

            // close db connection
            db.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return user;
    }

    public List<MqttNotification> getNotifications() {
        List<MqttNotification> notifications = new ArrayList<>();

        try {
            SQLiteDatabase db = this.getReadableDatabase();
            String selectQuery = "select analytic_id, analytic_resource_id, topic, options, device_name, analytic_title from notification";

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
                            cursor.getString(5)
                    );

                    notifications.add(notification);
                } while (cursor.moveToNext());
            }

            // close cursor
            cursor.close();

            // close db connection
            db.close();
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

            // close db connection
            db.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return appSettings;
    }

}
