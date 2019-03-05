package com.hanifbudiarto.flutter_service_plugin.model;

public class MqttNotification {
    private String analyticId;
    private String resourceId;
    private String topic;
    private MqttOption option;

    public MqttNotification(String analyticId, String resourceId, String topic, MqttOption option) {
        this.analyticId = analyticId;
        this.resourceId = resourceId;
        this.topic = topic;
        this.option = option;
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
}
