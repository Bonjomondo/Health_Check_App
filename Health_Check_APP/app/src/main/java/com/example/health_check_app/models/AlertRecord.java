package com.example.health_check_app.models;

public class AlertRecord {
    private String time;
    private String type;
    private String message;
    private long timestamp;
    
    public AlertRecord(String type, String message) {
        this.type = type;
        this.message = message;
        this.timestamp = System.currentTimeMillis();
        this.time = formatTime(timestamp);
    }
    
    private String formatTime(long timestamp) {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault());
        return sdf.format(new java.util.Date(timestamp));
    }
    
    public String getTime() {
        return time;
    }
    
    public String getType() {
        return type;
    }
    
    public String getMessage() {
        return message;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
}
