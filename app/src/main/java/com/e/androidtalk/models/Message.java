package com.e.androidtalk.models;

import java.util.Date;
import java.util.List;

import lombok.Data;

/**
 */
@Data
public class Message {

    private String messageId;
    private User messageUser;
    private String chatId;
    private int unreadCount;
    private Date messageDate;
    private MessageType messageType;
    private List<String> readUserList;


    public enum MessageType {
        TEXT, PHOTO, EXIT
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public void setMessageDate(Date messageDate) {
        this.messageDate = messageDate;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public void setMessageType(MessageType messageType) {
        this.messageType = messageType;
    }

    public void setMessageUser(User messageUser) {
        this.messageUser = messageUser;
    }

    public void setReadUserList(List<String> readUserList) {
        this.readUserList = readUserList;
    }

    public void setUnreadCount(int unreadCount) {
        this.unreadCount = unreadCount;
    }

    public String getChatId() {
        return chatId;
    }

    public Date getMessageDate() {
        return messageDate;
    }

    public int getUnreadCount() {
        return unreadCount;
    }

    public List<String> getReadUserList() {
        return readUserList;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public User getMessageUser() {
        return messageUser;
    }

    public String getMessageId() {
        return messageId;
    }
}
