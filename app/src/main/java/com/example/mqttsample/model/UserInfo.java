package com.example.mqttsample.model;

public class UserInfo {
    String username;
    String licensePlate;

    public UserInfo(String username, String licensePlate) {
        this.username = username;
        this.licensePlate = licensePlate;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getLicensePlate() {
        return licensePlate;
    }

    public void setLicensePlate(String licensePlate) {
        this.licensePlate = licensePlate;
    }
}
