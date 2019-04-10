package com.hanifbudiarto.flutter_service_plugin.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class OnBootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent serviceIntent = new Intent(context, SamService.class);
        context.stopService(serviceIntent);
        context.startService(serviceIntent);
    }
}
