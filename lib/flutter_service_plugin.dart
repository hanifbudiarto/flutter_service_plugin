import 'package:flutter/services.dart';

class FlutterServicePlugin {
  static const MethodChannel _channel =
      const MethodChannel('flutter_service_plugin');

  static restartService() {
    _channel.invokeMethod("restartMqttService");
  }

  static register() {
    _channel.invokeMethod("register");
  }
}
