package com.app.chat;

public class MessageRequest {

    private String sender_name;
    private String receiver_name;
    private String message;

    public MessageRequest() {
    }

    public MessageRequest(String sender_name, String receiver_name, String message) {
        this.sender_name = sender_name;
        this.receiver_name = receiver_name;
        this.message = message;
    }

    public String getReceiver_name() {
        return receiver_name;
    }

    public void setReceiver_name(String receiver_name) {
        this.receiver_name = receiver_name;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSender_name() {
        return sender_name;
    }

    public void setSender_name(String sender_name) {
        this.sender_name = sender_name;
    }
}
