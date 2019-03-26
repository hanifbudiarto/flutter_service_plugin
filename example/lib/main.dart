import 'package:flutter/material.dart';

import 'package:flutter_service_plugin/flutter_service_plugin.dart';

void main() => runApp(MyApp());

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: Text('Running on Android'),
        ),
        body: Center(
          child: RaisedButton(onPressed: () async {
            try {
              FlutterServicePlugin.restartService();
            }
            catch (e) {
              print(e.toString());
            }
          }, child: Text("Run MQTT Service"),),
        ),
      ),
    );
  }
}
