package com.hanifbudiarto.flutter_service_plugin;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.google.gson.Gson;
import com.hanifbudiarto.flutter_service_plugin.model.MqttNotification;
import com.hanifbudiarto.flutter_service_plugin.model.MqttOption;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;

public class DatabaseHelper extends SQLiteOpenHelper {
    Gson gson = new Gson();

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

    MqttNotification getNotificationsByTopic(String topic) {
        MqttNotification notification = null;

        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "select analytic_id, analytic_resource_id, topic, options from notification where topic = '" +topic+"' limit 1";

        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                MqttOption option = gson.fromJson(cursor.getString(3), MqttOption.class);

                MqttNotification result = new MqttNotification(
                        cursor.getString(0),
                        cursor.getString(1),
                        cursor.getString(2),
                        option
                );

                notification = result;

            } while (cursor.moveToNext());
        }

        // close db connection
        db.close();

        return notification;
    }

    List<MqttNotification> getNotifications() {
        List<MqttNotification> notifications = new ArrayList<>();

        try {
            SQLiteDatabase db = this.getReadableDatabase();
            String selectQuery = "select analytic_id, analytic_resource_id, topic, options from notification";

            Cursor cursor = db.rawQuery(selectQuery, null);

            // looping through all rows and adding to list
            if (cursor.moveToFirst()) {
                do {
                    MqttOption option = gson.fromJson(cursor.getString(3), MqttOption.class);

                    MqttNotification notification = new MqttNotification(
                            cursor.getString(0),
                            cursor.getString(1),
                            cursor.getString(2),
                            option
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
}
