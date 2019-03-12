package com.hanifbudiarto.flutter_service_plugin.model;

import java.util.Date;

public class MqttPayload {

    private Date datetime;
    private String value;

    public MqttPayload(Date datetime, String value) {
        this.datetime = datetime;
        this.value = value;
    }

    public Date getDatetime() {
        return datetime;
    }

    public void setDatetime(Date datetime) {
        this.datetime = datetime;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
