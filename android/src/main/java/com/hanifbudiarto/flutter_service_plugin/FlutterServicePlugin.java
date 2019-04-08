package com.hanifbudiarto.flutter_service_plugin;

import android.app.Activity;
import android.content.Intent;
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
        // starting network change service
        // Intent networkChangeService = new Intent(activity, NetworkChangeService.class);
        // activity.startService(networkChangeService);
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
