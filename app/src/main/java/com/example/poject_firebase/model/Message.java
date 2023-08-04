package com.example.poject_firebase.model;

import java.io.Serializable;

// TODO: 2023-08-04 메시지 저장 클래스 
public class Message implements Serializable {
    private String uid;
    private String content;
    private String date;
    private Boolean confirmed = false;

    public Message() {
    }

    public Message(String uid, String content, String date, Boolean confirmed) {
        this.uid = uid;
        this.content = content;
        this.date = date;
        this.confirmed = confirmed;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Boolean getConfirmed() {
        return confirmed;
    }

    public void setConfirmed(Boolean confirmed) {
        this.confirmed = confirmed;
    }
}
