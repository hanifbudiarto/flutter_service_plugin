package com.hanifbudiarto.flutter_service_plugin;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.hanifbudiarto.flutter_service_plugin.model.MqttNotification;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class SamService extends Service {
    private final String TAG = getClass().getSimpleName();
    private final String BROKER = "ssl://iot.samelement.com:8883";

    private String clientId = MqttClient.generateClientId();

    private MqttAndroidClient mqttAndroidClient;
    private MqttConnectOptions mqttConnectOptions;

    private String[] topics;
    private int[] qoss;

    private DatabaseHelper dbHelper = new DatabaseHelper(this);

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        loadMqttTopics();
        initializeMqtt();
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
            try {
                mqttAndroidClient.unsubscribe("#");
            } catch (MqttException e) {
                e.printStackTrace();
            }
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
        qoss = new int[size] ;

        int index = 0;
        for(MqttNotification notification : notifications) {
            topics[index] = notification.getTopic();
            qoss[index] = 0; // QOS 0

            index++;
        }

    }

    private void initializeMqtt() {
        Log.d(TAG, "initiating");
        mqttAndroidClient = new MqttAndroidClient(this, BROKER, clientId );
        mqttAndroidClient.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                Log.d(TAG, "connectionLost");
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) {
                Log.d(TAG, topic + " : " + message.toString());
                showNotification(message.toString());
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                Log.d(TAG, "deliveryComplete");
            }
        });
        mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setCleanSession(true);
        mqttConnectOptions.setAutomaticReconnect(true);
        mqttConnectOptions.setUserName("c5ac741d90b0cf7b17a462d34aaaaaf2");
        mqttConnectOptions.setPassword("987213c28d48e5d3c562731fc126123c".toCharArray());

        if (BROKER.contains("ssl")) {
            SocketFactory.SocketFactoryOptions socketFactoryOptions = new SocketFactory.SocketFactoryOptions();
            try {
                socketFactoryOptions.withCaInputStream(getResources().openRawResource(R.raw.cafile));
                mqttConnectOptions.setSocketFactory(new SocketFactory(socketFactoryOptions));
            } catch (IOException | NoSuchAlgorithmException | KeyStoreException | CertificateException | KeyManagementException | UnrecoverableKeyException e) {
                e.printStackTrace();
            }
        }
    }

    private void showNotification(String message) {
        Log.d(TAG, "Trying to show notification");

        try {
            // Create an explicit intent for an Activity in your app
            Intent intent = new Intent(this, getMainActivityClass(this));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "SAM_channel_id")
                    .setSmallIcon(R.drawable.ic_launcher)
                    .setContentTitle("SAM IoT")
                    .setContentText(message)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    // Set the intent that will fire when the user taps the notification
                    .setContentIntent(pendingIntent)
                    .setVibrate(new long[] {1000})
                    .setAutoCancel(true);


            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

            // notificationId is a unique int for each notification that you must define
            notificationManager.notify(1, builder.build());
        }
        catch(Exception e) {
            Log.d(TAG, e.getMessage());
        }
        Log.d(TAG, "End notification");
    }

    private void mqttConnect() {
        try {
            mqttAndroidClient.connect(mqttConnectOptions, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.d(TAG, "Successfully connected");
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
