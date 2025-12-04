package com.example.therassistant2;

import java.util.List;

public class Chat {
    private String chatId;
    private String firstName;
    private String lastName;
    private List<String> participants;
    private String lastMessage;
    private long lastTimestamp;

    // Empty constructor required by Firestore
    public Chat() {}

    // Full constructor
    public Chat(String chatId, String firstName, String lastName, List<String> participants, String lastMessage, long lastTimestamp) {
        this.chatId = chatId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.participants = participants;
        this.lastMessage = lastMessage;
        this.lastTimestamp = lastTimestamp;
    }

    // Helper simpler constructor if you don't have participants yet
    public Chat(String chatId, String firstName, String lastName, List<String> participants) {
        this(chatId, firstName, lastName, participants, "", 0L);
    }

    // Getters and setters
    public String getChatId() { return chatId; }
    public void setChatId(String chatId) { this.chatId = chatId; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public List<String> getParticipants() { return participants; }
    public void setParticipants(List<String> participants) { this.participants = participants; }

    public String getLastMessage() { return lastMessage; }
    public void setLastMessage(String lastMessage) { this.lastMessage = lastMessage; }

    public long getLastTimestamp() { return lastTimestamp; }
    public void setLastTimestamp(long lastTimestamp) { this.lastTimestamp = lastTimestamp; }

    // Convenience
    public String getDisplayName() {
        return (firstName == null ? "" : firstName) + " " + (lastName == null ? "" : lastName);
    }
}
