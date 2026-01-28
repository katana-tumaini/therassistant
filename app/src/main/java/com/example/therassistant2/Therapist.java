package com.example.therassistant2;

import java.io.Serializable;

public class Therapist implements Serializable {
    private String uid;
    private String name;
    private String therapistType;
    private String phoneNumber;
    private String availability;
    private String profileImageUrl;
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private String Age;

    public Therapist() {
        // Default constructor required for calls to DataSnapshot.getValue(Therapist.class)
    }

    public Therapist(String uid, String name, String TherapistType, String phoneNumber, String availability, String profileImageUrl, String firstName, String lastName, String email, String password) {
        this.uid = uid;
        this.name = name;
        this.therapistType = TherapistType;
        this.phoneNumber = phoneNumber;
        this.availability = availability;
        this.profileImageUrl = profileImageUrl;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;

    }

    // Getters
    public String getUid() {
        return uid;
    }

    public String getName() {
        return name;
    }
    public String gettherapisttype() {
        return therapistType;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getAvailability() {
        return availability;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    // New getters
    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getAge() {
        return Age;
    }

    // Setters
    public void setUid(String uid) {
        this.uid = uid;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void settherapisttype(String TherapistType) {
        this.therapistType = TherapistType;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setAvailability(String availability) {
        this.availability = availability;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    // New setters
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }
    public void setAge(String age) {
        Age = age;
    }
}
