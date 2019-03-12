package com.hanifbudiarto.flutter_service_plugin.model;

public class MqttNotification {
    private String analyticId;
    private String resourceId;
    private String topic;
    private MqttOption option;
    private String deviceName;
    private String analyticTitle;

    public MqttNotification(String analyticId, String resourceId, String topic, MqttOption option,
                            String deviceName, String analyticTitle) {
        this.analyticId = analyticId;
        this.resourceId = resourceId;
        this.topic = topic;
        this.option = option;
        this.deviceName = deviceName;
        this.analyticTitle = analyticTitle;
    }

    public String getAnalyticId() {
        return analyticId;
    }

    public void setAnalyticId(String analyticId) {
        this.analyticId = analyticId;
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public MqttOption getOption() {
        return option;
    }

    public void setOption(MqttOption option) {
        this.option = option;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getAnalyticTitle() {
        return analyticTitle;
    }

    public void setAnalyticTitle(String analyticTitle) {
        this.analyticTitle = analyticTitle;
    }
}
