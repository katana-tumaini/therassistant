package com.example.therassistant2;

public class ModelNotification {

    private String id; // Required to uniquely target and update read states in Firestore
    private String userId;
    private String title;
    private String message;
    private long timestamp;
    private boolean read;

    // Custom properties support therapists & clients notification logic:
    // "booking_request", "booking_accepted", "booking_declined", "message", "session_nearing"
    private String type;
    private String senderName;
    private String bookingId;
    private String bookingDate;
    private String bookingTime;
    private String bookingStatus; // "pending", "accepted", "declined"

    // Default public constructor required for firestore doc.toObject() conversions
    public ModelNotification() {}

    public ModelNotification(String id, String userId, String title, String message, long timestamp, boolean read,
                             String type, String senderName, String bookingId, String bookingDate, String bookingTime, String bookingStatus) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.message = message;
        this.timestamp = timestamp;
        this.read = read;
        this.type = type;
        this.senderName = senderName;
        this.bookingId = bookingId;
        this.bookingDate = bookingDate;
        this.bookingTime = bookingTime;
        this.bookingStatus = bookingStatus;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public boolean isRead() { return read; }
    public void setRead(boolean read) { this.read = read; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }

    public String getBookingId() { return bookingId; }
    public void setBookingId(String bookingId) { this.bookingId = bookingId; }

    public String getBookingDate() { return bookingDate; }
    public void setBookingDate(String bookingDate) { this.bookingDate = bookingDate; }

    public String getBookingTime() { return bookingTime; }
    public void setBookingTime(String bookingTime) { this.bookingTime = bookingTime; }

    public String getBookingStatus() { return bookingStatus; }
    public void setBookingStatus(String bookingStatus) { this.bookingStatus = bookingStatus; }
}