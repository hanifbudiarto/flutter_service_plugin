import 'dart:async';

import 'package:flutter/services.dart';

class FlutterServicePlugin {
  static const MethodChannel _channel =
      const MethodChannel('flutter_service_plugin');

  static Future restartService() async {
    await _channel.invokeMethod("restartMqttService");
  }
}
