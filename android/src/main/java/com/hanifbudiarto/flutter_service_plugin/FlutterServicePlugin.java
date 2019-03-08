package com.hanifbudiarto.flutter_service_plugin;

import android.app.Activity;
import android.content.Intent;

import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry.Registrar;

/** FlutterServicePlugin */
public class FlutterServicePlugin implements MethodCallHandler {

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
      case "startMqttService":
        startMqttService();
        break;
      case "restartMqttService":
        restartMqttService();
        break;
      case "stopMqttService":
        stopMqttService();
        break;
      default:
        result.notImplemented();
        break;
    }
  }

  // starting mqtt service
  private void startMqttService() {
      Intent intent = new Intent(activity, SamService.class);
      activity.startService(intent);
  }

  // restarting mqtt service
  private void restartMqttService() {
      stopMqttService();
      startMqttService();
  }

  private void stopMqttService() {
      Intent intent = new Intent(activity, SamService.class);
      activity.stopService(intent);
  }

}
