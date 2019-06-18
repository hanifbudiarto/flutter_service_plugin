package com.hanifbudiarto.flutter_service_plugin.model;

public class DeviceNotification {
    public String getDeviceId() {
        return deviceId;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public String getDeviceSn() {
        return deviceSn;
    }

    public String getDeveloperId() {
        return developerId;
    }

    public boolean isNotify() {
        return notify;
    }

    public boolean isAlarm() {
        return alarm;
    }

    public String getTopic() {
        return topic;
    }

    public String getLastState() {
        return lastState;
    }

    public String getNotifyChecked() {
        return notifyChecked;
    }

    public String getAlarmChecked() {
        return alarmChecked;
    }

    final String deviceId;
    final String deviceName;
    final String deviceSn;
    final String developerId;
    final boolean notify;
    final boolean alarm;
    final String topic;
    final String lastState;
    final String notifyChecked;
    final String alarmChecked;

    public DeviceNotification(String deviceId,
                              String deviceName,
                              String deviceSn,
                              String developerId,
                              int notify,
                              int alarm,
                              String topic,
                              String lastState,
                              String notifyChecked,
                              String alarmChecked
    ) {
        this.deviceId = deviceId;
        this.deviceName = deviceName;
        this.deviceSn = deviceSn;
        this.developerId = developerId;
        this.notify = notify == 1;
        this.alarm = alarm == 1;
        this.topic = topic;
        this.lastState = lastState;
        this.notifyChecked = notifyChecked;
        this.alarmChecked = alarmChecked;
    }
}
