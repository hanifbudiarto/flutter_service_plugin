package com.hanifbudiarto.flutter_service_plugin.model;

public class User {

    private String apiKey;
    private String apiToken;
    private String broker;

    public User(String apiKey, String apiToken, String broker) {
        this.apiKey = apiKey;
        this.apiToken = apiToken;
        this.broker = broker;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getApiToken() {
        return apiToken;
    }

    public void setApiToken(String apiToken) {
        this.apiToken = apiToken;
    }

    public String getBroker() {
        return broker;
    }

    public void setBroker(String broker) {
        this.broker = broker;
    }
}
