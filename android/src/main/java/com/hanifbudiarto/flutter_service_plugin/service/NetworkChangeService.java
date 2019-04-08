package com.hanifbudiarto.flutter_service_plugin.service;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

public class NetworkChangeService extends Service {

    private final String TAG = NetworkChangeService.class.getSimpleName();

    private IntentFilter intentFilter;
    private NetworkChangeReceiver networkChangeReceiver;

    @Override
    public void onCreate() {
        super.onCreate();

        intentFilter = new IntentFilter();
        String actionBootCompleted = "android.intent.action.BOOT_COMPLETED";
        intentFilter.addAction(actionBootCompleted);
        String actionConnectionChange = "android.net.conn.CONNECTIVITY_CHANGE";
        intentFilter.addAction(actionConnectionChange);

        networkChangeReceiver = new NetworkChangeReceiver();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        registerReceiver(networkChangeReceiver, intentFilter);
        Log.e(TAG, "Network Change Receiver registered!");

        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(networkChangeReceiver);
        Log.e(TAG, "Network Change Receiver unregistered!");

        super.onDestroy();
    }
}
