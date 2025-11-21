package com.example.health_check_app;

import android.content.Context;
import android.content.SharedPreferences;

public class AppConfig {
    private static final String PREFS_NAME = "HealthCheckSettings";
    
    // MQTT Configuration Keys
    private static final String KEY_MQTT_BROKER = "mqtt_broker";
    private static final String KEY_MQTT_USERNAME = "mqtt_username";
    private static final String KEY_MQTT_PASSWORD = "mqtt_password";
    
    // Threshold Keys
    private static final String KEY_HEART_RATE_MAX = "heartRateMax";
    private static final String KEY_TEMPERATURE_MAX = "temperatureMax";
    
    // Feature Keys
    private static final String KEY_SEDENTARY_REMINDER = "sedentaryReminder";
    private static final String KEY_VIBRATION_FEEDBACK = "vibrationFeedback";
    
    // Default Values
    private static final String DEFAULT_MQTT_BROKER = "tcp://iot-instance.aliyuncs.com:1883";
    private static final int DEFAULT_HEART_RATE_MAX = 100;
    private static final float DEFAULT_TEMPERATURE_MAX = 37.3f;
    
    private SharedPreferences prefs;
    
    public AppConfig(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }
    
    // MQTT Configuration
    public String getMqttBroker() {
        return prefs.getString(KEY_MQTT_BROKER, DEFAULT_MQTT_BROKER);
    }
    
    public void setMqttBroker(String broker) {
        prefs.edit().putString(KEY_MQTT_BROKER, broker).apply();
    }
    
    public String getMqttUsername() {
        return prefs.getString(KEY_MQTT_USERNAME, "");
    }
    
    public void setMqttUsername(String username) {
        prefs.edit().putString(KEY_MQTT_USERNAME, username).apply();
    }
    
    public String getMqttPassword() {
        return prefs.getString(KEY_MQTT_PASSWORD, "");
    }
    
    public void setMqttPassword(String password) {
        prefs.edit().putString(KEY_MQTT_PASSWORD, password).apply();
    }
    
    // Threshold Configuration
    public int getHeartRateMax() {
        return prefs.getInt(KEY_HEART_RATE_MAX, DEFAULT_HEART_RATE_MAX);
    }
    
    public void setHeartRateMax(int value) {
        prefs.edit().putInt(KEY_HEART_RATE_MAX, value).apply();
    }
    
    public float getTemperatureMax() {
        return prefs.getFloat(KEY_TEMPERATURE_MAX, DEFAULT_TEMPERATURE_MAX);
    }
    
    public void setTemperatureMax(float value) {
        prefs.edit().putFloat(KEY_TEMPERATURE_MAX, value).apply();
    }
    
    // Feature Switches
    public boolean isSedentaryReminderEnabled() {
        return prefs.getBoolean(KEY_SEDENTARY_REMINDER, false);
    }
    
    public void setSedentaryReminderEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_SEDENTARY_REMINDER, enabled).apply();
    }
    
    public boolean isVibrationFeedbackEnabled() {
        return prefs.getBoolean(KEY_VIBRATION_FEEDBACK, true);
    }
    
    public void setVibrationFeedbackEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_VIBRATION_FEEDBACK, enabled).apply();
    }
}
