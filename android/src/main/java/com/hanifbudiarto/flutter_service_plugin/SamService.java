package com.hanifbudiarto.flutter_service_plugin;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import com.hanifbudiarto.flutter_service_plugin.model.MqttNotification;
import com.hanifbudiarto.flutter_service_plugin.model.MqttPayload;
import com.hanifbudiarto.flutter_service_plugin.model.User;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.IOException;
import java.math.BigInteger;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class SamService extends Service {
    private final String TAG = getClass().getSimpleName();

    // MQTT Broker
    private String broker = "somewhere_broker";
    private final String NOTIFICATION_CHANNEL_ID = "SAM_Notification_Channel_ID";
    private final int DEFAULT_QOS = 0;

    private String username = "someone_username";
    private String password = "someone_password";

    // MQTT Client and its connection options
    private MqttAndroidClient mqttAndroidClient;
    private MqttConnectOptions mqttConnectOptions;

    // list of topics and its QOS s
    private String[] topics;
    private int[] qoss;

    // database helper to load topics
    private DatabaseHelper dbHelper;

    // util
    private SimpleDateFormat formatter;

    // onCreate will be executed only once on a lifetime
    @Override
    public void onCreate() {
        super.onCreate();

        dbHelper  = new DatabaseHelper(this);
        formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        User user = dbHelper.getUser();

        if (user != null) {
            broker = "ssl://" + user.getBroker() + ":8883";
            username = user.getApiKey();
            password = user.getApiToken();

            Log.d(TAG, "onStartCommand : " + broker + " with " + username + " and " + password);
        }
        else {
            Log.d(TAG, "user is null");
        }

        // so initialize mqtt client object and its options
        initMqttOptions();
        initMqttClient();

        loadMqttTopics();

        // connect to broker
        mqttConnect();

        return Service.START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        // method not used
        return null;
    }

    @Override
    public void onDestroy() {
        if (mqttAndroidClient != null) {
            mqttAndroidClient.unregisterResources();
            mqttAndroidClient.close();
        }

        super.onDestroy();
    }

    private void loadMqttTopics() {
        Log.d(TAG, "loading topics");

        List<MqttNotification> notifications = dbHelper.getNotifications();
        int size = notifications.size();

        topics = new String[size];
        qoss = new int[size];

        int index = 0;
        for(MqttNotification notification : notifications) {
            topics[index] = notification.getTopic();
            qoss[index] = DEFAULT_QOS;

            index++;
        }

    }

    private void initMqttOptions() {
        mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setAutomaticReconnect(true);
        mqttConnectOptions.setUserName(username);
        mqttConnectOptions.setPassword(password.toCharArray());

        if (broker.contains("ssl")) {
            SocketFactory.SocketFactoryOptions socketFactoryOptions = new SocketFactory.SocketFactoryOptions();
            try {
                socketFactoryOptions.withCaInputStream(getResources().openRawResource(R.raw.cafile));
                mqttConnectOptions.setSocketFactory(new SocketFactory(socketFactoryOptions));
            } catch (IOException | NoSuchAlgorithmException | KeyStoreException | CertificateException | KeyManagementException | UnrecoverableKeyException e) {
                e.printStackTrace();
            }
        }
    }

    private void initMqttClient() {
        Log.d(TAG, "initiating");
        mqttAndroidClient = new MqttAndroidClient(this, broker, MqttClient.generateClientId() );
        mqttAndroidClient.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                Log.d(TAG, "The Connection was lost. " + cause.getMessage());
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                MqttNotification notification = dbHelper.getNotificationsByTopic(topic);

                String msg = new String(message.getPayload());
                onMessageReceived(notification, getPayload(msg));
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                Log.d(TAG, "deliveryComplete");
            }
        });
    }

    private MqttPayload getPayload(String message) {
        MqttPayload payload = null;

        if (message.contains(";")) {
            try {
                String[] splitted = message.split(";");

                if (splitted.length >= 2) {
                    payload = new MqttPayload(formatter.parse(splitted[0]), splitted[1]);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        else {
            payload = new MqttPayload(Calendar.getInstance().getTime(), message);
        }

        return payload;
    }

    private void onMessageReceived(MqttNotification notification, MqttPayload payload) {
        if (payload == null) return;

        Log.d(TAG, "Incoming message: " + payload.getValue());

        boolean isExceedThreshold = exceedThreshold(
                payload.getValue(),
                notification.getOption().getRule(),
                notification.getOption().getThreshold());
        if (isExceedThreshold) {
            if (notification.getOption().isNotify()) {
                showNotification(notification.getTopic(), buildMessage(notification, payload.getValue()));
            }

            if (notification.getOption().isAlert()) {
                launchAlarmActivity(notification, payload.getValue());
            }
        }
    }

    private boolean exceedThreshold(String value, String rule, double threshold) {
        if (rule.equals("#")) {
            // boolean datatype
            return Boolean.parseBoolean(value) == (threshold == 1);
        }

        switch (rule) {
            case ">" :
                return Double.parseDouble(value) > threshold;
            case "<" :
                return Double.parseDouble(value) < threshold;
            case "=" :
                return Double.parseDouble(value) == threshold;
            default:
                return false;
        }
    }

    private String getComparisonString(String comp) {
        switch (comp) {
            case ">": return "bigger than";
            case "<" : return "lower than";
            case "=" : return "equals";
        }

        return "";
    }

    private String buildMessage(MqttNotification notification, String value) {
        if (notification != null) {
            if (notification.getOption().getRule().equals("#")) {
                return notification.getDeviceName() + " (" + notification.getAnalyticTitle().toUpperCase()
                        + ") equals " + value.toUpperCase();
            }
            else {
                return notification.getDeviceName() + " (" + notification.getAnalyticTitle().toUpperCase() + ") equals " + value + " "
                        + " ( " +getComparisonString(notification.getOption().getRule()) + " "
                        + notification.getOption().getThreshold() + " )";
            }
        }
        return "";
    }

    private void launchAlarmActivity(MqttNotification notification, String message) {
        Intent secondIntent = new Intent(this, AlarmActivity.class);

        Bundle extras = new Bundle();
        extras.putString(AlarmActivity.EXTRA_TOPIC, notification.getTopic());
        extras.putString(AlarmActivity.EXTRA_MESSAGE, message);
        extras.putString(AlarmActivity.EXTRA_TITLE, notification.getAnalyticTitle());
        extras.putString(AlarmActivity.EXTRA_DEVICE, notification.getDeviceName());

        secondIntent.putExtras(extras);

        secondIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_HISTORY);
        startActivity(secondIntent);
    }

    private void showNotification(String topic, String message) {
        Log.d(TAG, "Trying to show notification");

        try {
            // Create an explicit intent for an Activity in your app
            Intent intent = new Intent(this, getMainActivityClass(this));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                    .setContentTitle("SAM IoT")
                    .setContentText(message)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    // Set the intent that will fire when the user taps the notification
                    .setContentIntent(pendingIntent)
                    .setVibrate(new long[] {1000, 1000, 1000})
                    .setAutoCancel(true)
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(message));

            // set notification logo
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                builder.setSmallIcon(R.drawable.ic_stat_logo_white_trans);
                builder.setColor(Color.parseColor("#FF3F51B5"));
            } else {
                builder.setSmallIcon(R.drawable.ic_stat_logo_white_trans);
            }

            // set notification ringtone
            Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            builder.setSound(alarmSound);

            // unique notification id
            BigInteger id = new BigInteger(topic.getBytes());

            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

            // notificationId is a unique int for each notification that you must define
            notificationManager.notify(id.intValue(), builder.build());
        }
        catch(Exception e) {
            Log.d(TAG, e.getMessage());
        }
        Log.d(TAG, "End notification");
    }

    private void mqttConnect() {
        Log.d(TAG, "Connecting with username : " + username + " & password : " + password + " to "+ broker);

        try {
            mqttAndroidClient.connect(mqttConnectOptions, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.d(TAG, "Successfully connected");

                    DisconnectedBufferOptions disconnectedBufferOptions = new DisconnectedBufferOptions();
                    disconnectedBufferOptions.setBufferEnabled(true);
                    disconnectedBufferOptions.setBufferSize(100);
                    disconnectedBufferOptions.setPersistBuffer(false);
                    disconnectedBufferOptions.setDeleteOldestMessages(false);
                    mqttAndroidClient.setBufferOpts(disconnectedBufferOptions);

                    subscribeTopics();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.d(TAG, "Failed to connect mqttConnect : " + exception.getMessage());
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    private Class getMainActivityClass(Context context) {
        String packageName = context.getPackageName();
        Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(packageName);
        String className = launchIntent.getComponent().getClassName();
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void subscribeTopics() {
        try {
            if (topics != null && topics.length > 0 && qoss != null && qoss.length > 0) {
                mqttAndroidClient.subscribe(topics, qoss, null, new IMqttActionListener() {
                    @Override
                    public void onSuccess(IMqttToken asyncActionToken) {
                        Log.d(TAG, "Successfully subscribed");
                        for(String topic : topics) {
                            Log.d(TAG, "topic : " + topic);
                        }
                    }

                    @Override
                    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                        Log.d(TAG, "Gagal subscribed " + exception.getMessage());
                    }
                });
            }
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
}
