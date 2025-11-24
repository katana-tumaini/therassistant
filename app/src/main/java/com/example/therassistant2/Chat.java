package com.example.therassistant2;

import java.util.List;

public class Chat {
    private String chatId;
    private String firstName;
    private String lastName;
    private List<String> participants;

    public Chat() {
        // Default constructor required for calls to DataSnapshot.getValue(Chat.class)
    }

    public Chat(String chatId, String firstName, String lastName) {
        this.chatId = chatId;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public Chat(String chatId, String firstName, String lastName, List<String> participants) {
        this.chatId = chatId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.participants = participants;
    }

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
}
