package com.hanifbudiarto.flutter_service_plugin_example;

import android.content.Intent;
import android.os.Bundle;

import com.hanifbudiarto.flutter_service_plugin.service.NetworkChangeService;

import io.flutter.app.FlutterActivity;
import io.flutter.plugins.GeneratedPluginRegistrant;

public class MainActivity extends FlutterActivity {
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // starting network change service
    Intent networkChangeService = new Intent(this, NetworkChangeService.class);
    startService(networkChangeService);

    GeneratedPluginRegistrant.registerWith(this);
  }
}
