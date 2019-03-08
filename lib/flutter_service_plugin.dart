import 'dart:async';

import 'package:flutter/services.dart';

class FlutterServicePlugin {
  static const MethodChannel _channel =
      const MethodChannel('flutter_service_plugin');

  static Future startService() async {
    await _channel.invokeMethod("startMqttService");
  }

  static Future stopService() async {
    await _channel.invokeMethod("stopMqttService");
  }

  static Future restartService() async {
    await _channel.invokeMethod("restartMqttService");
  }
}
