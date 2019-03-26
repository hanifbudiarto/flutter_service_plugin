package com.hanifbudiarto.flutter_service_plugin.service;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.hanifbudiarto.flutter_service_plugin.screen.AlarmActivity;
import com.hanifbudiarto.flutter_service_plugin.R;
import com.hanifbudiarto.flutter_service_plugin.model.MqttNotification;
import com.hanifbudiarto.flutter_service_plugin.model.MqttPayload;
import com.hanifbudiarto.flutter_service_plugin.model.User;
import com.hanifbudiarto.flutter_service_plugin.util.DatabaseHelper;
import com.hanifbudiarto.flutter_service_plugin.util.NotificationHelper;
import com.hanifbudiarto.flutter_service_plugin.util.SocketFactory;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
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
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import androidx.annotation.Nullable;

public class SamService extends Service {
    // error tag
    private final String TAG = getClass().getSimpleName();

    private final String NOTIFICATION_TITLE = "SAM IoT";

    // reconnect and clean session variable for mqtt connect options
    private final boolean RECONNECT = true;
    private final boolean CLEAN_SESSION = true;

    // default MQTT QoS
    private final int DEFAULT_QOS = 0;

    // MQTT Broker
    // because broker can be changed sometimes
    // and programmer don't want to deal with changing it every time.
    // broker is saved in SQLite database from flutter main application
    private String broker = "somewhere_broker";

    // we also save the username and password for this broker
    private String username = "someone_username";
    private String password = "someone_password";

    // MQTT Client and its connection options
    private MqttAndroidClient mqttAndroidClient;
    private MqttConnectOptions mqttConnectOptions;

    // list of topics and its QOS s
    private String[] topics;
    private int[] qoss;

    // database helper to load topics, broker address etc.
    private DatabaseHelper dbHelper;

    // utility class for converting date format
    private SimpleDateFormat formatter;

    // now notification has its own class
    private NotificationHelper notificationHelper;

    private void initBrokerAndAuth() {
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
    }

    private void initMqttOptions() {
        mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setAutomaticReconnect(RECONNECT);
        mqttConnectOptions.setCleanSession(CLEAN_SESSION);
        mqttConnectOptions.setUserName(username);
        mqttConnectOptions.setPassword(password.toCharArray());

        // load certificate file if this broker connect via secure ssl
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
                    Log.e(TAG, "Reconnected");
                }
                else {
                    Log.e(TAG, "Connected");
                }

                subscribe();
            }

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

    private void initTopics() {
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

    // subscribe to broker
    private void subscribe() {
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
                        Log.d(TAG, "Failed to subscribe " + exception.getMessage());
                    }
                });
            }
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }


    private void connect() {
        Thread connectThread = new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "Connecting with username : " + username + " & password : " + password + " to "+ broker);

                try {
                    mqttAndroidClient.connect(mqttConnectOptions, null, new IMqttActionListener() {
                        @Override
                        public void onSuccess(IMqttToken asyncActionToken) {
                            Log.d(TAG, "Successfully connected");
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
        });

        connectThread.run();
    }

    // onCreate will be executed only once on a lifetime
    @Override
    public void onCreate() {
        super.onCreate();

        // initialize helper class
        notificationHelper = new NotificationHelper(this);
        dbHelper  = new DatabaseHelper(this);
        formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // initialize MQTT core elements
        initBrokerAndAuth();
        initMqttOptions();
        initMqttClient();
        initTopics();

        // connecting
        connect();

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

    // splitting string resulted from mqtt received
    // for value result, contains datetime. (<datetime>;<value>)
    // for boolean result, only value (true or false)
    private MqttPayload getPayload(String message) {
        MqttPayload payload = null;

        if (message.contains(";")) {
            try {
                String[] separatedMessage = message.split(";");

                if (separatedMessage.length >= 2) {
                    payload = new MqttPayload(formatter.parse(separatedMessage[0]), separatedMessage[1]);
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
                showNotification(notification, payload.getValue());
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

    // build message for notification message on notification bar/action bar
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

    private void launchAlarmActivity(MqttNotification notification, String valueReceived) {
        Intent secondIntent = new Intent(this, AlarmActivity.class);

        Bundle extras = new Bundle();
        extras.putString(AlarmActivity.EXTRA_VALUE, valueReceived);
        extras.putString(AlarmActivity.EXTRA_TITLE, notification.getAnalyticTitle());
        extras.putString(AlarmActivity.EXTRA_DEVICE, notification.getDeviceName());

        secondIntent.putExtras(extras);

        secondIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        startActivity(secondIntent);
    }

    private void showNotification(MqttNotification notification, String valueReceived) {
        Log.d(TAG, "Trying to show notification");
        String topic = notification.getTopic();
        String message = buildMessage(notification, valueReceived);

        notificationHelper.createNotification(topic, NOTIFICATION_TITLE, message);
        Log.d(TAG, "End notification");
    }
}
