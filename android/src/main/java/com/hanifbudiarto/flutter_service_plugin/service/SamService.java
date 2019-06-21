package com.hanifbudiarto.flutter_service_plugin.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.hanifbudiarto.flutter_service_plugin.R;
import com.hanifbudiarto.flutter_service_plugin.model.DeviceNotification;
import com.hanifbudiarto.flutter_service_plugin.model.MqttNotification;
import com.hanifbudiarto.flutter_service_plugin.model.MqttPayload;
import com.hanifbudiarto.flutter_service_plugin.model.User;
import com.hanifbudiarto.flutter_service_plugin.screen.AlarmActivity;
import com.hanifbudiarto.flutter_service_plugin.screen.AlertActivity;
import com.hanifbudiarto.flutter_service_plugin.util.AnalyticIconHelper;
import com.hanifbudiarto.flutter_service_plugin.util.DatabaseHelper;
import com.hanifbudiarto.flutter_service_plugin.util.NotificationHelper;
import com.hanifbudiarto.flutter_service_plugin.util.SocketFactory;
import com.hanifbudiarto.flutter_service_plugin.util.StateIndicatorHelper;

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
import java.util.Date;
import java.util.List;
import java.util.Locale;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

public class SamService extends Service {
    // error tag
    private final String TAG = getClass().getSimpleName();

    private int notificationId = -999;

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
    private boolean[] firstMessages;

    // utility class for converting date format
    private SimpleDateFormat formatter;

    // now notification has its own class
    private NotificationHelper notificationHelper;

    private void initBrokerAndAuth() {
        User user = DatabaseHelper.getHelper(SamService.this).getUser();

        if (user != null) {
            broker = "ssl://" + user.getBroker() + ":8883";
            username = user.getApiKey();
            password = user.getApiToken();

            Log.d(TAG, "onStartCommand : " + broker + " with " + username + " and " + password);
        } else {
            Log.d(TAG, "user is null");
        }
    }

    private void initMqttOptions() {
        mqttConnectOptions = new MqttConnectOptions();
        // reconnect and clean session variable for mqtt connect options
        mqttConnectOptions.setAutomaticReconnect(true);
        mqttConnectOptions.setCleanSession(true);
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

    private int indexOf(String topic, String[] myTopics) {
        for(int i=0; i<myTopics.length; i++) {
            if (myTopics.equals(topic)) return i;
        }

        return -1;
    }

    private void initMqttClient() {
        Log.d(TAG, "initiating");
        mqttAndroidClient = new MqttAndroidClient(this, broker, MqttClient.generateClientId());
        mqttAndroidClient.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {
                if (reconnect) {
                    Log.e(TAG, "Reconnected");
                } else {
                    Log.e(TAG, "Connected");
                }

                updateNotificationContent("Connected");

                subscribe();
            }

            @Override
            public void connectionLost(Throwable cause) {
                Log.d(TAG, "The Connection was lost. " + cause.getMessage());
                updateNotificationContent("Connection was lost");
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                int index = indexOf(topic, topics);
                if (index < 0) return;

                if (firstMessages[index] == true) {
                    firstMessages[index] = false;
                    return;
                }

                List<MqttNotification> notifications = DatabaseHelper.getHelper(SamService.this).getAllNotificationsByTopic(topic);
                if (notifications != null && notifications.size() > 0) {
                    for (MqttNotification notification : notifications) {
                        String msg = new String(message.getPayload());
                        onMessageReceived(notification, getPayload(msg));
                    }
                }

                List<DeviceNotification> deviceNotifications = DatabaseHelper.getHelper(SamService.this).getDeviceNotificationByTopic(topic);
                if (deviceNotifications != null && deviceNotifications.size() > 0) {
                    for (DeviceNotification notification : deviceNotifications) {
                        String msg = new String(message.getPayload());
                        onStateChanges(notification, msg);
                    }
                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                Log.d(TAG, "deliveryComplete");
            }
        });
    }

    private void initTopics() {
        Log.d(TAG, "loading topics");

        List<MqttNotification> notifications = DatabaseHelper.getHelper(SamService.this).getNotifications();
        List<DeviceNotification> deviceNotifications = DatabaseHelper.getHelper(SamService.this).getDeviceNotifications();

        int size = notifications.size() + deviceNotifications.size();
        int defaultQos = 0;

        topics = new String[size];
        qoss = new int[size];

        int index = 0;
        for (MqttNotification notification : notifications) {
            topics[index] = notification.getTopic();
            qoss[index] = defaultQos;

            index++;
        }

        for (DeviceNotification notification : deviceNotifications) {
            // ${widget.device.developerId}/${widget.device.sn}/\$state
            topics[index] = notification.getDeveloperId() + "/" + notification.getDeviceSn() + "/$state";
            qoss[index] = defaultQos;

            index++;
        }
    }

    private void initFirstMessages() {
        firstMessages = new boolean[topics.length];

        for (int i=0; i<topics.length; i++) {
            firstMessages[i] = true;
        }
    }

    // subscribe to broker
    private void subscribe() {
        try {
            if (topics != null && topics.length > 0 && qoss != null && qoss.length > 0) {

                initFirstMessages();

                mqttAndroidClient.subscribe(topics, qoss, null, new IMqttActionListener() {
                    @Override
                    public void onSuccess(IMqttToken asyncActionToken) {
                        Log.d(TAG, "Successfully subscribed");
                        for (String topic : topics) {
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
                Log.d(TAG, "Connecting with username : " + username + " & password : " + password + " to " + broker);

                try {
                    mqttAndroidClient.connect(mqttConnectOptions, null, new IMqttActionListener() {
                        @Override
                        public void onSuccess(IMqttToken asyncActionToken) {
                            Log.d(TAG, "Successfully connected");
                            updateNotificationContent("Connected");
                        }

                        @Override
                        public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                            Log.d(TAG, "Failed to connect : " + exception.getMessage());
                            updateNotificationContent("Failed to connect");
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
        formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (mqttAndroidClient == null || !mqttAndroidClient.isConnected()) {
            // initialize MQTT core elements
            initBrokerAndAuth();
            initMqttOptions();
            initMqttClient();
            initTopics();

            // connecting
            connect();
        }

        notificationId = createId();

        startForeground(notificationId, getNotificationForForegroundService("Initializing"));

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

    private Notification getNotificationForForegroundService(String content) {
        NotificationManager notificationManager = (NotificationManager)
                getSystemService(Context.NOTIFICATION_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(
                    NotificationHelper.CHANNEL_ID, NotificationHelper.CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(notificationChannel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this,
                NotificationHelper.CHANNEL_ID)
                .setContentTitle("Server Status")
                .setContentText(content)
                .setOngoing(true);

        // set notification logo
        Bitmap icon = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
        builder.setSmallIcon(R.drawable.ic_link);
        builder.setLargeIcon(icon);

        return builder.build();

    }

    private void updateNotificationContent(String content) {
        NotificationManager notificationManager = (NotificationManager)
                getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(notificationId, getNotificationForForegroundService(content));
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

        } else {
            payload = new MqttPayload(Calendar.getInstance().getTime(), message);
        }

        return payload;
    }

    private boolean isStateChecked(String checkedString, String state) {
        if (checkedString.isEmpty()) return false;

        return checkedString.contains(state.substring(0, 1));
    }

    private void onStateChanges(DeviceNotification notification, String state) {
        Log.d(TAG, "State changed to: " + state);
        if (notification.isNotify() && !state.equals(notification.getLastState())
                && isStateChecked(notification.getNotifyChecked(), state)) {
            showDeviceNotification(notification, state);
        }

        if (notification.isAlarm() && !state.equals(notification.getLastState())
                && isStateChecked(notification.getAlarmChecked(), state)) {
            launchAlertActivity(notification, state);
        }

        DatabaseHelper.getHelper(SamService.this).updateDeviceState(notification.getDeviceId(), state);
    }

    private void onMessageReceived(MqttNotification notification, MqttPayload payload) {
        if (payload == null) return;

        Log.d(TAG, "notification " + notification.getOption().getRule()
                + " " + notification.getOption().getThreshold());

        Log.d(TAG, "Incoming message: " + payload.getValue());

        boolean isExceedThreshold = exceedThreshold(
                payload.getValue(),
                notification.getOption().getRule(),
                notification.getOption().getThreshold());

        Log.d(TAG, "Exceed threshold: " + isExceedThreshold);

        if (isExceedThreshold) {

            Log.d(TAG, "IsNotify: " + notification.getOption().isNotify() + " IsAlert: " + notification.getOption().isAlert());
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
            case ">":
                return Double.parseDouble(value) > threshold;
            case "<":
                return Double.parseDouble(value) < threshold;
            case "=":
                return Double.parseDouble(value) == threshold;
            default:
                return false;
        }
    }

    private String getComparisonString(String comp) {
        switch (comp) {
            case ">":
                return "bigger than";
            case "<":
                return "lower than";
            case "=":
                return "equals";
        }

        return "";
    }

    // build message for notification message on notification bar/action bar
    private String buildMessage(MqttNotification notification, String value) {
        if (notification != null) {
            if (notification.getOption().getRule().equals("#")) {
                return value.toUpperCase();
            } else {
                return value.toUpperCase() + " ( " + getComparisonString(notification.getOption().getRule()) + " "
                        + notification.getOption().getThreshold() + " )";
            }
        }
        return "";
    }

    private void launchAlertActivity(DeviceNotification notification, String state) {
        Intent secondIntent = new Intent(this, AlertActivity.class);

        Bundle extras = new Bundle();
        extras.putString(AlarmActivity.EXTRA_VALUE, state);
        extras.putString(AlarmActivity.EXTRA_TITLE, notification.getDeviceName());
        extras.putString(AlarmActivity.EXTRA_DEVICE, notification.getDeviceSn());

        secondIntent.putExtras(extras);

        secondIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        startActivity(secondIntent);
    }

    private void launchAlarmActivity(MqttNotification notification, String valueReceived) {
        Intent secondIntent = new Intent(this, AlarmActivity.class);

        Bundle extras = new Bundle();

        if (notification.getOption().getText() != null && notification.getOption().getText().length() > 0) {
            valueReceived = notification.getOption().getText();
        }
        extras.putString(AlarmActivity.EXTRA_VALUE, valueReceived);
        extras.putString(AlarmActivity.EXTRA_TITLE, notification.getAnalyticTitle());
        extras.putString(AlarmActivity.EXTRA_DEVICE, notification.getDeviceName());

        secondIntent.putExtras(extras);

        secondIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        startActivity(secondIntent);
    }

    private void showDeviceNotification(DeviceNotification notification, String state) {
        Log.d(TAG, "Trying to show device notification");
        String message = notification.getDeviceName() + " is " + StateIndicatorHelper.getStateName(state);
        String notificationTitle = notification.getDeviceName();
        Bitmap largeIcon = StateIndicatorHelper.getStateImage(getResources(), state);

        notificationHelper.createNotification(notification.getDeviceSn()
                        + notification.getDeviceId() + notification.getDeveloperId(),
                notificationTitle, message, largeIcon);
        Log.d(TAG, "End notification");
    }

    private void showNotification(MqttNotification notification, String valueReceived) {
        Log.d(TAG, "Trying to show notification");
        String message = buildMessage(notification, valueReceived);
        if (notification.getOption().getText() != null && notification.getOption().getText().length() > 0) {
            message = notification.getOption().getText();
        }

        String notificationTitle = notification.getAnalyticTitle();
        Bitmap largeIcon = AnalyticIconHelper.getLargeIconBitmap(getResources(), notification.getAnalyticModel());

        notificationHelper.createNotification(notification.getAnalyticId(), notificationTitle, message, largeIcon);
        Log.d(TAG, "End notification");
    }

    private int createId() {
        Date now = new Date();
        int id = Integer.parseInt(new SimpleDateFormat("ddHHmmss", Locale.US).format(now));
        return id;
    }

}
