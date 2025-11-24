package com.example.therassistant2;

public class Session {
    private String title;
    private String date;
    private String time;
    private String details;
    private String clientEmail;
    private String clientFirstName;
    private String clientLastName;
    private String therapistName;  // Name of the therapist
    private String therapistEmail; // Email of the therapist

    // Default constructor required for calls to DataSnapshot.getValue(Session.class)
    public Session() {
    }

    public Session(String title, String date, String time, String details, String clientEmail, String clientFirstName, String clientLastName, String therapistName, String therapistEmail) {
        this.date = date;
        this.time = time;
        this.details = details;
        this.clientEmail = clientEmail;
        this.clientFirstName = clientFirstName;
        this.clientLastName = clientLastName;
        this.therapistName = therapistName;
        this.therapistEmail = therapistEmail;
    }

    // Getters and setters

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public String getClientEmail() {
        return clientEmail;
    }

    public void setClientEmail(String clientEmail) {
        this.clientEmail = clientEmail;
    }

    public String getClientFirstName() {
        return clientFirstName;
    }

    public void setClientFirstName(String clientFirstName) {
        this.clientFirstName = clientFirstName;
    }

    public String getClientLastName() {
        return clientLastName;
    }

    public void setClientLastName(String clientLastName) {
        this.clientLastName = clientLastName;
    }

    public String getTherapistName() {
        return therapistName;
    }

    public void setTherapistName(String therapistName) {
        this.therapistName = therapistName;
    }

    public String getTherapistEmail() {
        return therapistEmail;
    }

    public void setTherapistEmail(String therapistEmail) {
        this.therapistEmail = therapistEmail;
    }
}
