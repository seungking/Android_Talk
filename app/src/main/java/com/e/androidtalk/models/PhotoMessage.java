package com.e.androidtalk.models;

import java.util.Date;
import java.util.List;

import lombok.Data;

/**
 */
@Data
public class PhotoMessage extends Message {
    private String photoUrl;

    @Override
    public Date getMessageDate() {
        return super.getMessageDate();
    }

    @Override
    public int getUnreadCount() {
        return super.getUnreadCount();
    }

    @Override
    public List<String> getReadUserList() {
        return super.getReadUserList();
    }

    @Override
    public MessageType getMessageType() {
        return super.getMessageType();
    }

    @Override
    public String getChatId() {
        return super.getChatId();
    }

    @Override
    public String getMessageId() {
        return super.getMessageId();
    }

    @Override
    public User getMessageUser() {
        return super.getMessageUser();
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    @Override
    public void setChatId(String chatId) {
        super.setChatId(chatId);
    }

    @Override
    public void setMessageDate(Date messageDate) {
        super.setMessageDate(messageDate);
    }

    @Override
    public void setMessageId(String messageId) {
        super.setMessageId(messageId);
    }

    @Override
    public void setMessageType(MessageType messageType) {
        super.setMessageType(messageType);
    }

    @Override
    public void setMessageUser(User messageUser) {
        super.setMessageUser(messageUser);
    }

    @Override
    public void setReadUserList(List<String> readUserList) {
        super.setReadUserList(readUserList);
    }

    @Override
    public void setUnreadCount(int unreadCount) {
        super.setUnreadCount(unreadCount);
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }
}
