package com.hanifbudiarto.flutter_service_plugin.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class NetworkChangeReceiver extends BroadcastReceiver {
    private final String TAG = NetworkChangeReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            boolean online = isOnline(context);

            // if not online, stop mqtt service;
            // if online, start again
            Intent serviceIntent = new Intent(context, SamService.class);
            if (online) {
                Log.d(TAG, "Device online, start service");
                context.startService(serviceIntent);
            } else {
                Log.d(TAG, "Device offline, stop service");
                context.stopService(serviceIntent);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, e.getMessage());
        }
    }

    private boolean isOnline(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();

        //should check null because in airplane mode it will be null
        return (netInfo != null && netInfo.isConnected());
    }
}
