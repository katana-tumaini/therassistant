package com.example.therassistant2;

public class ModelBookingRequests {
    private String requestId;
    private String clientId;
    private String clientName;
    private String therapistId;
    private String date;
    private String time;
    private String status;
    private String meetingType;

    public ModelBookingRequests(){

    }

    public ModelBookingRequests(String requestId, String clientId, String clientName, String therapistId, String date, String time, String status, String meetingType){
        this.requestId = requestId;
        this.clientId = clientId;
        this.clientName = clientName;
        this.therapistId = therapistId;
        this.date = date;
        this.time = time;
        this.status = status;
        this.meetingType = meetingType;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String getTherapistId() {
        return therapistId;
    }

    public void setTherapistId(String therapistId) {
        this.therapistId = therapistId;
    }

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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMeetingType() {
        return meetingType;
    }

    public void setMeetingType(String meetingType) {
        this.meetingType = meetingType;
    }
}
