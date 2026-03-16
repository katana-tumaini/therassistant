package com.example.therassistant2;

public class ModelNotification {

    private String userId;
    private String title;
    private String message;
    private long timestamp;
    private boolean read;

    public ModelNotification(){}

    public String getUserId() { return userId; }
    public String getTitle() { return title; }
    public String getMessage() { return message; }
    public long getTimestamp() { return timestamp; }
    public boolean isRead() { return read; }

}
