package com.hanifbudiarto.flutter_service_plugin.model;

import java.util.Date;

public class MqttPayload {

    private Date datetime;
    private double value;

    public MqttPayload(Date datetime, double value) {
        this.datetime = datetime;
        this.value = value;
    }

    public Date getDatetime() {
        return datetime;
    }

    public void setDatetime(Date datetime) {
        this.datetime = datetime;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }
}
