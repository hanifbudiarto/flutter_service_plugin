package com.hanifbudiarto.flutter_service_plugin.model;

public class UserPrefs {

    private String userAccessToken;
    private String userRefreshToken;
    private String developerRefreshToken;
    private String broker;

    public UserPrefs(String userAccessToken, String userRefreshToken, String developerRefreshToken, String broker) {
        this.userAccessToken = userAccessToken;
        this.userRefreshToken = userRefreshToken;
        this.developerRefreshToken = developerRefreshToken;
        this.broker = broker;
    }

    public String getUserAccessToken() {
        return userAccessToken;
    }

    public void setUserAccessToken(String userAccessToken) {
        this.userAccessToken = userAccessToken;
    }

    public String getUserRefreshToken() {
        return userRefreshToken;
    }

    public void setUserRefreshToken(String userRefreshToken) {
        this.userRefreshToken = userRefreshToken;
    }

    public String getDeveloperRefreshToken() {
        return developerRefreshToken;
    }

    public void setDeveloperRefreshToken(String developerRefreshToken) {
        this.developerRefreshToken = developerRefreshToken;
    }

    public String getBroker() {
        return broker;
    }

    public void setBroker(String broker) {
        this.broker = broker;
    }
}
