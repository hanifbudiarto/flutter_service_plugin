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

        MqttConnectOptions cobaMqttConnectOptions = new MqttConnectOptions();
        cobaMqttConnectOptions.setCleanSession(true);
        cobaMqttConnectOptions.setAutomaticReconnect(true);
        cobaMqttConnectOptions.setUserName(username);
        cobaMqttConnectOptions.setPassword(password.toCharArray());

        if (broker.contains("ssl")) {
            SocketFactory.SocketFactoryOptions socketFactoryOptions = new SocketFactory.SocketFactoryOptions();
            try {
                socketFactoryOptions.withCaInputStream(getResources().openRawResource(R.raw.cafile));
                cobaMqttConnectOptions.setSocketFactory(new SocketFactory(socketFactoryOptions));
            } catch (IOException | NoSuchAlgorithmException | KeyStoreException | CertificateException | KeyManagementException | UnrecoverableKeyException e) {
                e.printStackTrace();
            }
        }

        final MqttAndroidClient cobaMqttAndroidClient = new MqttAndroidClient(this, broker, MqttClient.generateClientId() );
        cobaMqttAndroidClient.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {
                if (reconnect) {
                    Log.d(TAG, "Reconnecting broo");
                }

                else Log.d(TAG, "Connected to broker bro");
            }

            @Override
            public void connectionLost(Throwable cause) {

            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {

            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });

        try {
            cobaMqttAndroidClient.connect(cobaMqttConnectOptions, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.d(TAG, "success koneek");

                    DisconnectedBufferOptions disconnectedBufferOptions = new DisconnectedBufferOptions();
                    disconnectedBufferOptions.setBufferEnabled(true);
                    disconnectedBufferOptions.setBufferSize(100);
                    disconnectedBufferOptions.setPersistBuffer(false);
                    disconnectedBufferOptions.setDeleteOldestMessages(false);
                    cobaMqttAndroidClient.setBufferOpts(disconnectedBufferOptions);


                    try {
                        cobaMqttAndroidClient.subscribe(topics, qoss, null, new IMqttActionListener() {
                            @Override
                            public void onSuccess(IMqttToken asyncActionToken) {
                                Log.d(TAG, "Successfully subscribed broo");
                            }

                            @Override
                            public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                                Log.d(TAG, "Gagal subcribe "+ exception.getMessage());
                            }
                        });
                    } catch (MqttException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.d(TAG, "Gatot konek");
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }


//        // so initialize mqtt client object and its options
//        initMqttOptions();
//        initMqttClient();
//
//        // get topics from database
//        loadMqttTopics();
//
//        // connect to broker
//        mqttConnect();
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


    // no problem
    private void initMqttOptions() {
        mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setCleanSession(true);
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
        mqttAndroidClient.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {
                if (reconnect) {
                    Log.d(TAG, "reconnect to "+ broker);
                    Log.d(TAG, "Reconnecting with username : " + username + " & password : " + password + " to "+ broker);
                    subscribeTopics();
                }
                else {
                    Log.d(TAG, "connected to "+ broker);
                }
            }

            @Override
            public void connectionLost(Throwable cause) {
                Log.d(TAG, "The Connection was lost. " + cause.getMessage());
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                String msg = new String(message.getPayload());
                onMessageReceived(topic, getPayload(msg));
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
                    payload = new MqttPayload(formatter.parse(splitted[0]), Double.parseDouble(splitted[1]));
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        return payload;
    }

    private void onMessageReceived(String topic, MqttPayload payload) {
        if (payload == null) return;

        Log.d(TAG, "Incoming message: " + payload.getValue());

        MqttNotification notification = dbHelper.getNotificationsByTopic(topic);
        boolean isExceedThreshold = exceedThreshold(
                payload.getValue(),
                notification.getOption().getRule(),
                notification.getOption().getThreshold());
        if (isExceedThreshold) {
            if (notification.getOption().isNotify()) {
                showNotification(topic, buildMessage(notification, payload.getValue()));
            }

            if (notification.getOption().isAlert()) {
                launchAlarmActivity(topic, buildMessage(notification, payload.getValue()));
            }
        }
    }

    private boolean exceedThreshold(double value, String rule, double threshold) {
        switch (rule) {
            case ">" :
                return value > threshold;
            case "<" :
                return value < threshold;
            case "=" :
                return value == threshold;
            default:
                return false;
        }
    }

    private String buildMessage(MqttNotification notification, double value) {
        if (notification != null) {
            return "Analytic ID ini " + notification.getAnalyticId() + " "
                    + notification.getOption().getRule() + " "
                    + notification.getOption().getThreshold()
                    + " nilainya segini " + value;
        }
        return "";
    }

    private void launchAlarmActivity(String topic, String message) {
        Intent secondIntent = new Intent(this, AlarmActivity.class);

        Bundle extras = new Bundle();
        extras.putString(AlarmActivity.EXTRA_TOPIC, topic);
        extras.putString(AlarmActivity.EXTRA_MESSAGE, message);

        secondIntent.putExtras(extras);

        secondIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
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
                    .setAutoCancel(true);

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
                    Log.d(TAG, "Failed to connect : " + exception.getMessage());
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
            mqttAndroidClient.subscribe(topics, qoss, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.d(TAG, "Successfully subscribed");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {

                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
}
