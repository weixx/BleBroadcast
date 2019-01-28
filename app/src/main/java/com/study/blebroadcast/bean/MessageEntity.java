package com.study.blebroadcast.bean;

import java.util.Date;

public class MessageEntity {
    private String content = "";
    private boolean isSend = true;
    private Date time = new Date();

    public MessageEntity() {
    }

    public MessageEntity(String content, boolean isSend, Date time) {
        this.content = content;
        this.isSend = isSend;
        this.time = time;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isSend() {
        return isSend;
    }

    public void setSend(boolean send) {
        isSend = send;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MessageEntity that = (MessageEntity) o;

        if (isSend != that.isSend) return false;
        return content != null ? content.equals(that.content) : that.content == null;
    }

    @Override
    public int hashCode() {
        int result = content != null ? content.hashCode() : 0;
        result = 31 * result + (isSend ? 1 : 0);
        return result;
    }
}
