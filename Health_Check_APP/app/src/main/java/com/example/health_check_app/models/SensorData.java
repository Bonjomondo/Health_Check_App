package com.example.health_check_app.models;

public class SensorData {
    private int heartRate;
    private int bloodOxygen;
    private float bodyTemperature;
    private float environmentTemperature;
    private int humidity;
    private MotionStatus motionStatus;
    private int steps;
    private long timestamp;
    private int batteryLevel;
    
    public enum MotionStatus {
        SEDENTARY,
        WALKING,
        FALL_DETECTED
    }
    
    public SensorData() {
        this.timestamp = System.currentTimeMillis();
    }
    
    // Getters and Setters
    public int getHeartRate() {
        return heartRate;
    }
    
    public void setHeartRate(int heartRate) {
        this.heartRate = heartRate;
    }
    
    public int getBloodOxygen() {
        return bloodOxygen;
    }
    
    public void setBloodOxygen(int bloodOxygen) {
        this.bloodOxygen = bloodOxygen;
    }
    
    public float getBodyTemperature() {
        return bodyTemperature;
    }
    
    public void setBodyTemperature(float bodyTemperature) {
        this.bodyTemperature = bodyTemperature;
    }
    
    public float getEnvironmentTemperature() {
        return environmentTemperature;
    }
    
    public void setEnvironmentTemperature(float environmentTemperature) {
        this.environmentTemperature = environmentTemperature;
    }
    
    public int getHumidity() {
        return humidity;
    }
    
    public void setHumidity(int humidity) {
        this.humidity = humidity;
    }
    
    public MotionStatus getMotionStatus() {
        return motionStatus;
    }
    
    public void setMotionStatus(MotionStatus motionStatus) {
        this.motionStatus = motionStatus;
    }
    
    public int getSteps() {
        return steps;
    }
    
    public void setSteps(int steps) {
        this.steps = steps;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
    
    public int getBatteryLevel() {
        return batteryLevel;
    }
    
    public void setBatteryLevel(int batteryLevel) {
        this.batteryLevel = batteryLevel;
    }
}
