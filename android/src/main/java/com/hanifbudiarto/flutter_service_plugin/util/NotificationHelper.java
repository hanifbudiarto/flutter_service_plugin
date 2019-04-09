package com.hanifbudiarto.flutter_service_plugin.util;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.provider.Settings;

import com.hanifbudiarto.flutter_service_plugin.R;
import com.hanifbudiarto.flutter_service_plugin.util.FlutterUtil;

import java.math.BigInteger;
import java.util.Calendar;

import androidx.core.app.NotificationCompat;

public class NotificationHelper {

    public static final String CHANNEL_ID = "SAMElementNotificationChannelID";
    public static final String CHANNEL_NAME = "SAMElementNotificationChannelName";

    private Context context;

    public NotificationHelper(Context context) {
        this.context = context;
    }

    public void createNotification(String topic, String title, String message, Bitmap largeIcon) {
        try {
            // unique notification id
            BigInteger notificationId = new BigInteger(topic.getBytes());

            // Create an explicit intent for an Activity in your app
            Intent intent = new Intent(context, FlutterUtil.getMainActivityClass(context));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

            // create builder
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID);
            builder.setContentTitle(title)
                    .setContentText(message)
                    .setAutoCancel(true)
                    .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                    .setVibrate(new long[] {1000, 1000, 1000})
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setShowWhen(true)
                    .setWhen(Calendar.getInstance().getTimeInMillis())
                    .setContentIntent(pendingIntent);

            /*
            * // set notification ringtone
            Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            builder.setSound(alarmSound);
            * */

            // set notification logo
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                builder.setSmallIcon(R.drawable.ic_sam_notification_logo_trans);
                builder.setLargeIcon(largeIcon);
                builder.setColor(Color.parseColor("#FF3F51B5"));
            } else {
                builder.setSmallIcon(R.drawable.ic_sam_notification_logo_trans);
                builder.setLargeIcon(largeIcon);
            }

            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O)
            {
                int importance = NotificationManager.IMPORTANCE_HIGH;
                NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance);
                notificationChannel.enableLights(true);
                notificationChannel.setLightColor(Color.RED);
                notificationChannel.enableVibration(true);
                notificationChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});

                notificationManager.createNotificationChannel(notificationChannel);
            }

            notificationManager.notify(notificationId.intValue(), builder.build());
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
