package com.example.therassistant2;

public class Message{
    private String text, textID, Uid;
    long timestamp;

    public Message() {

    }

    public Message(String text, long timestamp, String Uid){
        this.text = text;
        this.timestamp = timestamp;
        this.Uid = Uid;
    }

    public Message(String text, String Uid){
        this.text = text;
        this.Uid = Uid;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getTextID() {
        return textID;
    }

    public void setTextID(String textID) {
        this.textID = textID;
    }

    public String getUid() {
        return Uid;
    }

    public void setUid(String uid) {
        Uid = uid;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}

