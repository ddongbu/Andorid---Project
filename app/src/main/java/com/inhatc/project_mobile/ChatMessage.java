package com.inhatc.project_mobile;

public class ChatMessage {
    private String message;
    private String name;
    private String timestamp;
    private String uid;

    public ChatMessage(){}
    public ChatMessage(String message, String name, String timestamp, String uid) {
        this.message = message;
        this.name = name;
        this.timestamp = timestamp;
        this.uid = uid;
    }
    public boolean isMyMessage(String loginUid){
        return uid.equals(loginUid);
    }
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}


