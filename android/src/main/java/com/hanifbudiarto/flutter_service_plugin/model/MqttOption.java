package com.hanifbudiarto.flutter_service_plugin.model;

public class MqttOption {
    private String rule;
    private double threshold;
    private boolean notify;
    private boolean alert;

    public MqttOption(String rule, double value, boolean notify, boolean alert) {
        this.rule = rule;
        this.threshold = value;
        this.notify = notify;
        this.alert = alert;
    }

    public String getRule() {
        return rule;
    }

    public void setRule(String rule) {
        this.rule = rule;
    }

    public double getValue() {
        return threshold;
    }

    public void setValue(double value) {
        this.threshold = value;
    }

    public boolean isNotify() {
        return notify;
    }

    public void setNotify(boolean notify) {
        this.notify = notify;
    }

    public boolean isAlert() {
        return alert;
    }

    public void setAlert(boolean alert) {
        this.alert = alert;
    }
}
