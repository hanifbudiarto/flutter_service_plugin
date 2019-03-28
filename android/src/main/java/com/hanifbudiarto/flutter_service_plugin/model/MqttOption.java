package com.hanifbudiarto.flutter_service_plugin.model;

public class MqttOption {
    private String rule;
    private double threshold;
    private boolean notify;
    private boolean alert;
    private String text;

    public MqttOption(String rule, double value, boolean notify, boolean alert, String text) {
        this.rule = rule;
        this.threshold = value;
        this.notify = notify;
        this.alert = alert;
        this.text = text;
    }

    public String getRule() {
        return rule;
    }

    public void setRule(String rule) {
        this.rule = rule;
    }

    public double getThreshold() {
        return threshold;
    }

    public void setThreshold(double value) {
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

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
