package com.hanifbudiarto.flutter_service_plugin;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;

import com.hanifbudiarto.flutter_service_plugin.service.NetworkChangeService;
import com.hanifbudiarto.flutter_service_plugin.service.SamService;

import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry.Registrar;

/** FlutterServicePlugin */
public class FlutterServicePlugin implements MethodCallHandler {

  private final String TAG = FlutterServicePlugin.class.getSimpleName();
  private Activity activity;

  private FlutterServicePlugin(Activity activity) {
    this.activity = activity;
  }

  /** Plugin registration. */
  public static void registerWith(Registrar registrar) {
    final MethodChannel channel = new MethodChannel(registrar.messenger(), "flutter_service_plugin");
    channel.setMethodCallHandler(new FlutterServicePlugin(registrar.activity()));
  }

  @Override
  public void onMethodCall(MethodCall call, Result result) {
    switch (call.method) {
      case "restartMqttService":
        restartMqttService();
        break;
      case "stopMqttService":
        stopMqttService();
        break;
      case "register":
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

          try {
            // register her is ignoring battery optimization
            Intent ignoreOptimizationIntent = new Intent();

            String packageName = activity.getPackageName();
            PowerManager pm = (PowerManager) activity.getSystemService(Context.POWER_SERVICE);
            if (pm.isIgnoringBatteryOptimizations(packageName)) {
              ignoreOptimizationIntent.setAction(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
            }
            else {
              ignoreOptimizationIntent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
              ignoreOptimizationIntent.setData(Uri.parse("package:" + packageName));
            }
            activity.startActivity(ignoreOptimizationIntent);
          }
          catch(Exception e) {
            Log.d(TAG, e.getMessage());
          }
        }
        break;
      default:
        result.notImplemented();
        break;
    }
  }

  private boolean stopMqttService() {
    Intent intent = new Intent(activity, SamService.class);
    boolean stopped = activity.stopService(intent);
    Log.e(TAG,"Service stopped " + stopped);

    return stopped;
  }

  // restarting mqtt service
  private void restartMqttService() {
    Intent intent = new Intent(activity, SamService.class);
    stopMqttService();

    activity.startService(intent);
  }


}
