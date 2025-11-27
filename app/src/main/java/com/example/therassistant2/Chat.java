package com.example.therassistant2;

import java.util.List;

public class Chat {

    // Original Fields (Kept Exactly)
    private String chatId;
    private String firstName;
    private String lastName;
    private List<String> participants;

    // New Fields for Messaging
    private String lastMessage;
    private long lastTimestamp;
    private String lastSenderId;

    // Required Empty Constructor for Firebase
    public Chat() {
    }

    // Original Constructor (KePT)
    public Chat(String chatId, String firstName, String lastName) {
        this.chatId = chatId;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    // Original Constructor with Participants (KePT)
    public Chat(String chatId, String firstName, String lastName, List<String> participants) {
        this.chatId = chatId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.participants = participants;
    }

    // Full Constructor (For Active Chats)
    public Chat(String chatId, String firstName, String lastName,
                List<String> participants,
                String lastMessage,
                long lastTimestamp,
                String lastSenderId) {

        this.chatId = chatId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.participants = participants;
        this.lastMessage = lastMessage;
        this.lastTimestamp = lastTimestamp;
        this.lastSenderId = lastSenderId;
    }

    // Getters
    public String getChatId() {
        return chatId;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public List<String> getParticipants() {
        return participants;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public long getLastTimestamp() {
        return lastTimestamp;
    }

    public String getLastSenderId() {
        return lastSenderId;
    }

    // Setters (Needed for Firebase updates)
    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setParticipants(List<String> participants) {
        this.participants = participants;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public void setLastTimestamp(long lastTimestamp) {
        this.lastTimestamp = lastTimestamp;
    }

    public void setLastSenderId(String lastSenderId) {
        this.lastSenderId = lastSenderId;
    }
}
