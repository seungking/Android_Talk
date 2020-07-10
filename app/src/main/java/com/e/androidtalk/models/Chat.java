package com.e.androidtalk.models;

import java.util.Date;

import lombok.Data;

/**
 */
@Data
public class Chat {

    private String chatId;
    private String title;
    private Date createDate;
    private TextMessage lastMessage;
    private boolean disabled;
    private int totalUnreadCount;


    public Date getCreateDate() {
        return createDate;
    }

    public int getTotalUnreadCount() {
        return totalUnreadCount;
    }

    public String getChatId() {
        return chatId;
    }

    public String getTitle() {
        return title;
    }

    public TextMessage getLastMessage() {
        return lastMessage;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    public void setLastMessage(TextMessage lastMessage) {
        this.lastMessage = lastMessage;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setTotalUnreadCount(int totalUnreadCount) {
        this.totalUnreadCount = totalUnreadCount;
    }
}
