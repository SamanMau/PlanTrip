package com.example.PlanTrip.Entity;

import java.time.Duration;
import java.time.LocalDateTime;

public class TokenManager {
    private String accessToken;
    private LocalDateTime createdAt;

    public void setAccessToken(String token) {
        this.accessToken = token;
        this.createdAt = LocalDateTime.now(); // sparar tidpunkt när token skapades
    }

    public boolean isTokenExpired() {
        if (createdAt == null) return true;
        Duration duration = Duration.between(createdAt, LocalDateTime.now());
        return duration.toMinutes() >= 60; // kolla om det gått 60 minuter
    }

    public String getAccessToken() {
        return accessToken;
    }
}