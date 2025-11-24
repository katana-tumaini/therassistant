package com.example.therassistant2;

public class Client {
    private String clientId;
    private String clientFirstName;
    private String clientLastName;
    private String clientEmail;
    private String clientDiagnosis;
    private String therapistId;

    public Client() {
        // Default constructor required for calls to DataSnapshot.getValue(Client.class)
    }

    public Client(String clientId, String clientFirstName, String clientLastName, String clientEmail, String clientDiagnosis, String therapistId) {
        this.clientId = clientId;
        this.clientFirstName = clientFirstName;
        this.clientLastName = clientLastName;
        this.clientEmail = clientEmail;
        this.clientDiagnosis = clientDiagnosis;
        this.therapistId = therapistId;
    }

    public String getClientId() {
        return clientId;
    }

    public String getClientFirstName() {
        return clientFirstName;
    }

    public String getClientLastName() {
        return clientLastName;
    }

    public String getClientEmail() {
        return clientEmail;
    }

    public String getClientDiagnosis() {
        return clientDiagnosis;
    }

    public String getTherapistId() {
        return therapistId;
    }
}
